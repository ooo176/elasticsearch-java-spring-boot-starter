# Spring Boot Starter for Elasticsearch

[![](https://jitpack.io/v/ooo176/elasticsearch-java-spring-boot-starter.svg)](https://jitpack.io/#ooo176/elasticsearch-java-spring-boot-starter)

一个基于 Elasticsearch Java API Client 7.17.7 的 Spring Boot Starter，提供简单易用的 Elasticsearch 操作封装，支持索引管理、文档 CRUD、搜索查询、聚合统计等功能。

## 📋 目录

- [特性](#特性)
- [依赖版本](#依赖版本)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [使用指南](#使用指南)
- [架构设计](#架构设计)
- [常见问题](#常见问题)
- [开发计划](#开发计划)

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
    <version>1.2</version>
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
    
    @Type(type = {"text", "keyword"})
    private String title;
    
    @Type(type = {"text"})
    private String content;
    
    // getters and setters
}
```

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
3. **动态生效**：通过 `@ConditionalOnExpression` 根据配置参数动态决定是否启用 Starter
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
- [ ] 完善 `TypeMappingBuilder`，支持更多字段类型
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