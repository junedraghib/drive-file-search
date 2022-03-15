package com.elasticsearch.repositories;

import com.elasticsearch.models.File;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends ElasticsearchRepository<File, String> {
    List<File> findByFileName(String name);
}
