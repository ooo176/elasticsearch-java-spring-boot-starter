# Spring Boot Starter for Elasticsearch

[![](https://jitpack.io/v/ooo176/elasticsearch-java-spring-boot-starter.svg)](https://jitpack.io/#ooo176/elasticsearch-java-spring-boot-starter)

<div align="right">

**English** | [中文](README.md)

</div>

---

A Spring Boot Starter based on Elasticsearch Java API Client 7.17.7, providing simple and easy-to-use Elasticsearch operation encapsulation, supporting index management, document CRUD, search queries, aggregation statistics, and more.

## ✨ Features

- 🚀 **Out of the Box**: Dynamically activated by configuring the `elasticsearch.version` parameter, no additional configuration needed
- 📦 **Dual Service Layer**: Provides `ElasticsearchService` (native API wrapper) and `ElasticsearchSimpleService` (simplified operations)
- 🔍 **Complete Functionality**: Supports index management, document CRUD, complex queries, aggregation statistics, highlighting, and more
- 📝 **Unified Logging**: Automatically logs input and output parameters for all operations, facilitating debugging and troubleshooting
- 🎯 **Annotation-Driven**: Simplifies configuration through annotations like `@Id`, `@IndexName`, `@Type`
- 🛡️ **Type Safety**: Based on Elasticsearch Java API Client, providing complete type support

## 📦 Dependencies

| Component                         | Version            |
| --------------------------------- | ------------------ |
| Spring Boot                       | 2.3.6.RELEASE      |
| Elasticsearch Java API Client     | 7.17.7             |
| Java                              | 1.8+               |

## 🚀 Quick Start

### 1. Add Dependencies

#### 1.1 Add JitPack Repository

Add to `pom.xml`:

```xml
<repositories>
    <repository>
        <id>aliyun</id>
        <url>https://maven.aliyun.com/repository/public</url>
    </repository>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

#### 1.2 Add Starter Dependency

```xml
<dependency>
    <groupId>com.github.ooo176</groupId>
    <artifactId>spring-boot-starter-elasticsearch</artifactId>
    <version>1.2</version>
</dependency>
```

### 2. Configure Elasticsearch

Add configuration to `application.yml` or `application.properties`:

```yaml
elasticsearch:
  cluster_name: ESCluster1
  host: localhost
  port: 9200
  version: 7.17.7  # Required: This parameter must be set to 7.17.7 for the starter to take effect
  username: elastic  # Optional, if ES requires authentication
  password: 123456   # Optional, if ES requires authentication
  connectTimeout: 5000
  socketTimeout: 60000
  index:
    numberOfShards: 1
    maxResultWindow: 1000000

management:
  health:
    elasticsearch:
      enabled: false
```

### 3. Use Services

#### 3.1 Use Simplified Service (Recommended)

```java
@Autowired
private ElasticsearchSimpleService simpleService;

// Create index
boolean success = simpleService.createIndex("my_index");

// Bulk insert documents
List<MyDocument> documents = Arrays.asList(...);
boolean success = simpleService.bulk("my_index", documents);

// Search documents
Query query = QueryBuilders.match().field("title").query("search keyword").build()._toQuery();
SearchResponse<MyDocument> response = simpleService.search("my_index", query, MyDocument.class);
```

#### 3.2 Use Native Service

```java
@Autowired
private ElasticsearchService esService;

// Create index
CreateIndexRequest request = new CreateIndexRequest.Builder()
    .index("my_index")
    .build();
CreateIndexResponse response = esService.createIndex(request);
```

## ⚙️ Configuration

| Parameter                              | Default    | Description                                                      | Example                        |
| -------------------------------------- | ---------- | ---------------------------------------------------------------- | ------------------------------ |
| `elasticsearch.version`                | -          | **Required**: Determines whether to enable the Starter, currently only supports `7.17.7` | `7.17.7`                       |
| `elasticsearch.host`                   | -          | Elasticsearch host address                                       | `127.0.0.1` or `localhost`    |
| `elasticsearch.port`                   | `9200`     | Elasticsearch HTTP port                                          | `9200`                         |
| `elasticsearch.username`               | -          | Elasticsearch authentication username (optional)                 | `elastic`                      |
| `elasticsearch.password`               | -          | Elasticsearch authentication password (optional)                 | `123456`                       |
| `elasticsearch.connectTimeout`         | `5000`     | Connection timeout (milliseconds)                                 | `5000`                         |
| `elasticsearch.socketTimeout`          | `60000`    | Socket timeout (milliseconds)                                    | `60000`                        |
| `elasticsearch.index.numberOfShards`  | `1`        | Number of shards when creating index                             | `3`                            |
| `elasticsearch.index.maxResultWindow` | `1000000`  | Maximum result window for deep pagination queries                | `1000000`                      |

## 📖 Usage Guide

### Document Entity Class Definition

Define document entities using annotations:

```java
import ooo.github.io.es.anno.Id;
import ooo.github.io.es.anno.IndexName;
import ooo.github.io.es.anno.Type;

@IndexName("my_documents")
public class MyDocument {
    
    @Id
    private String id;
    
    // text type for full-text search, with keyword subfield for exact matching and sorting
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    // text type with default analyzer
    @Type(type = {"text"})
    private String content;
    
    // keyword type for exact matching
    @Type(type = {"keyword"}, ignoreAbove = 256)
    private String email;
    
    // Numeric types
    @Type(type = {"integer"})
    private Integer age;
    
    @Type(type = {"double"})
    private Double price;
    
    // scaled_float type for precise decimal storage (e.g., prices, ratings)
    @Type(type = {"scaled_float"}, scalingFactor = 100.0)
    private Double rating;
    
    // date type with multiple formats
    @Type(type = {"date"}, format = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private Date createdAt;
    
    // boolean type
    @Type(type = {"boolean"})
    private Boolean isPublished;
    
    // IP address type
    @Type(type = {"ip"})
    private String ipAddress;
    
    // Geo point type
    @Type(type = {"geo_point"})
    private String location;
    
    // dense_vector type for vector search
    @Type(type = {"dense_vector"}, dims = 128)
    private float[] embedding;
    
    // join type for parent-child document relationships
    @Type(type = {"join"}, relations = "question:answer")
    private String joinField;
    
    // token_count type for counting tokens
    @Type(type = {"token_count"}, analyzer = "standard")
    private Integer wordCount;
    
    // getters and setters
}
```

#### @Type Annotation Parameters

| Parameter | Type | Default | Description | Applicable Types |
|-----------|------|---------|-------------|------------------|
| `type` | `String[]` | `{"text", "keyword"}` | Field type, first is primary type, others as multi-fields | All types |
| `analyzer` | `String` | `""` | Analyzer for indexing | `text`, `token_count` |
| `searchAnalyzer` | `String` | `""` | Analyzer for searching | `text` |
| `format` | `String` | `""` | Date format, e.g., `yyyy-MM-dd HH:mm:ss\|epoch_millis` | `date` |
| `scalingFactor` | `double` | `1.0` | Scaling factor, actual value × scalingFactor stored | `scaled_float` |
| `dims` | `int` | `128` | Vector dimensions | `dense_vector` |
| `relations` | `String` | `""` | Parent-child relations, format: `parent:child1,child2` or `parent1:child1;parent2:child2` | `join` |
| `ignoreAbove` | `int` | `0` | Values exceeding this length won't be indexed (0 means use default 256) | `keyword` |

#### Supported Data Types

**Text Types:**
- `text` - Full-text search field, will be analyzed
- `keyword` - Exact match field, not analyzed
- `search_as_you_type` - Search as you type

**Numeric Types:**
- `long`, `integer`, `short`, `byte` - Integer types
- `double`, `float`, `half_float`, `scaled_float` - Floating-point types

**Other Types:**
- `boolean` - Boolean value
- `date` - Date and time
- `object`, `nested` - Object and nested types
- `geo_point`, `geo_shape` - Geo types
- `ip` - IP address
- `completion` - Autocomplete
- `token_count` - Token count
- `join` - Parent-child relationship
- `dense_vector`, `sparse_vector` - Vector types
- `rank_feature`, `rank_features` - Rank features

### Index Operations

```java
// Create index (using default configuration)
boolean success = simpleService.createIndex("my_index");

// Create index (specify mapping)
TypeMapping mapping = TypeMapping.of(m -> m
    .properties("title", p -> p.text(t -> t))
    .properties("content", p -> p.text(t -> t))
);
boolean success = simpleService.createIndex("my_index", mapping);

// Create index from class (auto-generate mapping)
boolean success = simpleService.createIndex("my_index", MyDocument.class);

// Check if index exists
boolean exists = simpleService.existIndex("my_index");

// Delete index
boolean success = simpleService.deleteIndex("my_index");
```

### Document Operations

```java
// Bulk insert (using @Id annotated field as document ID)
List<MyDocument> documents = Arrays.asList(...);
boolean success = simpleService.bulk("my_index", documents);

// Bulk insert (ignore document ID, auto-generated by ES)
boolean success = simpleService.bulk("my_index", documents, true);

// Delete documents by condition
Query query = QueryBuilders.term().field("status").value("deleted").build()._toQuery();
boolean success = simpleService.delete("my_index", query);
```

### Query Operations

#### Basic Query

```java
// Simple query
Query query = QueryBuilders.match().field("title").query("keyword").build()._toQuery();
SearchResponse<MyDocument> response = simpleService.search("my_index", query, MyDocument.class);

// Paginated query
SearchResponse<MyDocument> response = simpleService.search(
    "my_index", 
    query, 
    0,  // from
    10, // size
    MyDocument.class
);
```

#### Complex Query

```java
import ooo.github.io.es.dto.SearchInput;

// Build complex query
BoolQuery.Builder boolQuery = QueryBuilders.bool();
boolQuery.must(QueryBuilders.match().field("title").query("keyword").build()._toQuery());
boolQuery.filter(QueryBuilders.range().field("createTime").gte(JsonData.of("2024-01-01")).build()._toQuery());

SearchInput<MyDocument> searchInput = new SearchInput<>();
searchInput.setIndexName("my_index");
searchInput.setQuery(boolQuery.build()._toQuery());
searchInput.setTClass(MyDocument.class);
searchInput.setFrom(0);
searchInput.setSize(10);

// Add highlighting
Highlight highlight = Highlight.of(h -> h
    .fields("title", HighlightField.of(f -> f))
    .fields("content", HighlightField.of(f -> f))
);
searchInput.setHighlight(highlight);

// Add sorting
searchInput.setSortOptions(Arrays.asList(
    SortOptions.of(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc)))
));

// Execute query
SearchResponse<MyDocument> response = simpleService.search(searchInput);

// Process highlight results
HighlightUtil.convert(response);
```

#### Aggregation Query

```java
SearchInput<MyDocument> searchInput = new SearchInput<>();
searchInput.setIndexName("my_index");
searchInput.setQuery(query);
searchInput.setTClass(MyDocument.class);

// Add aggregation
searchInput.addStringTermsTypeAggregation("category_agg", "category", 50);

// Execute query
SearchResponse<MyDocument> response = simpleService.search(searchInput);

// Read aggregation results
Map<Object, Long> aggregationResult = SearchResponseUtil.readStreamTypeAggregation(
    response, 
    "category_agg"
);
```

## 🏗️ Architecture

### Design Philosophy

1. **Native API Wrapper**: `ElasticsearchService` wraps native Elasticsearch Java API Client methods, with native types for input and output, providing unified logging
2. **Simplified Operation Layer**: `ElasticsearchSimpleService` provides a secondary wrapper over `ElasticsearchService`, offering a more concise API and lower barrier to entry
3. **Dynamic Activation**: Uses `@ConditionalOnProperty(name = "elasticsearch.version")` to dynamically determine whether to enable the Starter based on configuration parameters
4. **Annotation-Driven**: Simplifies index and document configuration through custom annotations

### UML Class Diagram

```
┌─────────────────────────┐
│ ElasticsearchService    │  (Interface: Native API Wrapper)
└───────────┬─────────────┘
            │
            │ implements
            │
┌───────────▼─────────────┐
│ Elasticsearch7ServiceImpl│  (Implementation: ES 7.17.7)
└───────────┬─────────────┘
            │
            │ used by
            │
┌───────────▼─────────────┐
│ ElasticsearchSimpleService│  (Interface: Simplified Operations)
└───────────┬─────────────┘
            │
            │ implements
            │
┌───────────▼─────────────┐
│Elasticsearch7SimpleServiceImpl│  (Implementation: Simplified Operations)
└─────────────────────────┘
```

### Core Components

- **ElasticsearchAutoConfiguration**: Auto-configuration class responsible for creating `ElasticsearchClient` Bean
- **ElasticsearchService**: Native API wrapper service providing basic operations for indices, documents, queries, etc.
- **ElasticsearchSimpleService**: Simplified operation service providing a more user-friendly API
- **TypeMappingBuilder**: Utility class for auto-generating ES mappings from Java classes
- **SearchResponseUtil**: Query result processing utility class
- **HighlightUtil**: Highlight result processing utility class

## ❓ FAQ

### 1. JsonParser Not Found

**Symptom**: Compilation error indicating `JsonParser` class not found

**Solution**: Add dependency to `pom.xml`:

```xml
<dependency>
    <groupId>jakarta.json</groupId>
    <artifactId>jakarta.json-api</artifactId>
    <version>2.0.1</version>
</dependency>
```

**Reference**: https://github.com/elastic/elasticsearch-java/issues/79

### 2. ES Version Conflict with Spring Boot

**Symptom**: Elasticsearch version used in the project conflicts with Spring Boot's default version

**Solution**: Explicitly specify Elasticsearch version in project `pom.xml`:

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>7.17.7</version>
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 3. Missing Required Property

**Symptom**: Error indicating missing required property when creating index

**Solution**: Check index mapping configuration to ensure all required properties are set

**Reference**: https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/missing-required-property.html

### 4. Unassigned Nodes in ES Head After Creating Index

**Symptom**: Replica shards show as Unassigned after creating index in single-node ES cluster

**Solution**: For single-node Elasticsearch clusters, set replica count to 0 when creating index:

```bash
PUT /my_index/_settings
{
  "index": {
    "number_of_replicas": 0
  }
}
```

Or specify replica count through configuration when creating index.

### 5. Starter Not Taking Effect

**Checklist**:
- ✅ Confirm `elasticsearch.version` is configured as `7.17.7`
- ✅ Confirm Starter dependency has been added
- ✅ Confirm JitPack repository has been added
- ✅ Check if Spring Boot auto-configuration is enabled

## 🔧 Unit Tests

The project provides complete unit test cases based on JUnit. Before running tests:

1. Configure Elasticsearch connection information for test environment
2. Configure VM options in IDEA (if needed):

```
-Delasticsearch.host=localhost
-Delasticsearch.port=9200
-Delasticsearch.version=7.17.7
```

## 📝 Development Plan

- [ ] Support more Elasticsearch versions (8.x)
- [ ] Enhance `TypeMappingBuilder` to support more field types
- [ ] Optimize `HighlightUtil` to support recursive parent class property retrieval
- [ ] Add more convenient methods for aggregation types
- [ ] Support bulk update operations
- [ ] Add connection pool configuration
- [ ] Support multi-datasource configuration

## 🤝 Contributing

Issues and Pull Requests are welcome!

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- **ooo** - Initial development

---

**Note**: Some methods are still under development. Please submit an Issue if needed.
