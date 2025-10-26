package com.alphapay.payEngine.alphaServices.historyTransaction.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DailyPaymentMerchantReportPdf {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static byte[] build(String[] msgKeys) throws Exception {
        // Indices must match your email template placeholders
        // 0..8: tx stats (8 is JSON failure reasons)
        // 9..15: merchant kpis
        // 16..19: approval breakdown JSONs (manager, admin, mbme, myfatoorah)
        // 20: recommendations (plain text)
        // 21: min commission, 22: max commission
        Map<String, Integer> failureReasons = parseJsonIntMap(safe(msgKeys, 8));
        Map<String, Integer> manager = parseJsonIntMap(safe(msgKeys, 16));
        Map<String, Integer> admin = parseJsonIntMap(safe(msgKeys, 17));
        Map<String, Integer> mbme = parseJsonIntMap(safe(msgKeys, 18));
        Map<String, Integer> myfatoorah = parseJsonIntMap(safe(msgKeys, 19));

        String recommendations = safe(msgKeys, 20);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(24, 24, 28, 24);

        // Styles (default font)
        Style h1 = new Style().setFontSize(16).setBold();
        Style h2 = new Style().setFontSize(13).setBold().setFontColor(ColorConstants.BLACK);
        Style h3 = new Style().setFontSize(12).setBold();
        Style body = new Style().setFontSize(10);
        Style muted = new Style().setFontSize(9).setFontColor(ColorConstants.GRAY);

        // Header (logo + title)
        Table header = new Table(UnitValue.createPercentArray(new float[]{25f, 75f})).useAllAvailableWidth();
        try {
            Image logo = new Image(ImageDataFactory.create(new URL("https://merchant.alphapay.ae/favicon.png")))
                    .setWidth(60);
            header.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
        } catch (Exception e) {
            header.addCell(new Cell().add(new Paragraph("AlphaPay"))
                    .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
        }
        Cell titleCell = new Cell().add(new Paragraph("Daily Payment & Merchant Report").addStyle(h1))
                .add(new Paragraph("A snapshot of performance in the last 24 hours.").addStyle(muted))
                .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(titleCell);
        doc.add(header);
        doc.add(new Paragraph(currentStamp()).addStyle(muted).setMarginTop(4));

        // KPI grid #1 (8 cards)
        doc.add(new Paragraph("Transaction Overview (Last 24h)").addStyle(h2).setMarginTop(10));
        Table kpi1 = kpiGrid(4);
        addKpi(kpi1, "Total Trans", safe(msgKeys, 0), "info");
        addKpi(kpi1, "Total Amount", safe(msgKeys, 1), "neutral");
        addKpi(kpi1, "Success (count)", safe(msgKeys, 2), "success");
        addKpi(kpi1, "Success (amount)", safe(msgKeys, 3), "success");
        addKpi(kpi1, "Failed (count)", safe(msgKeys, 4), "danger");
        addKpi(kpi1, "Failed (amount)", safe(msgKeys, 5), "danger");
        addKpi(kpi1, "Pending", safe(msgKeys, 6), "warning");
        addKpi(kpi1, "In Progress", safe(msgKeys, 7), "processing");
        doc.add(kpi1);

        // KPI grid #2 (commission)
        Table kpi2 = kpiGrid(2);
        addKpi(kpi2, "Max Commission (AED)", safe(msgKeys, 22), "neutral");
        addKpi(kpi2, "Min Commission (AED)", safe(msgKeys, 21), "neutral");
        doc.add(kpi2);

        // Failure reasons table
        doc.add(new Paragraph("Failure Reasons").addStyle(h3).setMarginTop(8));
        doc.add(mapTable(failureReasons, "Reason", "Count"));

        // Merchant Onboarding section
        doc.add(new Paragraph("Merchant Onboarding Overview (Last 24h)").addStyle(h2).setMarginTop(12));
        Table kpi3 = kpiGrid(3);
        addKpi(kpi3, "New Merchants", safe(msgKeys, 9), "info");
        addKpi(kpi3, "Approved Today", safe(msgKeys, 10), "success");
        addKpi(kpi3, "Rejected Today", safe(msgKeys, 11), "danger");
        addKpi(kpi3, "Last Login (count)", safe(msgKeys, 12), "neutral");
        addKpi(kpi3, "Avg Approval (hrs)", safe(msgKeys, 13), "neutral");
        addKpi(kpi3, "Locked Accounts", safe(msgKeys, 14), "warning");
        addKpi(kpi3, "Disabled Accounts", safe(msgKeys, 15), "danger");
        // balance the grid
        doc.add(kpi3);

        // Two-column approval breakdown
        Table twoCol = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        // Left: Manager + Admin
        Div left = new Div();
        left.add(new Paragraph("Approval Breakdown (Internal)").addStyle(h3));
        left.add(new Paragraph("Manager Approval").addStyle(body).setBold());
        left.add(mapTable(manager, "Status", "Count"));
        left.add(new Paragraph("Admin Approval").addStyle(body).setBold().setMarginTop(6));
        left.add(mapTable(admin, "Status", "Count"));
        // Right: MBME + MyFatoorah
        Div right = new Div();
        right.add(new Paragraph("Approval Breakdown (PSPs)").addStyle(h3));
        right.add(new Paragraph("MBME Approval").addStyle(body).setBold());
        right.add(mapTable(mbme, "Status", "Count"));
        right.add(new Paragraph("MyFatoorah Approval").addStyle(body).setBold().setMarginTop(6));
        right.add(mapTable(myfatoorah, "Status", "Count"));

        twoCol.addCell(new Cell().add(left).setBorder(Border.NO_BORDER));
        twoCol.addCell(new Cell().add(right).setBorder(Border.NO_BORDER));
        doc.add(twoCol);

        // Recommendations (monospace feel with gray box)
        doc.add(new Paragraph("AI Recommendations").addStyle(h2).setMarginTop(12));
        Paragraph pre = new Paragraph(recommendations == null ? "" : recommendations)
                .setFontSize(9)
                .setBackgroundColor(new DeviceRgb(224, 224, 224))
                .setPaddingLeft(8).setPaddingRight(8).setPaddingTop(8).setPaddingBottom(8);
        doc.add(pre);
        doc.add(new Paragraph("If any metric looks unusual, reply to this email.")
                .addStyle(muted).setMarginTop(6));

        doc.close();
        return baos.toByteArray();
    }

    // --- helpers ---
    private static String safe(String[] arr, int idx) {
        return (arr != null && idx >= 0 && idx < arr.length && arr[idx] != null) ? arr[idx] : "";
    }

    private static Map<String, Integer> parseJsonIntMap(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyMap();
            return MAPPER.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static String currentStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // --- status tone helpers ---
    private static DeviceRgb toneBg(String tone) {
        if (tone == null) tone = "info";
        switch (tone.toLowerCase(Locale.ROOT)) {
            case "success": return new DeviceRgb(232, 245, 233);   // #E8F5E9
            case "danger":  return new DeviceRgb(253, 236, 234);   // #FDECEA
            case "warning": return new DeviceRgb(255, 248, 225);   // #FFF8E1
            case "processing": return new DeviceRgb(224, 247, 250); // #E0F7FA
            case "neutral": return new DeviceRgb(245, 245, 245);   // #F5F5F5
            case "info":
            default:         return new DeviceRgb(227, 242, 253);   // #E3F2FD
        }
    }
    private static DeviceRgb toneBorder(String tone) {
        if (tone == null) tone = "info";
        switch (tone.toLowerCase(Locale.ROOT)) {
            case "success": return new DeviceRgb(200, 230, 201);   // #C8E6C9
            case "danger":  return new DeviceRgb(244, 199, 195);   // ~#F4C7C3
            case "warning": return new DeviceRgb(255, 236, 179);   // #FFECB3
            case "processing": return new DeviceRgb(178, 235, 242); // #B2EBF2
            case "neutral": return new DeviceRgb(224, 224, 224);   // #E0E0E0
            case "info":
            default:         return new DeviceRgb(187, 222, 251);   // #BBDEFB
        }
    }

    private static Table kpiGrid(int cols) {
        float[] perc = new float[cols];
        Arrays.fill(perc, 100f / cols);
        Table t = new Table(UnitValue.createPercentArray(perc)).useAllAvailableWidth();
        t.setMarginTop(6);
        return t;
    }

    private static void addKpi(Table grid, String label, String value) {
        addKpi(grid, label, value, "info");
    }

    private static void addKpi(Table grid, String label, String value, String tone) {
        DeviceRgb bg = toneBg(tone);
        DeviceRgb br = toneBorder(tone);
        Div box = new Div()
                .setBackgroundColor(bg)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(br, 0.8f))
                .setBorderRadius(new BorderRadius(6))
                .setPadding(8)
                .setMarginBottom(6);
        box.add(new Paragraph(label).setFontSize(9).setFontColor(ColorConstants.GRAY));
        box.add(new Paragraph(value == null ? "" : value).setFontSize(12).setBold());
        grid.addCell(new Cell().add(box).setBorder(Border.NO_BORDER));
    }

    private static Table mapTable(Map<String, Integer> map, String c1, String c2) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{75f, 25f}))
                .useAllAvailableWidth();
        table.setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(224, 224, 224), 0.75f))
             .setBorderRadius(new BorderRadius(6));
        // header
        table.addHeaderCell(new Cell().add(new Paragraph(c1).setBold()).setBackgroundColor(new DeviceRgb(224, 224, 224)));
        table.addHeaderCell(new Cell().add(new Paragraph(c2).setBold()).setBackgroundColor(new DeviceRgb(224, 224, 224)));
        if (map == null || map.isEmpty()) {
            table.addCell(new Cell(1, 2).add(new Paragraph("{}")).setTextAlignment(TextAlignment.CENTER));
            return table;
        }
        map.forEach((k, v) -> {
            table.addCell(new Cell().add(new Paragraph(k)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(v))));
        });
        return table;
    }
}