package de.hs_mannheim.informatik.ct.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.itextpdf.text.DocumentException;

import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import lombok.val;

@RestController
@RequestMapping("printout")
public class PrintOutController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private DynamicContentService contentService;

    @Autowired
    private Utilities utilities;

    @Value("${server.port}")
    private String port;

    @Value("${hostname}")
    private String host;

    @GetMapping(value = "/rooms")
    public ResponseEntity<StreamingResponseBody> getRoomPrintout(HttpServletRequest request) {
        val outFileName = "QR Codes.pdf";

        StreamingResponseBody responseBody = outputStream -> {
            try {
                contentService.writeRoomsPrintoutPDF(
                        roomService.all(),
                        outputStream,
                        room -> utilities.getUriToLocalPath(
                                RoomController.getRoomCheckinPath(room),
                                request
                        )
                );
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + outFileName)
                .body(responseBody);

    }
}