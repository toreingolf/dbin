package net.toreingolf.dbin.ui;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Component
public class DbinUi {

    public static final String METHOD_OBJECTS = "objects";
    public static final String METHOD_TABDEF = "tabDef";
    public static final String METHOD_TABDATA = "tabData";
    public static final String METHOD_USERS = "users";

    public static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm";

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

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(DATETIME_FORMAT);

    private StringBuilder page;

    @Getter
    private long rowCount = 0;

    @Getter
    private int columnIndex = 0;

    @Getter
    @Setter
    private String pkValue = null;


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
        resetRowCount();
    }

    public void tableClose() {
        page.append("</table>");
    }

    public void tableRowOpen() {
        page.append("<tr>");
    }

    public void tableRowClose() {
        page.append("</tr>");
        increaseRowCount();
    }

    public void columnHeader(String header) {
        page.append("<td bgcolor=#ccc><a class=\"NAV\">");
        page.append(header);
        page.append("</a></td>");
    }

    public void columnHeaders(String... headers) {
        Arrays.stream(headers).forEach(this::columnHeader);
    }

    public void tableData(String text, String attr) {
        page.append("<td valign=top");
        if (attr != null) {
            page.append(" ").append(attr);
        }
        page.append(">").append(text == null ? "" : text).append("</td>");
    }

    public void tableData(String text) {
        tableData(text, null);
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

    public void increaseRowCount() {
        rowCount++;
    }

    public void resetRowCount() {
        rowCount = 0;
    }

    public void showRowCount() {
        if (rowCount > 0) {
            page.append("<p>").append(plainText("Rows: " + rowCount)).append("&nbsp;<p>");
        }
    }

    public void showRowCount(long value) {
        rowCount = value;
        showRowCount();
    }

    public void increaseColumnIndex() {
        columnIndex++;
    }

    public void resetColumnIndex() {
        columnIndex = 0;
    }

    public String tableHeader(String owner, String tableName, String method) {
        return "TABLE "
                + anchor(METHOD_OBJECTS
                        + addParameter("owner", owner, "?")
                        + addParameter("objectType", "TABLE")
                , owner
                )
                + "."
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

    public String anchor(String url, String text, String attr) {
        return "<a href=\"" + url + "\"" + (attr == null ? "" : attr) + ">" + text + "</a>";

    }

    public String anchor(String url, String text) {
        return anchor(url, text, null);
    }

    public String anchor(String url, Long number) {
        return anchor(url, String.valueOf(number));
    }

    public void formOpen(String action) {
        page.append("<form action=\"").append(action).append("\" method=\"get\">");
    }

    public void formClose() {
        page.append("</form>");
    }

    public String formHidden(String fieldName, String fieldValue) {
        return "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + fieldValue + "\"/>";
    }

    public String formText(String fieldName, String fieldValue, int size, int maxLength) {
        return "<input type=\"text\" name=\"" + fieldName + "\" size=\"" + size + "\" maxlength=\"" + maxLength + "\"/>";
    }

    public String formSubmit(String name, String value) {
        return "<input type=\"submit\" name=\"" + name + "\" value=\"" + value + "\"/>";
    }

    public String objUrl(String owner, String objectName, String method, String parameterName) {
        return method
                + addParameter("owner", owner, "?")
                + addParameter(parameterName, objectName);
    }

    public String tabUrl(String owner, String tableName, String method) {
        return objUrl(owner, tableName, method, "tableName");
    }

    public String tabDefUrl(String owner, String tableName) {
        return tabUrl(owner, tableName, METHOD_TABDEF);
    }

    public String tabLink(String owner, String tableName, String method) {
        return anchor(tabUrl(owner, tableName, method), tableName);
    }

    public String tabDataUrl(String owner, String tableName) {
        return tabUrl(owner, tableName, METHOD_TABDATA);
    }

    public String tabDefLink(String owner, String tableName) {
        return anchor(tabDefUrl(owner, tableName), tableName);
    }
}
