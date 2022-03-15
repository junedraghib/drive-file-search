package com.elasticsearch.configuration;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.elasticsearch.repositories")
@ComponentScan(basePackages = { "com.elasticsearch" })
public class ElasticsearchClientConfig extends AbstractElasticsearchConfiguration {

	@Value(value = "${elasticsearch.url}")
	private String elasticSearchURL;

	@Override
	@Bean
	public RestHighLevelClient elasticsearchClient() {

		final ClientConfiguration clientConfiguration = 
				ClientConfiguration
				.builder()
				.connectedTo(elasticSearchURL)
				.build();

		return RestClients
				.create(clientConfiguration)
				.rest();
	}
}
