package net.toreingolf.dbin.ui;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Component
public class DbinUi {

    private static final String CSS = """
<style type="text/css">
<!--
A.N { text-decoration:none; font-family:Arial; font-weight:Normal; font-size:10pt; color:#000; }
A.U { text-decoration:underline; font-family:Arial; font-weight:Normal; font-size:10pt; color:#000; }
A.NAV { text-decoration:none; font-family:Arial; font-weight:Bold; font-size:10pt; color:#000; }
A.TAB { text-decoration:none; font-family:Arial; font-weight:Bold; font-size:10pt; color:#000; }
A.HI { text-decoration:none; font-family:Arial; font-weight:Bold; font-size:10pt; color:#C00; }
A.NEW { text-decoration:none; font-family:Arial; font-weight:Bold; font-size:10pt; color:#060; }
A.DIM { text-decoration:none; font-family:Arial; font-weight:Bold; font-size:10pt; color:#999; }
-->
</style>
""";

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private StringBuilder page;
    private int rowCount = 0;

    public String getPage() {
        return page.toString();
    }

    public void htmlOpen(String title, String bgColor, String linkColor) {
        page = new StringBuilder();
        page.append("<html><head><title>");
        page.append(title);
        page.append("</title></head><body bgcolor=#");
        page.append(bgColor);
        page.append(" text=#000");
        page.append(" link=#");
        page.append(linkColor);
        page.append(" vlink=#000>");
        page.append(CSS);
    }

    public void htmlOpen(String title) {
        htmlOpen(title, "fff", "000");
    }

    public void htmlClose() {
        page.append("</body></html>");
    }

    public void p(String text) {
        page.append(text);
    }

    public void header(String text, int size) {
        page.append("<p><font face=\"Arial,Helvetica\" size=");
        page.append(size);
        page.append(" color=#000><b>");
        page.append(text);
        page.append("</b></font><br>");
    }

    public void header(String text) {
        header(text, 4);
    }

    public void tableOpen(int border, int cellSpacing, int cellPadding) {
        page.append("<table border=");
        page.append(border);
        page.append(" cellspacing=");
        page.append(cellSpacing);
        page.append(" cellpadding=");
        page.append(cellPadding);
        page.append(">");
    }

    public void tableClose() {
        page.append("</table>");
    }

    public void tableRowOpen() {
        page.append("<tr>");
    }

    public void tableRowClose() {
        page.append("</tr>");
        rowCount++;
    }

    public void columnHeader(String header) {
        page.append("<td bgcolor=#ccc><a class=\"NAV\">");
        page.append(header);
        page.append("</a></td>");
    }

    public void columnHeaders(String... headers) {
        Arrays.stream(headers).forEach(this::columnHeader);
    }

    public void tableData(String text) {
        page.append("<td>");
        page.append(text == null ? "" : text);
        page.append("</td>");
    }

    public void detailRow(String prompt, String value) {
        tableRowOpen();
        tableData(prompt + ":");
        tableData(value);
        tableRowClose();
    }

    public void detailRow(String prompt, Date date) {
        detailRow(prompt, FORMATTER.format(date));
    }

    public void resetRowCount() {
        rowCount = 0;
    }

    public void showRowCount() {
        if (rowCount > 0) {
            page.append("<p>");
            page.append(plainText("Rows: " + rowCount));
            page.append("&nbsp;<p>");
        }
    }

    public String tableHeader(String owner, String tableName, String method) {
        return "Table <a href=\"objects"
                + addParameter("owner", owner, "?")
                + addParameter("objectType", "TABLE")
                + "\">" + owner + "</a>."
                + tabLink(owner, tableName, method);
    }

    public String plainText(String text) {
        return "<a class=\"N\">" + text + "</a>";
    }

    public String dateString(Date date) {
        return FORMATTER.format(date);
    }

    public String addParameter(String name, String value, String divider) {
        return divider + name + "=" + value;
    }

    public String addParameter(String name, String value) {
        return addParameter(name, value, "&");
    }

    public String objLink(String owner, String objectName, String method, String parameterName) {
        return "<a href=\""
                + method
                + addParameter("owner", owner, "?")
                + addParameter(parameterName, objectName) + "\">"
                + objectName
                + "</a>";
    }

    public String tabLink(String owner, String tableName, String method) {
        return objLink(owner, tableName, method, "tableName");
    }

    public String tabDefLink(String owner, String tableName) {
        return tabLink(owner, tableName, "tabDef");
    }

    public String tabDataLink(String owner, String tableName) {
        return tabLink(owner, tableName, "tabData");
    }
}
