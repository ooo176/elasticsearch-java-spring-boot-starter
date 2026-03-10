# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot Starter for Elasticsearch 7.17.7, providing simplified wrappers around the Elasticsearch Java API Client. It's published via JitPack and designed to be used as a dependency in other Spring Boot projects.

**Key characteristics:**
- Maven-based Java 1.8 project
- Spring Boot 2.3.6.RELEASE
- Elasticsearch Java API Client 7.17.7
- Conditional auto-configuration based on `elasticsearch.version` property

## Architecture

### Two-Layer Service Design

The starter provides two service interfaces with different abstraction levels:

1. **ElasticsearchService** (`ooo.github.io.es.service.ElasticsearchService`)
   - Low-level wrapper around native Elasticsearch Java API Client
   - Direct access to request/response objects (CreateIndexRequest, SearchRequest, etc.)
   - Use when you need full control over Elasticsearch operations

2. **ElasticsearchSimpleService** (`ooo.github.io.es.service.ElasticsearchSimpleService`)
   - High-level simplified API for common operations
   - Methods accept simple parameters (indexName, Query, Class<T>)
   - Handles index creation, bulk operations, search with pagination
   - Implementation: `Elasticsearch7SimpleServiceImpl`

### Auto-Configuration

`ElasticsearchAutoConfiguration` is the entry point:
- Only activates when `elasticsearch.version` property is set
- Creates `ElasticsearchClient` bean with authentication and timeout configuration
- Component scans `ooo.github.io.es` package

Configuration properties are in `ElasticsearchProperties`:
- Connection: host, port, username, password
- Timeouts: connectTimeout (default 5000ms), socketTimeout (default 60000ms)
- Index settings: numberOfShards, maxResultWindow

### Annotation-Driven Mapping

Three annotations define document structure:

- **@IndexName** - Class-level, specifies index name
- **@Id** - Field-level, marks the document ID field
- **@Type** - Field-level, defines Elasticsearch field type and mapping properties
  - Supports all ES types: text, keyword, date, numeric types, geo types, nested, etc.
  - Multi-field support via array: `type = {"text", "keyword"}`
  - Additional properties: analyzer, format, scalingFactor, dims, relations, etc.

`TypeMappingBuilder` uses reflection to generate TypeMapping from annotated classes, including parent class fields.

### Utilities

- **TypeMappingBuilder** - Generates Elasticsearch mappings from Java classes with @Type annotations
- **HighlightUtil** - Processes search response highlights and merges them into result objects
- **SearchResponseUtil** - Extracts aggregation results from SearchResponse

## Build and Test

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

Tests require a running Elasticsearch 7.17.7 instance. Configure connection in test resources or via system properties:
```
-Delasticsearch.host=localhost
-Delasticsearch.port=9200
-Delasticsearch.version=7.17.7
```

Test class: `EsSimpleServiceTest` demonstrates index creation, bulk operations, search, and aggregations.

## Configuration Requirements

The starter only activates when `elasticsearch.version` is configured in application.yml/properties:

```yaml
elasticsearch:
  version: 7.17.7  # Required - must be exactly "7.17.7"
  host: localhost
  port: 9200
  username: elastic  # Optional
  password: password  # Optional
```

## Important Notes

- This is a library project, not a standalone application - it's meant to be used as a dependency
- The version check is strict: only "7.17.7" activates the auto-configuration
- All operations are logged with input/output parameters for debugging
- Document classes should use annotations (@IndexName, @Id, @Type) for automatic mapping generation
