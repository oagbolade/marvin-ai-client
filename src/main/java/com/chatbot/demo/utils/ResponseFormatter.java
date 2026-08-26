package com.chatbot.demo.utils;

public class ResponseFormatter {

    private static final String TABLE_STYLE = "style=\"border-collapse:collapse;width:100%;margin:0;\"";
    private static final String TH_STYLE = "style=\"border:1px solid #d0d7de;padding:8px;background-color:#f6f8fa;color:#1f2328;text-align:left;\"";
    private static final String TD_STYLE = "style=\"border:1px solid #d0d7de;padding:8px;color:#1f2328;\"";

    private ResponseFormatter() {
    }

    public static String toHtml(String formattedText) {
        if (formattedText == null || formattedText.isBlank()) {
            return "<p></p>";
        }

        var html = new StringBuilder();
        var tableRows = new java.util.ArrayList<String[]>();

        for (String line : formattedText.split("\n")) {
            if (line.contains("|")) {
                tableRows.add(parseCells(line));
            } else {
                flushTable(html, tableRows);
                if (!line.isBlank()) {
                    html.append("<p>")
                            .append(escapeHtml(line.trim()))
                            .append("</p>");
                }
            }
        }

        flushTable(html, tableRows);

        if (html.length() == 0) {
            return "<p>" + escapeHtml(formattedText.trim()) + "</p>";
        }

        return html.toString();
    }

    private static void flushTable(StringBuilder html, java.util.List<String[]> rows) {
        if (rows.isEmpty()) {
            return;
        }

        html.append("<table ")
                .append(TABLE_STYLE)
                .append(">\n");

        html.append("<thead><tr>");
        boolean includeRowNumbers = rows.size() > 1;
        if (includeRowNumbers) {
            html.append("<th ")
                    .append(TH_STYLE)
                    .append(">#</th>");
        }
        for (String header : rows.get(0)) {
            html.append("<th ")
                    .append(TH_STYLE)
                    .append(">")
                    .append(escapeHtml(header.trim()))
                    .append("</th>");
        }
        html.append("</tr></thead>");

        if (rows.size() > 1) {
            html.append("<tbody>");
            for (int i = 1; i < rows.size(); i++) {
                html.append("<tr>");
                if (includeRowNumbers) {
                    html.append("<td ")
                            .append(TD_STYLE)
                            .append(">")
                            .append(i)
                            .append("</td>");
                }
                for (String cell : rows.get(i)) {
                    html.append("<td ")
                            .append(TD_STYLE)
                            .append(">")
                            .append(escapeHtml(cell.trim()))
                            .append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</tbody>");
        }

        html.append("</table>");
        rows.clear();
    }

    private static String[] parseCells(String line) {
        return java.util.Arrays.stream(line.split("\\|"))
                .map(String::trim)
                .toArray(String[]::new);
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
