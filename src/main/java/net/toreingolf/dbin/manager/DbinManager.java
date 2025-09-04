package net.toreingolf.dbin.manager;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.domain.AllConsColumns;
import net.toreingolf.dbin.domain.AllConsColumnsId;
import net.toreingolf.dbin.domain.AllConstraints;
import net.toreingolf.dbin.domain.AllObjects;
import net.toreingolf.dbin.domain.AllTabColumns;
import net.toreingolf.dbin.domain.AllTabCommentsId;
import net.toreingolf.dbin.persistence.AllConsColumnsRepo;
import net.toreingolf.dbin.persistence.AllConstraintsRepo;
import net.toreingolf.dbin.persistence.AllObjectsRepo;
import net.toreingolf.dbin.persistence.AllTabColumnsRepo;
import net.toreingolf.dbin.persistence.AllTabCommentsRepo;
import net.toreingolf.dbin.ui.DbinUi;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class ConstraintTypeComparator implements Comparator<AllConstraints> {
    private int sortOrder(AllConstraints constraint) {
        return switch (constraint.getConstraintType()) {
            case "P" -> 1;
            case "U" -> 2;
            case "R" -> 3;
            case "C" -> 4;
            default -> throw new IllegalStateException("Unexpected value: " + constraint.getConstraintType());
        };
    }

    @Override
    public int compare(AllConstraints o1, AllConstraints o2) {
        return Integer.compare(sortOrder(o1), sortOrder(o2));
    }
}

@Service
@Slf4j
public class DbinManager {

    private final AllObjectsRepo allObjectsRepo;
    private final AllTabColumnsRepo allTabColumnsRepo;
    private final AllTabCommentsRepo allTabCommentsRepo;
    private final AllConstraintsRepo allConstraintsRepo;
    private final AllConsColumnsRepo allConsColumnsRepo;

    private final DbinUi ui;

    public static Sort OBJECT_SORT = Sort.by(Sort.Direction.ASC, "objectName");
    public static Sort COLUMN_SORT = Sort.by(Sort.Direction.ASC, "columnId");

    public DbinManager(AllObjectsRepo allObjectsRepo,
                       AllTabColumnsRepo allTabColumnsRepo,
                       AllTabCommentsRepo allTabCommentsRepo,
                       AllConstraintsRepo allConstraintsRepo,
                       AllConsColumnsRepo allConsColumnsRepo,
                       DbinUi dbinUi) {
        this.allObjectsRepo = allObjectsRepo;
        this.allTabColumnsRepo = allTabColumnsRepo;
        this.allTabCommentsRepo = allTabCommentsRepo;
        this.allConstraintsRepo = allConstraintsRepo;
        this.allConsColumnsRepo = allConsColumnsRepo;
        this.ui = dbinUi;
    }

    public String getObjects(String owner, String objectType) {

        log.info("get objects of type {} for owner {}", objectType, owner);

        String title = "Objects of type " + objectType + (owner == null ? "" : " owned by " + owner);
        List<AllObjects> objects = allObjectsRepo.findByOwnerAndObjectType(owner, objectType, OBJECT_SORT);

        ui.htmlOpen(title);
        ui.header(title);

        ui.tableOpen(0, 0, 4);
        ui.columnHeaders("Name", "Created", "Updated", "Status", "Comment");
        ui.resetRowCount();
        objects.forEach(o -> {
            ui.tableRowOpen();
            ui.tableData(ui.tabDefLink(owner, o.getObjectName()));
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
        log.info("get tabDef for table {} owned by {}", tableName, owner);

        String title = "Table " + owner + "." + tableName;
        List<AllTabColumns> columns = allTabColumnsRepo.findByOwnerAndTableName(owner, tableName, COLUMN_SORT);

        ui.htmlOpen(title);
        ui.header(ui.tableHeader(owner, tableName, "tabData"));

        var table = allObjectsRepo.findByOwnerAndObjectName(owner, tableName);

        ui.header("Details", 3);

        ui.tableOpen(0, 0, 4);
        ui.detailRow("Created", table.getCreated());
        ui.detailRow("Updated", table.getLastDdlTime());
        ui.detailRow("Rows", "");
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
                            ui.tabDefLink(c.getROwner(), getConstraintTarget(owner, c.getConstraintName()))
                                    + "("
                                    + getConstraintColumns(c.getROwner(), c.getRConstraintName())
                                    + ")"
                    );
                }
            }
            ui.tableRowClose();
        });
        ui.tableClose();
        ui.showRowCount();

        ui.htmlClose();

        return ui.getPage();
    }

    private String getTableComments(String owner, String tableName) {
        log.info("get comments for {}.{}", owner, tableName);
        return allTabCommentsRepo.findById(new AllTabCommentsId(owner, tableName)).getComments();
    }

    private String getConstraintColumns(String owner, String constraintName) {
        log.info("get columns for constraint {}.{}", owner, constraintName);
        return allConsColumnsRepo.findById(new AllConsColumnsId(owner, constraintName))
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
        return allConstraintsRepo.findByOwnerAndConstraintName(owner, target.getRConstraintName()).getTableName();
    }

    private String getNullableDescription(String nullable) {
        return switch (nullable) {
            case "N" -> "NOT NULL";
            case "Y" -> "";
            default -> throw new IllegalStateException("Unexpected value: " + nullable);
        };
    }
}
