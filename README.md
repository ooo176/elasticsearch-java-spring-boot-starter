# Spring Boot Starter for Elasticsearch

[![](https://jitpack.io/v/ooo176/elasticsearch-java-spring-boot-starter.svg)](https://jitpack.io/#ooo176/elasticsearch-java-spring-boot-starter)

<div align="right">

[English](README_EN.md) | **中文**

</div>

---

一个基于 Elasticsearch Java API Client 7.17.7 的 Spring Boot Starter，提供简单易用的 Elasticsearch 操作封装，支持索引管理、文档 CRUD、搜索查询、聚合统计等功能。

## ✨ 特性

- 🚀 **开箱即用**：通过配置 `elasticsearch.version` 参数动态生效，无需额外配置
- 📦 **双重服务层**：提供 `ElasticsearchService`（原生 API 封装）和 `ElasticsearchSimpleService`（简化操作）
- 🔍 **完整功能**：支持索引管理、文档 CRUD、复杂查询、聚合统计、高亮显示等
- 📝 **统一日志**：自动记录所有操作的入参和出参，便于调试和排查问题
- 🎯 **注解驱动**：通过 `@Id`、`@IndexName`、`@Type` 等注解简化配置
- 🛡️ **类型安全**：基于 Elasticsearch Java API Client，提供完整的类型支持

## 📦 依赖版本

| 组件                            | 版本            |
| ----------------------------- | ------------- |
| Spring Boot                   | 2.3.6.RELEASE |
| Elasticsearch Java API Client | 7.17.7        |
| Java                          | 1.8+          |

## 🚀 快速开始

### 1. 添加依赖

#### 1.1 添加 JitPack 仓库

在 `pom.xml` 中添加：

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

#### 1.2 添加 Starter 依赖

```xml
<dependency>
    <groupId>com.github.ooo176</groupId>
    <artifactId>spring-boot-starter-elasticsearch</artifactId>
    <version>1.3</version>
</dependency>
```

### 2. 配置 Elasticsearch

在 `application.yml` 或 `application.properties` 中添加配置：

```yaml
elasticsearch:
  cluster_name: ESCluster1
  host: localhost
  port: 9200
  version: 7.17.7  # 必须配置此参数，且值为 7.17.7 才会生效
  username: elastic  # 可选，如果 ES 需要认证
  password: 123456   # 可选，如果 ES 需要认证
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

### 3. 使用服务

#### 3.1 使用简化服务（推荐）

```java
@Autowired
private ElasticsearchSimpleService simpleService;

// 创建索引
boolean success = simpleService.createIndex("my_index");

// 批量插入文档
List<MyDocument> documents = Arrays.asList(...);
boolean success = simpleService.bulk("my_index", documents);

// 查询文档
Query query = QueryBuilders.match().field("title").query("搜索关键词").build()._toQuery();
SearchResponse<MyDocument> response = simpleService.search("my_index", query, MyDocument.class);
```

#### 3.2 使用原生服务

```java
@Autowired
private ElasticsearchService esService;

// 创建索引
CreateIndexRequest request = new CreateIndexRequest.Builder()
    .index("my_index")
    .build();
CreateIndexResponse response = esService.createIndex(request);
```

## ⚙️ 配置说明

| 参数                                    | 默认值       | 说明                                       | 示例                        |
| ------------------------------------- | --------- | ---------------------------------------- | ------------------------- |
| `elasticsearch.version`               | -         | **必填**，根据此参数决定是否启用 Starter，目前仅支持 `7.17.7` | `7.17.7`                  |
| `elasticsearch.host`                  | -         | Elasticsearch 主机地址                       | `127.0.0.1` 或 `localhost` |
| `elasticsearch.port`                  | `9200`    | Elasticsearch HTTP 端口                    | `9200`                    |
| `elasticsearch.username`              | -         | Elasticsearch 认证用户名（可选）                  | `elastic`                 |
| `elasticsearch.password`              | -         | Elasticsearch 认证密码（可选）                   | `123456`                  |
| `elasticsearch.connectTimeout`        | `5000`    | 连接超时时间（毫秒）                               | `5000`                    |
| `elasticsearch.socketTimeout`         | `60000`   | Socket 超时时间（毫秒）                          | `60000`                   |
| `elasticsearch.index.numberOfShards`  | `1`       | 创建索引时的分片数                                | `3`                       |
| `elasticsearch.index.maxResultWindow` | `1000000` | 深度分页查询的最大结果窗口                            | `1000000`                 |

## 📖 使用指南

### 文档实体类定义

使用注解定义文档实体：

```java
import ooo.github.io.es.anno.Id;
import ooo.github.io.es.anno.IndexName;
import ooo.github.io.es.anno.Type;

@IndexName("my_documents")
public class MyDocument {
    
    @Id
    private String id;
    
    // text 类型，支持全文搜索，同时添加 keyword 子字段用于精确匹配和排序
    // 索引时使用 ik_max_word（细粒度分词，最大化匹配），查询时使用 ik_smart（粗粒度分词，提高精准度）
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    // text 类型，索引和查询使用不同分词器的最佳实践
    // analyzer: 索引时分词器，使用 ik_max_word 进行最细粒度拆分，确保搜索结果不遗漏
    // searchAnalyzer: 查询时分词器，使用 ik_smart 进行粗粒度拆分，提高搜索精准度
    @Type(type = {"text"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    
    // keyword 类型，用于精确匹配
    @Type(type = {"keyword"}, ignoreAbove = 256)
    private String email;
    
    // 数值类型
    @Type(type = {"integer"})
    private Integer age;
    
    @Type(type = {"double"})
    private Double price;
    
    // scaled_float 类型，用于精确的小数存储（如价格、评分）
    @Type(type = {"scaled_float"}, scalingFactor = 100.0)
    private Double rating;
    
    // 日期类型，支持多种格式
    @Type(type = {"date"}, format = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private Date createdAt;
    
    // 布尔类型
    @Type(type = {"boolean"})
    private Boolean isPublished;
    
    // IP 地址类型
    @Type(type = {"ip"})
    private String ipAddress;
    
    // 地理坐标点
    @Type(type = {"geo_point"})
    private String location;
    
    // dense_vector 类型，用于向量搜索
    @Type(type = {"dense_vector"}, dims = 128)
    private float[] embedding;
    
    // join 类型，建立父子文档关系
    @Type(type = {"join"}, relations = "question:answer")
    private String joinField;
    
    // token_count 类型，统计词条数量
    @Type(type = {"token_count"}, analyzer = "standard")
    private Integer wordCount;
    
    // getters and setters
}
```

#### @Type 注解参数说明

| 参数 | 类型 | 默认值 | 说明 | 适用类型 |
|------|------|--------|------|----------|
| `type` | `String[]` | `{"text", "keyword"}` | 字段类型，第一个为主类型，其他作为多字段 | 所有类型 |
| `analyzer` | `String` | `""` | 分词器，用于索引时分词 | `text`, `token_count` |
| `searchAnalyzer` | `String` | `""` | 搜索分词器，用于查询时分词 | `text` |
| `format` | `String` | `""` | 日期格式，如 `yyyy-MM-dd HH:mm:ss\|epoch_millis` | `date` |
| `scalingFactor` | `double` | `1.0` | 缩放因子，实际值 × scalingFactor 后存储 | `scaled_float` |
| `dims` | `int` | `128` | 向量维度 | `dense_vector` |
| `relations` | `String` | `""` | 父子关系，格式：`parent:child1,child2` 或 `parent1:child1;parent2:child2` | `join` |
| `ignoreAbove` | `int` | `0` | 超过指定字符数的值不会被索引（0 表示使用默认值 256） | `keyword` |

#### 分词器最佳实践

**推荐做法：索引和查询使用不同的分词器**

在实际应用中，一个被广泛推荐且行之有效的做法是：**索引和查询时使用不同的分词器**。

- **索引时（Indexing）用 `ik_max_word`**：在建立索引时，使用 `ik_max_word` 对文本进行最细粒度的拆分。这样可以确保数据被尽可能多地匹配到，**最大化地保证搜索结果"不遗漏"**。
- **查询时（Searching）用 `ik_smart`**：当用户输入关键词进行搜索时，使用 `ik_smart` 对查询词进行粗粒度拆分。这样查询会被拆成较少、较长的词，匹配条件更集中，**有利于提高搜索结果的精准度**。

**使用示例：**

```java
// 索引时使用 ik_max_word，查询时使用 ik_smart
@Type(type = {"text"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
private String content;

// 同时支持全文搜索和精确匹配的场景
@Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
private String title;
```

#### 支持的数据类型

**文本类型：**
- `text` - 全文搜索字段，会被分词
- `keyword` - 精确匹配字段，不分词
- `search_as_you_type` - 输入即搜索

**数值类型：**
- `long`, `integer`, `short`, `byte` - 整数类型
- `double`, `float`, `half_float`, `scaled_float` - 浮点数类型

**其他类型：**
- `boolean` - 布尔值
- `date` - 日期时间
- `object`, `nested` - 对象和嵌套类型
- `geo_point`, `geo_shape` - 地理类型
- `ip` - IP 地址
- `completion` - 自动补全
- `token_count` - 词条计数
- `join` - 父子关系
- `dense_vector`, `sparse_vector` - 向量类型
- `rank_feature`, `rank_features` - 排名特征

### 索引操作

```java
// 创建索引（使用默认配置）
boolean success = simpleService.createIndex("my_index");

// 创建索引（指定映射）
TypeMapping mapping = TypeMapping.of(m -> m
    .properties("title", p -> p.text(t -> t))
    .properties("content", p -> p.text(t -> t))
);
boolean success = simpleService.createIndex("my_index", mapping);

// 根据类创建索引（自动生成映射）
boolean success = simpleService.createIndex("my_index", MyDocument.class);

// 检查索引是否存在
boolean exists = simpleService.existIndex("my_index");

// 删除索引
boolean success = simpleService.deleteIndex("my_index");
```

### 文档操作

```java
// 批量插入（使用 @Id 注解的字段作为文档 ID）
List<MyDocument> documents = Arrays.asList(...);
boolean success = simpleService.bulk("my_index", documents);

// 批量插入（忽略文档 ID，由 ES 自动生成）
boolean success = simpleService.bulk("my_index", documents, true);

// 根据条件删除文档
Query query = QueryBuilders.term().field("status").value("deleted").build()._toQuery();
boolean success = simpleService.delete("my_index", query);
```

### 查询操作

#### 基础查询

```java
// 简单查询
Query query = QueryBuilders.match().field("title").query("关键词").build()._toQuery();
SearchResponse<MyDocument> response = simpleService.search("my_index", query, MyDocument.class);

// 分页查询
SearchResponse<MyDocument> response = simpleService.search(
    "my_index", 
    query, 
    0,  // from
    10, // size
    MyDocument.class
);
```

#### 复杂查询

```java
import ooo.github.io.es.dto.SearchInput;

// 构建复杂查询
BoolQuery.Builder boolQuery = QueryBuilders.bool();
boolQuery.must(QueryBuilders.match().field("title").query("关键词").build()._toQuery());
boolQuery.filter(QueryBuilders.range().field("createTime").gte(JsonData.of("2024-01-01")).build()._toQuery());

SearchInput<MyDocument> searchInput = new SearchInput<>();
searchInput.setIndexName("my_index");
searchInput.setQuery(boolQuery.build()._toQuery());
searchInput.setTClass(MyDocument.class);
searchInput.setFrom(0);
searchInput.setSize(10);

// 添加高亮
Highlight highlight = Highlight.of(h -> h
    .fields("title", HighlightField.of(f -> f))
    .fields("content", HighlightField.of(f -> f))
);
searchInput.setHighlight(highlight);

// 添加排序
searchInput.setSortOptions(Arrays.asList(
    SortOptions.of(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc)))
));

// 执行查询
SearchResponse<MyDocument> response = simpleService.search(searchInput);

// 处理高亮结果
HighlightUtil.convert(response);
```

#### 聚合查询

```java
SearchInput<MyDocument> searchInput = new SearchInput<>();
searchInput.setIndexName("my_index");
searchInput.setQuery(query);
searchInput.setTClass(MyDocument.class);

// 添加聚合
searchInput.addStringTermsTypeAggregation("category_agg", "category", 50);

// 执行查询
SearchResponse<MyDocument> response = simpleService.search(searchInput);

// 读取聚合结果
Map<Object, Long> aggregationResult = SearchResponseUtil.readStreamTypeAggregation(
    response, 
    "category_agg"
);
```

## 🏗️ 架构设计

### 设计思路

1. **封装原生 API**：`ElasticsearchService` 封装 Elasticsearch Java API Client 原生方法，入参和出参均为原生类型，提供统一的日志记录
2. **简化操作层**：`ElasticsearchSimpleService` 对 `ElasticsearchService` 进行二次封装，提供更简洁的 API，降低使用门槛
3. **动态生效**：通过 `@ConditionalOnProperty(name = "elasticsearch.version")` 根据配置参数动态决定是否启用 Starter
4. **注解驱动**：通过自定义注解简化索引和文档的配置

### UML 类图

```
┌─────────────────────────┐
│ ElasticsearchService    │  (接口：原生 API 封装)
└───────────┬─────────────┘
            │
            │ implements
            │
┌───────────▼─────────────┐
│ Elasticsearch7ServiceImpl│  (实现类：ES 7.17.7)
└───────────┬─────────────┘
            │
            │ used by
            │
┌───────────▼─────────────┐
│ ElasticsearchSimpleService│  (接口：简化操作)
└───────────┬─────────────┘
            │
            │ implements
            │
┌───────────▼─────────────┐
│Elasticsearch7SimpleServiceImpl│  (实现类：简化操作实现)
└─────────────────────────┘
```

### 核心组件

- **ElasticsearchAutoConfiguration**：自动配置类，负责创建 `ElasticsearchClient` Bean
- **ElasticsearchService**：原生 API 封装服务，提供索引、文档、查询等基础操作
- **ElasticsearchSimpleService**：简化操作服务，提供更友好的 API
- **TypeMappingBuilder**：根据 Java 类自动生成 ES 映射的工具类
- **SearchResponseUtil**：查询结果处理工具类
- **HighlightUtil**：高亮结果处理工具类

## ❓ 常见问题

### 1. JsonParser 不存在

**现象**：编译时提示 `JsonParser` 类不存在

**解决方案**：在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>jakarta.json</groupId>
    <artifactId>jakarta.json-api</artifactId>
    <version>2.0.1</version>
</dependency>
```

**参考**：https://github.com/elastic/elasticsearch-java/issues/79

### 2. 与 Spring Boot 定义的 ES 版本不一致

**现象**：项目使用的 Elasticsearch 版本与 Spring Boot 默认版本冲突

**解决方案**：在项目 `pom.xml` 中显式指定 Elasticsearch 版本：

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

### 3. Missing required property

**现象**：创建索引时提示缺少必需的属性

**解决方案**：检查索引映射配置，确保所有必需属性都已设置

**参考**：https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/missing-required-property.html

### 4. 创建索引后，ES Head 出现 Unassigned 节点

**现象**：单节点 ES 集群创建索引后，副本分片显示为 Unassigned

**解决方案**：对于单节点 Elasticsearch 集群，创建索引时将副本数设置为 0：

```bash
PUT /my_index/_settings
{
  "index": {
    "number_of_replicas": 0
  }
}
```

或者在创建索引时通过配置指定副本数。

### 5. Starter 未生效

**检查清单**：
- ✅ 确认 `elasticsearch.version` 配置为 `7.17.7`
- ✅ 确认已添加 Starter 依赖
- ✅ 确认已添加 JitPack 仓库
- ✅ 检查 Spring Boot 自动配置是否启用

## 🔧 单元测试

项目提供了完整的单元测试用例，基于 JUnit 开发。运行测试前需要：

1. 配置测试环境的 Elasticsearch 连接信息
2. 在 IDEA 中配置 VM options（如果需要）：

```
-Delasticsearch.host=localhost
-Delasticsearch.port=9200
-Delasticsearch.version=7.17.7
```

## 📝 开发计划

- [ ] 支持更多 Elasticsearch 版本（8.x）
- [x] 完善 `TypeMappingBuilder`，支持更多字段类型
- [ ] 优化 `HighlightUtil`，支持递归获取父类属性
- [ ] 添加更多聚合类型的便捷方法
- [ ] 支持批量更新操作
- [ ] 添加连接池配置
- [ ] 支持多数据源配置

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证。

## 👥 作者

- **ooo** - 初始开发

---

**注意**：目前部分方法仍在开发中，如有需要请提 Issue。
