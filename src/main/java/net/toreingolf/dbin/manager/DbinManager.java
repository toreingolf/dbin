package net.toreingolf.dbin.manager;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.domain.AllObjects;
import net.toreingolf.dbin.persistence.AllObjectsRepo;
import net.toreingolf.dbin.ui.DbinUi;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DbinManager {

    private final AllObjectsRepo allObjectsRepo;
    private final DbinUi ui;

    public static Sort OBJECT_SORT = Sort.by(Sort.Direction.ASC, "objectName");

    public DbinManager(AllObjectsRepo allObjectsRepo, DbinUi dbinUi) {
        this.allObjectsRepo = allObjectsRepo;
        this.ui = dbinUi;
    }

    public String getObjects(String owner, String objectType) {

        log.info("get objects of type {} for owner {}", objectType, owner);

        String title = "Objects of type " + objectType + (owner == null ? "" : " owned by " + owner);
        List<AllObjects> objects = allObjectsRepo.findByOwnerAndObjectType(owner, objectType, OBJECT_SORT);

        ui.htmlOpen(title, "fff", "000");
        ui.header(title);

        ui.tableOpen(0, 0, 4);
        ui.tableRowOpen();
        ui.columnHeader("Name");
        ui.columnHeader("Created");
        ui.columnHeader("Updated");
        ui.columnHeader("Status");
        ui.tableRowClose();

        objects.forEach(o -> {
            ui.tableRowOpen();
            ui.tableData(o.getObjectName());
            ui.tableData(o.getCreated().toString());
            ui.tableData(o.getLastDdlTime().toString());
            ui.tableData(o.getStatus());
            ui.tableRowClose();
        });

        ui.tableClose();
        ui.htmlClose();

        return ui.getPage();
    }
}
