package net.toreingolf.dbin.manager;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.domain.AllConsColumns;
import net.toreingolf.dbin.domain.AllConstraints;
import net.toreingolf.dbin.domain.AllObjects;
import net.toreingolf.dbin.domain.AllSource;
import net.toreingolf.dbin.domain.AllTabColumns;
import net.toreingolf.dbin.domain.ConstraintTypeComparator;
import net.toreingolf.dbin.domain.IdTableName;
import net.toreingolf.dbin.domain.IdTableNameColumnName;
import net.toreingolf.dbin.domain.IdViewName;
import net.toreingolf.dbin.persistence.AllColCommentsRepo;
import net.toreingolf.dbin.persistence.AllConsColumnsRepo;
import net.toreingolf.dbin.persistence.AllConstraintsRepo;
import net.toreingolf.dbin.persistence.AllObjectsRepo;
import net.toreingolf.dbin.persistence.AllSourceRepo;
import net.toreingolf.dbin.persistence.AllTabColumnsRepo;
import net.toreingolf.dbin.persistence.AllTabCommentsRepo;
import net.toreingolf.dbin.persistence.AllViewsRepo;
import net.toreingolf.dbin.persistence.DbinRepo;
import net.toreingolf.dbin.ui.DbinUi;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.toreingolf.dbin.ui.DbinUi.METHOD_SOURCE;
import static net.toreingolf.dbin.ui.DbinUi.METHOD_TABDATA;
import static net.toreingolf.dbin.ui.DbinUi.METHOD_TABDEF;

@Service
@Slf4j
public class DbinManager {

    private final AllObjectsRepo allObjectsRepo;
    private final AllTabColumnsRepo allTabColumnsRepo;
    private final AllTabCommentsRepo allTabCommentsRepo;
    private final AllColCommentsRepo allColCommentsRepo;
    private final AllConstraintsRepo allConstraintsRepo;
    private final AllConsColumnsRepo allConsColumnsRepo;
    private final AllViewsRepo allViewsRepo;
    private final AllSourceRepo allSourceRepo;
    private final DbinRepo dbinRepo;

    private final DbinUi ui;

    private static final Sort OBJECT_SORT = Sort.by(Sort.Direction.ASC, "objectName");
    private static final Sort COLUMN_SORT = Sort.by(Sort.Direction.ASC, "columnId");
    private static final Sort LINE_SORT = Sort.by(Sort.Direction.ASC, "line");

    private static final String DATE_TYPE_1 = "DATE";
    private static final String DATE_TYPE_2 = "TIMESTAMP(6)";
    private static final String DATE_TYPE_3 = "TIMESTAMP(6) WITH TIME ZONE";
    private static final List<String> DATE_TYPES = List.of(DATE_TYPE_1, DATE_TYPE_2, DATE_TYPE_3);

    public static final String DATETIME_FORMAT_SQL = "dd.mm.yyyy hh24:mi";

    public DbinManager(AllObjectsRepo allObjectsRepo,
                       AllTabColumnsRepo allTabColumnsRepo,
                       AllTabCommentsRepo allTabCommentsRepo,
                       AllColCommentsRepo allColCommentsRepo,
                       AllConstraintsRepo allConstraintsRepo,
                       AllConsColumnsRepo allConsColumnsRepo,
                       AllViewsRepo allViewsRepo,
                       AllSourceRepo allSourceRepo,
                       DbinRepo dbinRepo,
                       DbinUi dbinUi) {
        this.allObjectsRepo = allObjectsRepo;
        this.allTabColumnsRepo = allTabColumnsRepo;
        this.allTabCommentsRepo = allTabCommentsRepo;
        this.allColCommentsRepo = allColCommentsRepo;
        this.allConstraintsRepo = allConstraintsRepo;
        this.allConsColumnsRepo = allConsColumnsRepo;
        this.allViewsRepo = allViewsRepo;
        this.allSourceRepo = allSourceRepo;
        this.dbinRepo = dbinRepo;
        this.ui = dbinUi;
    }

    public String getObjects(String owner, String objectType) {

        var isTable = "TABLE".equals(objectType);
        var isView = "VIEW".equals(objectType);

        log.info("objects of type {} for owner {}", objectType, owner);

        String title = "Objects of type " + objectType + (owner == null ? "" : " owned by " + owner);
        List<AllObjects> objects = allObjectsRepo.findByOwnerAndObjectType(owner, objectType, OBJECT_SORT);

        ui.htmlOpen(title);
        ui.header(title);

        ui.tableOpen(0, 0, 4);
        ui.tableRowOpen();
        ui.columnHeader("Name");
        if (isTable || isView) {
            ui.columnHeader("Rows");
        }
        ui.columnHeader("Created");
        ui.columnHeader("Updated");
        ui.columnHeader("Status");
        if (isTable) {
            ui.columnHeader("Comment");
        }
        ui.tableRowClose();

        ui.resetRowCount();
        objects.forEach(o -> {
            ui.tableRowOpen();

            if (isView) {
                ui.tableData(ui.viewDefLink(owner, o.getObjectName()));
            } else if (isTable) {
                ui.tableData(ui.tabDefLink(owner, o.getObjectName()));
            } else if ("PACKAGE".equals(objectType)) {
                ui.tableData(
                        ui.anchor(ui.objUrl(owner, o.getObjectName(), METHOD_SOURCE, "packageName")
                                , o.getObjectName()
                        )
                );
            }

            if (isTable || isView) {
                ui.tableData(tabDataLink(owner, o.getObjectName()), "align=right");
            }

            ui.tableData(ui.dateString(o.getCreated()));
            ui.tableData(ui.dateString(o.getLastDdlTime()));
            ui.tableData(o.getStatus());

            if (isTable) {
                ui.tableData(getTableComments(owner, o.getObjectName()));
            }

            ui.tableRowClose();
        });
        ui.tableClose();
        ui.showRowCount();

        objectListLink(owner, "View");
        objectListLink(owner, "Table");
        objectListLink(owner, "Package");
        bottomLink("users", "Users");

        ui.htmlClose();

        return ui.getPage();
    }

    public String getViewDef(String owner, String viewName) {
        log.info("viewDef for view {} owned by {}", viewName, owner);

        ui.htmlOpen("View " + owner + "." + viewName);
        ui.header("View " + ui.ownerLink(owner, "VIEW") + "." + viewName);

        var view = allViewsRepo.findById(new IdViewName(owner, viewName));
        ui.p("<pre>" + view.getText() + "</pre>");

        ui.htmlClose();

        return ui.getPage();
    }

    public String getTabDef(String owner, String tableName) {
        log.info("tabDef for table {} owned by {}", tableName, owner);

        String title = "Table " + owner + "." + tableName;

        ui.htmlOpen(title);
        ui.header(ui.tableHeader(owner, tableName, METHOD_TABDATA));

        var table = allObjectsRepo.findByOwnerAndObjectName(owner, tableName);

        ui.header("Details", 3);

        ui.tableOpen(0, 0, 4);
        ui.detailRow("Created", table.getCreated());
        ui.detailRow("Updated", table.getLastDdlTime());
        ui.detailRow("Rows", tabDataLink(owner, tableName));
        ui.detailRow("Comment", getTableComments(owner, tableName));
        ui.tableClose();

        List<AllTabColumns> columns = allTabColumnsRepo.findByOwnerAndTableName(owner, tableName, COLUMN_SORT);

        ui.header("Columns", 3);
        ui.tableOpen(0, 0, 4);
        ui.columnHeaders("Column Name", "Null?", "Data Type", "Length", "Default", "Search Criteria", "Comment");

        ui.formOpen(METHOD_TABDATA);

        columns.forEach(c -> {
            ui.tableRowOpen();
            ui.tableData(c.getColumnName());
            ui.tableData(getNullableDescription(c.getNullable()));
            ui.tableData(c.getDataType());
            ui.tableData(c.getDataLength());
            ui.tableData(c.getDataDefault());
            ui.tableData(
                    ui.formHidden("fieldName", c.getColumnName())
                            + ui.formText("fieldValue", 30, 1000)
            );
            ui.tableData(getTableColumnComments(owner, tableName, c.getColumnName()));
            ui.tableRowClose();
        });

        ui.tableRowOpen();
        ui.p(
                ui.formHidden("owner", owner)
                        + ui.formHidden("tableName", tableName)
        );
        ui.tableData(ui.formSubmit("action", "Query"), " colspan=6 align=\"right\"");
        ui.tableRowClose();
        ui.formClose();

        ui.tableClose();
        ui.showRowCount();

        var constraints = allConstraintsRepo.findByOwnerAndTableName(owner, tableName);
        constraints.sort(new ConstraintTypeComparator());

        ui.header("Constraints", 3);

        ui.tableOpen(0, 0, 4);
        ui.columnHeaders("Constraint Name", "Constraint Type", "Column(s) / Check", "Target");
        constraints.forEach(c -> {
            ui.tableRowOpen();
            ui.tableData(c.getConstraintName());
            ui.tableData(getConstraintTypeDescription(c.getConstraintType()));
            if ("C".equals(c.getConstraintType())) {
                ui.tableData(c.getSearchCondition());
            } else {
                ui.tableData(getConstraintColumns(owner, c.getConstraintName()));
                if ("R".equals(c.getConstraintType())) {
                    ui.tableData(
                            ui.tabDefLink(c.getTargetOwner(), getConstraintTarget(owner, c.getConstraintName()))
                                    + "("
                                    + getConstraintColumns(c.getTargetOwner(), c.getTargetConstraintName())
                                    + ")"
                    );
                }
            }
            ui.tableRowClose();
        });

        ui.tableClose();
        ui.showRowCount();

        showReferrers(owner, getPrimaryKeyName(owner, tableName), null);

        ui.htmlClose();

        return ui.getPage();
    }

    public String getTabData(String owner, String tableName, List<String> fieldName, List<String> fieldValue) {
        log.info("tabData for table {} owned by {}", tableName, owner);

        String title = "Table " + owner + "." + tableName;

        ui.htmlOpen(title);
        ui.header(ui.tableHeader(owner, tableName, METHOD_TABDEF) + "<br>");

        ui.tableOpen(1, 0, 2);

        var pkName = getPrimaryKeyName(owner, tableName);
        var pkColumn = getConstraintColumns(owner, pkName);
        var columns = allTabColumnsRepo.findByOwnerAndTableName(owner, tableName, COLUMN_SORT);

        StringBuilder sql = new StringBuilder("select ");
        List<String> columnNames = new ArrayList<>();

        ui.resetColumnIndex();
        columns.forEach(c -> {
            columnNames.add(c.getColumnName());
            if (ui.getColumnIndex() > 0) {
                sql.append(", ");
            }
            if (DATE_TYPES.contains(c.getDataType())) {
                sql.append("to_char(\"")
                        .append(c.getColumnName())
                        .append("\", '")
                        .append(DATETIME_FORMAT_SQL)
                        .append("') as \"")
                        .append(c.getColumnName())
                        .append("\"");
            } else {
                sql.append("\"").append(c.getColumnName()).append("\"");
            }
            ui.increaseColumnIndex();
        });

        sql.append(" from ").append(owner).append(".\"").append(tableName).append("\"");

        ui.resetColumnIndex();
        ui.setPkValue(null);

        if (fieldName != null) {
            AtomicInteger conditions = new AtomicInteger();
            fieldName.forEach(name -> {
                var value = fieldValue.get(ui.getColumnIndex());
                log.info("condition: field {} with value {}", name, value);
                if (value != null && !value.isEmpty()) {
                    if (conditions.get() == 0) {
                        sql.append(" where ");
                    } else {
                        sql.append(" and ");
                    }
                    conditions.getAndIncrement();
                    sql.append("\"").append(name).append("\"");

                    if (value.endsWith("%")) {
                        sql.append(" like '").append(value).append("'");
                    } else {
                        sql.append(" = '").append(value).append("'");
                    }

                    var i = columnNames.indexOf(name);
                    if (i > -1) {
                        columnNames.set(i, columnNames.get(i) + (value.endsWith("%") ? " like " : "=") + value);
                    }

                    if (name.equals(pkColumn)) {
                        ui.setPkValue(value);
                    }
                }
                ui.increaseColumnIndex();
            });
        }
        sql.append(" order by 1");

        var data = dbinRepo.getData(columns, sql.toString());
        log.info("data: {}", data);

        ui.tableRowOpen();
        columnNames.forEach(ui::columnHeader);
        ui.tableRowClose();

        var foreignKeys = getForeignKeys(owner, tableName);

        ui.resetRowCount();
        data.forEach(row -> {
            ui.tableRowOpen();
            ui.resetColumnIndex();
            row.forEach(column -> {
                var columnDef = columns.get(ui.getColumnIndex());
                String value;
                if (DATE_TYPES.contains(columnDef.getDataType()) || "CLOB".equals(columnDef.getDataType())) {
                    value = column;
                } else if (column == null) {
                    value = null;
                } else {
                    var fkCandidate = foreignKeys
                            .stream()
                            .filter(c -> getConstraintColumns(owner, c.getConstraintName()).equals(columnDef.getColumnName()))
                            .findFirst();
                    if (fkCandidate.isPresent()) {
                        var fk = fkCandidate.get();
                        value = tabDataFilterLink(
                                fk.getTargetOwner()
                                , getConstraintTarget(owner, fk.getConstraintName())
                                , getConstraintColumns(fk.getTargetOwner(), fk.getTargetConstraintName())
                                , column
                        );
                    } else {
                        value = tabDataFilterLink(owner, tableName, columnDef.getColumnName(), column);
                    }
                }
                ui.tableData(value);
                ui.increaseColumnIndex();
            });
            ui.tableRowClose();
        });

        ui.tableClose();
        ui.showRowCount();

        var rows = getTableSize(owner, tableName);
        if (ui.getRowCount() < rows) {
            bottomLink(ui.tabDataUrl(owner, tableName), "Show all (" + rows + ")");
        }

        showReferrers(owner, pkName, ui.getPkValue());

        ui.htmlClose();

        return ui.getPage();
    }

    public String getSource(String owner, String packageName, String part, String mode) {

        String bodyDesc;
        String bodyClass;
        String headerDesc;
        String headerClass;

        String sourceType;

        String formattedDesc;
        String formattedClass;
        String unformattedDesc;
        String unformattedClass;

        boolean unformatted = "U".equals(mode);

        if ("H".equals(part)) { // show package header

            bodyDesc = "body";
            bodyClass = "NAV";
            headerDesc = "HEADER";
            headerClass = "HI";

            sourceType = "PACKAGE";

        } else { // show package body

            bodyDesc = "BODY";
            bodyClass = "HI";
            headerDesc = "header";
            headerClass = "NAV";

            sourceType = "PACKAGE BODY";
        }

        if (unformatted) { // show source code unformatted

            formattedDesc = "formatted";
            formattedClass = "NAV";
            unformattedDesc = "UNFORMATTED";
            unformattedClass = "HI";

        } else { // format the output

            formattedDesc = "FORMATTED";
            formattedClass = "HI";
            unformattedDesc = "unformatted";
            unformattedClass = "NAV";
        }

        var url = "source"
                + ui.addParameter("owner", owner, "?")
                + ui.addParameter("packageName", packageName);

        String title = "Package " + owner + "." + packageName;

        ui.htmlOpen(title);

        ui.p(
                ui.headerText(
                        "Package "
                                + ui.ownerLink(owner, "PACKAGE")
                                + "."
                                + packageName
                        , 4
                )
                        + ui.navText(" ( ")
                        + sourceLink(url, "B", mode, bodyDesc, bodyClass)
                        + ui.navText(" / ")
                        + sourceLink(url, "H", mode, headerDesc, headerClass)
                        + ui.navText(" , ")
                        + sourceLink(url, part, "F", formattedDesc, formattedClass)
                        + ui.navText(" / ")
                        + sourceLink(url, part, "U", unformattedDesc, unformattedClass)
                        + ui.navText(" )")
                        + "<br><p>"
        );

        List<AllSource> source = allSourceRepo.findByOwnerAndNameAndType(owner, packageName, sourceType, LINE_SORT);

        if (unformatted) {
            ui.p("<pre>");
        }

        source.forEach(s -> {
            if (unformatted) {
                ui.p(s.getText());
            } else {
                ui.p(
                        ui.plainText(String.format("%05d", s.getLine())
                                + " &nbsp; "
                                + s.getText()
                                .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                + "<br>"
                        )
                );
            }
        });

        if (unformatted) {
            ui.p("</pre>");
        }

        return ui.getPage();
    }

    public String users() {
        String title = "Users";

        ui.htmlOpen(title);
        ui.header(title);

        var users = dbinRepo.getUsers();

        users.forEach(user -> ui.p(
                ui.anchor(
                        "objects" + ui.addParameter("owner", user, "?")
                        , user
                        , " class=\"U\""
                )
                        + "<br>"
        ));

        ui.showRowCount(users.size());

        ui.htmlClose();

        return ui.getPage();
    }

    private void showReferrers(String owner, String pkName, String pkValue) {
        ui.resetRowCount();
        var constraints = allConstraintsRepo.findByOwnerAndTargetConstraintNameAndConstraintType(owner, pkName, "R");
        constraints.forEach(c -> {

            if (ui.getRowCount() == 0) {
                ui.header("Referrers", 3);

                if (pkValue != null) {
                    ui.tableOpen(1, 0, 2);
                } else {
                    ui.tableOpen(0, 0, 4);
                }

                ui.tableRowOpen();
                ui.columnHeader("Table name");
                ui.columnHeader("Constraint name");
                ui.columnHeader("Column name");
                if (pkValue != null) {
                    ui.columnHeader("Column value");
                }
                ui.tableRowClose();
            }

            var columns = getConstraintColumns(owner, c.getConstraintName());

            ui.tableRowOpen();
            ui.tableData(tabDefLink(owner, c.getTableName()));
            ui.tableData(c.getConstraintName());
            ui.tableData(columns);
            if (pkValue != null) {
                ui.tableData(tabDataFilterLink(owner, c.getTableName(), columns, pkValue));
            }
            ui.tableRowClose();
        });

        if (ui.getRowCount() > 0) {
            ui.tableClose();
            ui.showRowCount();
        }
    }

    private String getPrimaryKeyName(String owner, String tableName) {
        var pk = allConstraintsRepo.findByOwnerAndTableNameAndConstraintType(owner, tableName, "P");
        return pk != null ? pk.getConstraintName() : "";
    }

    private List<AllConstraints> getForeignKeys(String owner, String tableName) {
        return allConstraintsRepo.findByOwnerAndTableName(owner, tableName)
                .stream()
                .filter(c -> "R".equals(c.getConstraintType()))
                .toList();
    }

    private String getTableComments(String owner, String tableName) {
        log.info("get comments for {}.{}", owner, tableName);
        return allTabCommentsRepo.findById(new IdTableName(owner, tableName)).getComments();
    }

    private String getTableColumnComments(String owner, String tableName, String columnName) {
        return allColCommentsRepo.findById(new IdTableNameColumnName(owner, tableName, columnName)).getComments();
    }

    private String getConstraintColumns(String owner, String constraintName) {
        log.info("get columns for constraint {}.{}", owner, constraintName);
        return allConsColumnsRepo.findByOwnerAndConstraintName(owner, constraintName)
                .stream()
                .map(AllConsColumns::getColumnName)
                .collect(Collectors.joining(", "));
    }

    private String getConstraintTypeDescription(String constraintType) {
        return switch (constraintType) {
            case "P" -> "Primary Key";
            case "U" -> "Unique Key";
            case "R" -> "Foreign Key";
            case "C" -> "Check";
            default -> throw new IllegalStateException("Unexpected value: " + constraintType);
        };
    }

    private String getConstraintTarget(String owner, String constraintName) {
        log.info("find target for constraint {}.{}", owner, constraintName);
        var target = allConstraintsRepo.findByOwnerAndConstraintName(owner, constraintName);
        log.info("target: {}", target);
        return allConstraintsRepo.findByOwnerAndConstraintName(owner, target.getTargetConstraintName()).getTableName();
    }

    private String getNullableDescription(String nullable) {
        return switch (nullable) {
            case "N" -> "NOT NULL";
            case "Y" -> "";
            default -> throw new IllegalStateException("Unexpected value: " + nullable);
        };
    }

    private String tabDefLink(String owner, String tableName) {
        return ui.anchor(ui.tabDefUrl(owner, tableName), tableName);
    }

    private String tabDataLink(String owner, String tableName) {
        return ui.anchor(ui.tabDataUrl(owner, tableName), getTableSize(owner, tableName));
    }

    private String tabDataFilterLink(String owner, String tableName, String fieldName, String value) {
        return ui.anchor(
                ui.tabDataUrl(owner, tableName)
                        + ui.addParameter("fieldName", fieldName)
                        + ui.addParameter("fieldValue", value)
                , value
        );
    }

    private Long getTableSize(String owner, String tableName) {
        return dbinRepo.getTableSize(owner, tableName);
    }

    private void bottomLink(String url, String text) {
        ui.p(ui.anchor(url, text, " class=\"U\"") + " &nbsp; ");
    }

    private void objectListLink(String owner, String objectType) {
        bottomLink(
                "objects"
                        + ui.addParameter("owner", owner, "?")
                        + ui.addParameter("objectType", objectType.toUpperCase())
                , objectType + "s"
        );
    }

    private String sourceLink(String url, String part, String mode, String text, String cssClass) {
        return ui.anchor(
                url
                        + ui.addParameter("part", part)
                        + ui.addParameter("mode", mode)
                , text
                , " class=\"" + cssClass + "\""
        );
    }
}
