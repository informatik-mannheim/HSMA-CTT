package de.hs_mannheim.informatik.ct.util;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class DocxTemplate<T> {
    private XWPFDocument document;
    private final File templateFile;
    private List<T> dataSource;
    private final TextTemplate<T> textFormatter;
    private final Function<T, byte[]> imageGenerator;

    // The id attribute of wp:docPr tags has to be unique, but can be arbitrarily large
    private int uniqueID = 1000;

    // To differentiate our own unique id's from original we check their length as originals should be shorter than 4 digits.
    private final int uniqueIDDigits = (int) Math.log10(uniqueID);

    // Precompile regex patterns used for filling the template
    private final Pattern docxImageReplacer = Pattern.compile("<a:blip r:embed=\"rId\\d\">");
    private final Pattern docxTextReplacer = Pattern.compile("(<w:t(?: .*)?>.*)#(\\w)(.*</w:t>)");
    // Regex templates for fixing errors in the resulting document
    private final Pattern docPrUniqueIdFix = Pattern.compile(String.format("<wp:docPr id=\"\\d{1,%d}\"", uniqueIDDigits));

    public DocxTemplate(File templateFile, TextTemplate<T> textFormatter, Function<T, byte[]> imageGenerator) {
        this.templateFile = templateFile;
        this.textFormatter = textFormatter;
        this.imageGenerator = imageGenerator;
    }

    public XWPFDocument generate(List<T> dataSource) throws IOException, XmlException {
        try (val templateStream = new FileInputStream(templateFile)) {
            document = new XWPFDocument(templateStream);
        }

        this.dataSource = dataSource;
        val imageIds = addImagesToDocMedia();
        val templateBuffer = getTemplateBuffer();
        // The section properties is at the end of body and sets headers, footers, etc.
        val sectionProperties = (CTSectPr) document.getDocument().getBody().getSectPr().copy();

        // Remove template page from final doc
        document.getDocument().setBody(new XWPFDocument().getDocument().getBody());

        // Generate new Pages
        val pageDataSource = StreamSupport.stream(
                ZipPageData(dataSource, imageIds).spliterator(),
                false).collect(Collectors.toList());

        val templateXml = getParagraphTemplateXml(templateBuffer);

        pageDataSource
                .parallelStream()
                .unordered()
                .map(data -> new Indexed<>(data.index, applyPageParagraphXml(templateXml, data.value)))
                .sorted(Comparator.comparingInt(o -> o.index))
                .forEachOrdered(paragraphs -> {
                    for (int paragraphIndex = 0; paragraphIndex < paragraphs.value.size(); paragraphIndex++) {
                        val paragraph = document.createParagraph();
                        paragraph.getCTP().set(paragraphs.value.get(paragraphIndex));
                        val isLastPage = paragraphs.index == dataSource.size() - 1;
                        if (!isLastPage && paragraphIndex == paragraphs.value.size() - 1) {
                            paragraph.setPageBreak(true);
                        }
                    }
                });
        // Reapply the section properties
        document.getDocument().getBody().setSectPr(sectionProperties);

        return document;
    }

    private List<String> addImagesToDocMedia() {
        val doc = document;
        val indexDataSource = new ArrayList<Indexed<T>>(dataSource.size());
        for (int i = 0; i < dataSource.size(); i++) {
            indexDataSource.add(new Indexed<>(i, dataSource.get(i)));
        }
        val indexedList = indexDataSource
                .parallelStream()
                .unordered()
                .map(indexData -> new Indexed<>(indexData.index, imageGenerator.apply(indexData.value)))
                .map(image -> {
                    synchronized (doc) {
                        try {
                            return new Indexed<>(image.index, document.addPictureData(image.value, Document.PICTURE_TYPE_PNG));
                        } catch (InvalidFormatException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .collect(Collectors.toList());

        return indexedList
                .stream()
                .sorted(Comparator.comparingInt(indexed -> indexed.index))
                .map(Indexed::getValue)
                .collect(Collectors.toList());
    }

    private byte[] getTemplateBuffer() throws IOException {
        try (val bufferStream = new ByteArrayOutputStream()) {
            document.write(bufferStream);
            return bufferStream.toByteArray();
        }
    }

    @SneakyThrows
    private List<XmlObject> applyPageParagraphXml(List<String> templateXml, PageData<T> data) {
        val replacedParagraphs = new ArrayList<XmlObject>(templateXml.size());
        for (val template : templateXml) {
            val paragraphXml = applyTemplateToXml(data, template);
            replacedParagraphs.add(XmlObject.Factory.parse(paragraphXml));
        }

        return replacedParagraphs;
    }

    private List<String> getParagraphTemplateXml(byte[] templateBuffer) throws IOException {
        try (val templateStream = new ByteArrayInputStream(templateBuffer); val template = new XWPFDocument(templateStream)) {
            return template.getParagraphs()
                    .stream()
                    .map(xwpfParagraph -> xwpfParagraph.getCTP().xmlText())
                    .collect(Collectors.toList());
        }
    }

    private String applyTemplateToXml(PageData<T> pageData, String templateXml) {
        val data = pageData.data;
        val imgIndex = pageData.qrCodeId;

        // Replace text markers
        templateXml = complexReplaceAll(templateXml, docxTextReplacer, match -> {
            // Group 2 contains the placeholder without the #
            val placeholder = match.group(2);
            val replacement = textFormatter.apply(data, placeholder);
            // Save text and XML tags around the template placeholder (group 1 & 3)
            return match.group(1) + replacement + match.group(3);
        });

        // Replace qr code image
        templateXml = complexReplaceAll(templateXml, docxImageReplacer,
                matchResult -> String.format("<a:blip r:embed=\"%s\">", imgIndex));

        templateXml = complexReplaceAll(templateXml, docPrUniqueIdFix,
                matchResult -> String.format("<wp:docPr id=\"%d\"", uniqueID++));
        return templateXml;
    }

    private String complexReplaceAll(String template, Pattern pattern, Function<MatchResult, String> replacement) {
        while (true) {
            val matcher = pattern.matcher(template);
            if (!matcher.find()) {
                break;
            }

            template = matcher.replaceFirst(replacement.apply(matcher.toMatchResult()));
        }

        return template;
    }

    private Iterable<Indexed<PageData<T>>> ZipPageData(Iterable<T> data, Iterable<String> qrImageIds) {
        val dataIterator = data.iterator();
        val idIterator = qrImageIds.iterator();
        return () -> new Iterator<Indexed<PageData<T>>>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return dataIterator.hasNext() && idIterator.hasNext();
            }

            @Override
            public Indexed<PageData<T>> next() {
                return new Indexed<>(index++, new PageData<>(dataIterator.next(), idIterator.next()));
            }
        };
    }

    public interface TextTemplate<T> {
        String apply(T data, String templatePlaceholder);
    }

    @Data
    @AllArgsConstructor
    private static class PageData<T> {
        private T data;
        private String qrCodeId;
    }

    @Data
    @AllArgsConstructor
    private static class Indexed<T> {
        private int index;
        private T value;
    }
}
