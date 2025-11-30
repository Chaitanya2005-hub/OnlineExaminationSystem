package com.stark.exam.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.stark.exam.model.User;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfService {

    public static void generateAdmitCard(User user, String filePath) throws Exception {
        // 1. Initialize Document
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // 2. Add Header (University Name)
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.BLUE);
        Paragraph title = new Paragraph("STARK UNIVERSITY", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // 3. Sub-Header
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
        Paragraph subTitle = new Paragraph("OFFICIAL ADMIT CARD - 2025", subTitleFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle);

        document.add(new Paragraph("\n")); // Spacer
        document.add(new Paragraph("______________________________________________________________________________"));
        document.add(new Paragraph("\n"));

        // 4. Student Details Table
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);
        document.add(new Paragraph("Name:        " + user.getFullName(), bodyFont));
        document.add(new Paragraph("Roll No:     " + user.getErpId(), bodyFont));
        document.add(new Paragraph("Department:  " + (user.getDepartment() != null ? user.getDepartment() : "N/A"), bodyFont));
        document.add(new Paragraph("Generated:   " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()), bodyFont));

        document.add(new Paragraph("\n\n"));

        // 5. Instructions Box
        Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED);
        document.add(new Paragraph("IMPORTANT INSTRUCTIONS:", warningFont));

        com.itextpdf.text.List list = new com.itextpdf.text.List(com.itextpdf.text.List.ORDERED);
        list.add(new ListItem("This Admit Card is mandatory for entry."));
        list.add(new ListItem("Report to the exam hall 30 minutes prior."));
        list.add(new ListItem("Electronic gadgets are strictly prohibited."));
        document.add(list);

        // 6. Footer
        document.add(new Paragraph("\n\n\n\n\n\n"));
        Paragraph footer = new Paragraph("(Controller of Examinations)", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        // 7. Close
        document.close();
    }
}
