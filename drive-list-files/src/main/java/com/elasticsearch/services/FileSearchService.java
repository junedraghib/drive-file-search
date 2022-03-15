package com.elasticsearch.services;

import com.elasticsearch.models.File;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileSearchService {
    private static final String FILE_INDEX = "fileindex";
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public FileSearchService(final ElasticsearchOperations elasticsearchOperations){
        super();
        this.elasticsearchOperations = elasticsearchOperations;
    }

    //returns list of document ids
    public List<String> createProductIndexBulk(final List<File> files){
        List<IndexQuery> queries = files.stream().map(file-> new IndexQueryBuilder().withId(file.getId()).withObject(file).build()).collect(Collectors.toList());

        return elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(FILE_INDEX));
    }

    //return document id
    public String createFileIndex(File file){
        IndexQuery indexQuery = new IndexQueryBuilder().withId(file.getId()).withObject(file).build();
        return elasticsearchOperations.index(indexQuery, IndexCoordinates.of(FILE_INDEX));
    }

    public List<File> findByFileName(final String fileName){
        Query searchQuery = new StringQuery("{\"match\":{\"name\":{\"query\":\""+ fileName + "\"}}}\"");
        SearchHits<File> fileSearchHits = elasticsearchOperations.search(searchQuery, File.class, IndexCoordinates.of(FILE_INDEX));

        List<File> filesMatches = new ArrayList<>();
        fileSearchHits.forEach(fileHit->{filesMatches.add(fileHit.getContent());});

        return filesMatches;
    }

    public List<File> findByTermContainingInFile(final String term){
        log.info("Searching File contaning term: {}", term);

        // 1. Create query on multiple fields enabling fuzzy search
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(term, "name", "filecontent").fuzziness(Fuzziness.AUTO);
        Query searchQuery = new NativeSearchQueryBuilder().withFilter(queryBuilder).build();

        // 2. Execute search
        SearchHits<File> fileHits =elasticsearchOperations.search(searchQuery, File.class,IndexCoordinates.of(FILE_INDEX));

        //3.Map serachHits to product list
        List<File> filesMatches = new ArrayList<>();
        fileHits.forEach(fileHit->{filesMatches.add(fileHit.getContent());});

        return filesMatches;
    }
}
