package net.toreingolf.dbin.ui;

import org.springframework.stereotype.Component;

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

    private StringBuilder page;

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
        page.append(">");
        page.append(CSS);
    }

    public void htmlClose() {
        page.append("</body></html>");
    }

    public void p(String text) {
        page.append(text);
    }

    public void header(String text) {
        page.append("<font face=\"Arial,Helvetica\" size=4 color=#000><b>");
        page.append(text);
        page.append("</b></font><br>");
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
    }

    public void columnHeader(String header) {
        page.append("<td bgcolor=#ccc><a class=\"NAV\">");
        page.append(header);
        page.append("</a></td>");
    }

    public void tableData(String text) {
        page.append("<td>");
        page.append(text);
        page.append("</td>");
    }
}
