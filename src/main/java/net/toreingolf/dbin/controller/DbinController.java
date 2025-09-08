package net.toreingolf.dbin.controller;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.manager.DbinManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static net.toreingolf.dbin.ui.DbinUi.METHOD_OBJECTS;
import static net.toreingolf.dbin.ui.DbinUi.METHOD_TABDATA;
import static net.toreingolf.dbin.ui.DbinUi.METHOD_TABDEF;
import static net.toreingolf.dbin.ui.DbinUi.METHOD_USERS;

@Controller()
@Slf4j
public class DbinController {

    public static final String DEFAULT_USER = "SCOTT";
    public static final String DEFAULT_OBJECT_TYPE = "TABLE";

    private final DbinManager dbinManager;

    public DbinController(DbinManager dbinManager) {
        this.dbinManager = dbinManager;
    }

    @GetMapping("/")
    public @ResponseBody String root() {
        log.info("root");
        return dbinManager.getObjects(DEFAULT_USER, DEFAULT_OBJECT_TYPE);
    }

    @GetMapping(METHOD_OBJECTS)
    public @ResponseBody String getObjects(
            @RequestParam(name = "owner", required = false) String owner,
            @RequestParam(name = "objectType", required = false) String objectType) {
        log.info("objects of type {} for owner {}", objectType, owner);
        return dbinManager.getObjects(owner, objectType == null ? DEFAULT_OBJECT_TYPE : objectType);
    }

    @GetMapping(METHOD_TABDEF)
    public @ResponseBody String getTabDef(
            @RequestParam(name = "owner", required = false) String owner,
            @RequestParam(name = "tableName", required = false) String tableName) {
        log.info("tabDef for table {} owned by {}", tableName, owner);
        return dbinManager.getTabDef(owner, tableName);
    }

    @GetMapping(METHOD_TABDATA)
    public @ResponseBody String tabData(
            @RequestParam(name = "owner", required = false) String owner,
            @RequestParam(name = "tableName", required = false) String tableName,
            @RequestParam(name = "columnName", required = false) List<String> columnName,
            @RequestParam(name = "columnValue", required = false) List<String> columnValue) {
        log.info("tabData for table {} owned by {}", tableName, owner);
        return dbinManager.getTabData(owner, tableName, columnName, columnValue);
    }

    @GetMapping(METHOD_USERS)
    public @ResponseBody String users() {
        return dbinManager.users();
    }
}
