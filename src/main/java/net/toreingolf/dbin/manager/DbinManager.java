package net.toreingolf.dbin.manager;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.domain.AllConsColumns;
import net.toreingolf.dbin.domain.AllObjects;
import net.toreingolf.dbin.domain.AllTabColumns;
import net.toreingolf.dbin.domain.ConstraintTypeComparator;
import net.toreingolf.dbin.domain.IdConstraintName;
import net.toreingolf.dbin.domain.IdTableName;
import net.toreingolf.dbin.domain.IdTableNameColumnName;
import net.toreingolf.dbin.persistence.AllColCommentsRepo;
import net.toreingolf.dbin.persistence.AllConsColumnsRepo;
import net.toreingolf.dbin.persistence.AllConstraintsRepo;
import net.toreingolf.dbin.persistence.AllObjectsRepo;
import net.toreingolf.dbin.persistence.AllTabColumnsRepo;
import net.toreingolf.dbin.persistence.AllTabCommentsRepo;
import net.toreingolf.dbin.persistence.DbinRepo;
import net.toreingolf.dbin.ui.DbinUi;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DbinManager {

    private final AllObjectsRepo allObjectsRepo;
    private final AllTabColumnsRepo allTabColumnsRepo;
    private final AllTabCommentsRepo allTabCommentsRepo;
    private final AllColCommentsRepo allColCommentsRepo;
    private final AllConstraintsRepo allConstraintsRepo;
    private final AllConsColumnsRepo allConsColumnsRepo;
    private final DbinRepo dbinRepo;

    private final DbinUi ui;

    public static Sort OBJECT_SORT = Sort.by(Sort.Direction.ASC, "objectName");
    public static Sort COLUMN_SORT = Sort.by(Sort.Direction.ASC, "columnId");

    public DbinManager(AllObjectsRepo allObjectsRepo,
                       AllTabColumnsRepo allTabColumnsRepo,
                       AllTabCommentsRepo allTabCommentsRepo,
                       AllColCommentsRepo allColCommentsRepo,
                       AllConstraintsRepo allConstraintsRepo,
                       AllConsColumnsRepo allConsColumnsRepo,
                       DbinRepo dbinRepo,
                       DbinUi dbinUi) {
        this.allObjectsRepo = allObjectsRepo;
        this.allTabColumnsRepo = allTabColumnsRepo;
        this.allTabCommentsRepo = allTabCommentsRepo;
        this.allColCommentsRepo = allColCommentsRepo;
        this.allConstraintsRepo = allConstraintsRepo;
        this.allConsColumnsRepo = allConsColumnsRepo;
        this.dbinRepo = dbinRepo;
        this.ui = dbinUi;
    }

    public String getObjects(String owner, String objectType) {

        log.info("objects of type {} for owner {}", objectType, owner);

        String title = "Objects of type " + objectType + (owner == null ? "" : " owned by " + owner);
        List<AllObjects> objects = allObjectsRepo.findByOwnerAndObjectType(owner, objectType, OBJECT_SORT);

        ui.htmlOpen(title);
        ui.header(title);

        ui.tableOpen(0, 0, 4);
        ui.columnHeaders("Name", "Rows", "Created", "Updated", "Status", "Comment");
        ui.resetRowCount();
        objects.forEach(o -> {
            ui.tableRowOpen();
            ui.tableData(ui.tabDefLink(owner, o.getObjectName()));
            ui.tableData(tabDataLink(owner, o.getObjectName()), "align=right");
            ui.tableData(ui.dateString(o.getCreated()));
            ui.tableData(ui.dateString(o.getLastDdlTime()));
            ui.tableData(o.getStatus());
            ui.tableData(getTableComments(owner, o.getObjectName()));
            ui.tableRowClose();
        });
        ui.tableClose();
        ui.showRowCount();

        ui.htmlClose();

        return ui.getPage();
    }

    public String getTabDef(String owner, String tableName) {
        log.info("tabDef for table {} owned by {}", tableName, owner);

        String title = "Table " + owner + "." + tableName;
        List<AllTabColumns> columns = allTabColumnsRepo.findByOwnerAndTableName(owner, tableName, COLUMN_SORT);

        ui.htmlOpen(title);
        ui.header(ui.tableHeader(owner, tableName, DbinUi.METHOD_TABDATA));

        var table = allObjectsRepo.findByOwnerAndObjectName(owner, tableName);

        ui.header("Details", 3);

        ui.tableOpen(0, 0, 4);
        ui.detailRow("Created", table.getCreated());
        ui.detailRow("Updated", table.getLastDdlTime());
        ui.detailRow("Rows", tabDataLink(owner, tableName));
        ui.detailRow("Comment", getTableComments(owner, tableName));
        ui.tableClose();

        ui.header("Columns", 3);
        ui.tableOpen(0, 0, 4);
        ui.columnHeaders("Column Name", "Null?", "Data Type", "Length", "Default", "Search Criteria", "Comments");
        ui.resetRowCount();
        columns.forEach(c -> {
            ui.tableRowOpen();
            ui.tableData(c.getColumnName());
            ui.tableData(getNullableDescription(c.getNullable()));
            ui.tableData(c.getDataType());
            ui.tableData(c.getDataLength());
            ui.tableData(c.getDataDefault());
            ui.tableData("");
            ui.tableData(getTableColumnComments(owner, tableName, c.getColumnName()));
            ui.tableRowClose();
        });
        ui.tableClose();
        ui.showRowCount();

        var constraints = allConstraintsRepo.findByOwnerAndTableName(owner, tableName);
        constraints.sort(new ConstraintTypeComparator());

        ui.header("Constraints", 3);

        ui.tableOpen(0, 0, 4);
        ui.columnHeaders("Constraint Name", "Constraint Type", "Column(s) / Check", "Target");
        ui.resetRowCount();
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

        showReferers(owner, getPrimaryKeyName(owner, tableName), null);

        ui.htmlClose();

        return ui.getPage();
    }

    private void showReferers(String owner, String pkName, String pkValue) {
        ui.resetRowCount();
        var constraints = allConstraintsRepo.findByOwnerAndTargetConstraintNameAndConstraintType(owner, pkName, "R");
        constraints.forEach(c -> {
            if (ui.getRowCount() == 0) {
                ui.header("Referers", 3);
                if (pkValue != null) {
                    ui.tableOpen(1, 0, 2);
                } else {
                    ui.tableOpen(0, 0, 4);
                }
                ui.columnHeaders("Table name", "Constraint name", "Column name");
            }

            var columns = getConstraintColumns(owner, c.getConstraintName());

            ui.tableRowOpen();
            ui.tableData(tabDefLink(owner, c.getTableName()));
            ui.tableData(c.getConstraintName());
            ui.tableData(columns);
            ui.tableRowClose();
        });

        if (ui.getRowCount() > 0) {
            ui.tableClose();
            ui.showRowCount();
        }
    }

    public String getTabData(String owner, String tableName) {
        log.info("tabData for table {} owned by {}", tableName, owner);

        String title = "Table " + owner + "." + tableName;

        ui.htmlOpen(title);
        ui.header(ui.tableHeader(owner, tableName, DbinUi.METHOD_TABDEF));

        showReferers(owner, null, null);

        ui.htmlClose();

        return ui.getPage();
    }

    private String getPrimaryKeyName(String owner, String tableName) {
        var pk = allConstraintsRepo.findByOwnerAndTableNameAndConstraintType(owner, tableName, "P");
        return pk != null ? pk.getConstraintName() : "";
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
        return allConsColumnsRepo.findById(new IdConstraintName(owner, constraintName))
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

    private Long getTableSize(String owner, String tableName) {
        return dbinRepo.getTableSize(owner, tableName);
    }

    private String getTableSizeFormatted(long size) {
        return size + " row" + (size > 0 ? "s" : "");
    }
}
