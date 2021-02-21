package de.hs_mannheim.informatik.ct.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import lombok.val;
import lombok.var;

public class DocxTemplate<T> {
    private XWPFDocument document;
    private final File templateFile;
    private List<String> imageReferences;
    private List<T> dataSource;
    private final TextTemplate<T> textFormatter;
    private final Function<T, byte[]> imageGenerator;
    
    // The id attribute of wp:docPr tags has to be unique, but can be arbitrarily large
    private int uniqueID = 1000;
   
    // To differentiate our own unique id's from original we check their length as originals should be shorter than 4 digits.
    private final int uniqueIDDigits = (int) Math.log10(uniqueID);

    // Precompile regex patterns used for filling the template
    private final Pattern docxImageReplacer = Pattern.compile("<a:blip r:embed=\"rId\\d\">");
    private final Pattern docxTextReplacer = Pattern.compile("(<w:t>.*)#(\\w)(.*</w:t>)");
   
    // Regex templates for fixing errors in the resulting document
    private final Pattern docPrUniqueIdFix = Pattern.compile(String.format("<wp:docPr id=\"\\d{1,%d}\"", uniqueIDDigits));

    public DocxTemplate(File templateFile, TextTemplate<T> textFormatter, Function<T, byte[]> imageGenerator) {
        this.templateFile = templateFile;
        this.textFormatter = textFormatter;
        this.imageGenerator = imageGenerator;
    }

    public XWPFDocument generate(List<T> dataSource) throws IOException, InvalidFormatException {
        try (val templateStream = new FileInputStream(templateFile)) {
            document = new XWPFDocument(templateStream);
        }

        this.dataSource = dataSource;
        addImagesToDocMedia();
        val templateBuffer = getTemplateBuffer();
        
        // The section properties is at the end of body and sets headers, footers, etc.
        val sectionProperties = (CTSectPr) document.getDocument().getBody().getSectPr().copy();

        // Remove template page from final doc
        document.getDocument().setBody(new XWPFDocument().getDocument().getBody());

        // Generate new Pages
        for (int pageIndex = 0; pageIndex < dataSource.size(); pageIndex++) {
            try {
                applyTemplateToPage(pageIndex, templateBuffer);
            } catch (XmlException e) {
                throw new InvalidFormatException("Applying the template resulted in invalid XML.", e);
            }

            // Set a page break in every page, except for the final page
            if(pageIndex < dataSource.size() - 1) {
                document.getParagraphArray(document.getParagraphs().size() - 1).setPageBreak(true);
            }
        }

        // Reapply the section properties
        document.getDocument().getBody().setSectPr(sectionProperties);

        return document;
    }

    private void addImagesToDocMedia() throws InvalidFormatException {
        imageReferences = new ArrayList<>();

        for (T data : dataSource) {
            val image = imageGenerator.apply(data);
            imageReferences.add(document.addPictureData(image, Document.PICTURE_TYPE_PNG));
        }
    }

    private byte[] getTemplateBuffer() throws IOException {
        try (val bufferStream = new ByteArrayOutputStream()) {
            document.write(bufferStream);
            return bufferStream.toByteArray();
        }
    }

    private void applyTemplateToPage(int dataIndex, byte[] templateBuffer) throws IOException, XmlException {
        try (val templateStream = new ByteArrayInputStream(templateBuffer); val template = new XWPFDocument(templateStream);) {
            for (XWPFParagraph paragraph : template.getParagraphs()) {
                val templateXml = paragraph.getCTP().xmlText();
                var paragraphXml = applyTemplateToXml(dataIndex, templateXml);

                // Add paragraph with new XML
                document.createParagraph().getCTP().set(XmlObject.Factory.parse(paragraphXml));
            }
        }
    }

    private String applyTemplateToXml(int dataIndex, String templateXml) {
        val data = dataSource.get(dataIndex);
        val imgIndex = imageReferences.get(dataIndex);

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

    public interface TextTemplate<T> {
        String apply(T data, String templatePlaceholder);
    }
}
