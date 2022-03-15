package com.elasticsearch.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "fileindex")
public class File {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword, name = "name")
    private String fileName;

    @Field(type = FieldType.Keyword, name = "webcontentlink")
    private String webContentLink;

    @Field(type = FieldType.Text, name = "filecontent")
    private String fileContent;
}
