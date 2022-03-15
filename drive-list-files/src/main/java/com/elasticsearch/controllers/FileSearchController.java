package com.elasticsearch.controllers;

import com.elasticsearch.models.File;
import com.elasticsearch.services.FileSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@Slf4j
public class FileSearchController {

    @Autowired
    private FileSearchService fileSearchService;

    @GetMapping("/files")
    @ResponseBody
    public List<File> fetchByFileNameOrContent(@RequestParam(value = "q", required = true) String query){
        log.info("Searching by term contained in file name or contained..");
        return fileSearchService.findByTermContainingInFile(query);
    }
}
