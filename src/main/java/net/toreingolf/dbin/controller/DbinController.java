package net.toreingolf.dbin.controller;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.manager.DbinManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller()
@Slf4j
public class DbinController {

    private final DbinManager dbinManager;

    public DbinController(DbinManager dbinManager) {
        this.dbinManager = dbinManager;
    }

    @GetMapping("/")
    public @ResponseBody String root() {
        log.info("root");
        return dbinManager.getObjects("SCOTT", "TABLE");
    }

    @GetMapping("/objects")
    public @ResponseBody String getObjects(
            @RequestParam(name = "owner", required = false) String owner,
            @RequestParam(name = "objectType", required = false) String objectType) {
        log.info("get objects of type {} for owner {}", objectType, owner);
        return dbinManager.getObjects(owner, objectType);
    }
}
