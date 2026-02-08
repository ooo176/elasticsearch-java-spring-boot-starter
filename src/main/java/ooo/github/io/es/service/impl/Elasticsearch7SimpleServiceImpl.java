package ooo.github.io.es.service.impl;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.es.anno.Id;
import ooo.github.io.es.config.ElasticsearchProperties;
import ooo.github.io.es.dto.SearchInput;
import ooo.github.io.es.exception.ElasticsearchException;
import ooo.github.io.es.service.ElasticsearchService;
import ooo.github.io.es.service.ElasticsearchSimpleService;
import ooo.github.io.es.util.TypeMappingBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 简化服务实现类
 *
 * @author kaiqin
 */
@Slf4j
@Component
public class Elasticsearch7SimpleServiceImpl implements ElasticsearchSimpleService {

    private final ElasticsearchProperties properties;
    private final ElasticsearchService elasticsearchService;

    public Elasticsearch7SimpleServiceImpl(ElasticsearchProperties properties, ElasticsearchService elasticsearchService) {
        this.properties = properties;
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public boolean createIndex(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(indexName)
                    .settings(new IndexSettings.Builder()
                            .maxResultWindow(properties.getIndex().getMaxResultWindow())
                            .numberOfShards(properties.getIndex().getNumberOfShards())
                            .build())
                    .build();
            CreateIndexResponse index = elasticsearchService.createIndex(createIndexRequest);
            return index != null && index.acknowledged();
        } catch (ElasticsearchException e) {
            log.error("创建索引失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public boolean createIndex(String indexName, TypeMapping typeMapping) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            CreateIndexRequest.Builder createRequest = new CreateIndexRequest.Builder()
                    .index(indexName)
                    .settings(new IndexSettings.Builder()
                            .maxResultWindow(properties.getIndex().getMaxResultWindow())
                            .numberOfShards(properties.getIndex().getNumberOfShards())
                            .build());
            if (typeMapping != null) {
                createRequest.mappings(typeMapping);
            }
            CreateIndexResponse index = elasticsearchService.createIndex(createRequest.build());
            return index != null && index.acknowledged();
        } catch (ElasticsearchException e) {
            log.error("创建索引失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public <T> boolean createIndex(String indexName, Class<T> clazz) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("文档类型不能为空");
        }

        try {
            TypeMapping typeMapping = TypeMappingBuilder.mapBuilder(clazz);
            return createIndex(indexName, typeMapping);
        } catch (Exception e) {
            log.error("根据类创建索引失败, 索引名称: {}, 类型: {}", indexName, clazz.getName(), e);
            throw new ElasticsearchException("根据类创建索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteIndex(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder().index(indexName).build();
            DeleteIndexResponse response = elasticsearchService.deleteIndex(deleteRequest);
            return response.acknowledged();
        } catch (ElasticsearchException e) {
            log.error("删除索引失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public boolean delete(String indexName, Query query) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if (query == null) {
            throw new IllegalArgumentException("查询条件不能为空");
        }

        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder()
                    .query(query)
                    .index(indexName)
                    .build();
            DeleteByQueryResponse delete = elasticsearchService.delete(deleteByQueryRequest);
            return CollectionUtils.isEmpty(delete.failures());
        } catch (ElasticsearchException e) {
            log.error("删除文档失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public boolean existIndex(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            ExistsRequest existsRequest = new ExistsRequest.Builder().index(indexName).build();
            BooleanResponse response = elasticsearchService.existIndex(existsRequest);
            return response.value();
        } catch (ElasticsearchException e) {
            log.error("查询索引是否存在失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public <T> boolean bulk(String indexName, List<T> ts) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if (CollectionUtils.isEmpty(ts)) {
            log.warn("批量插入数据为空, 索引名称: {}", indexName);
            return false;
        }

        try {
            Field idField = getId(ts.get(0).getClass());
            List<BulkOperation> operationList = new ArrayList<>(ts.size());
            for (T t : ts) {
                if (t == null) {
                    log.warn("批量插入数据中存在空对象, 跳过");
                    continue;
                }
                try {
                    Object idValue = idField.get(t);
                    if (idValue == null) {
                        log.warn("文档ID为空, 跳过该文档");
                        continue;
                    }
                    String id = idValue.toString();
                    BulkOperation bulkOperation = new BulkOperation.Builder()
                            .index(new IndexOperation.Builder<>().document(t).id(id).build())
                            .build();
                    operationList.add(bulkOperation);
                } catch (IllegalAccessException e) {
                    log.error("获取文档ID失败", e);
                    throw new ElasticsearchException("获取文档ID失败: " + e.getMessage(), e);
                }
            }
            
            if (CollectionUtils.isEmpty(operationList)) {
                log.warn("批量插入操作列表为空");
                return false;
            }

            BulkRequest bulkRequest = new BulkRequest.Builder()
                    .index(indexName)
                    .operations(operationList)
                    .build();
            BulkResponse bulkResponse = elasticsearchService.bulk(bulkRequest);
            return !bulkResponse.errors();
        } catch (ElasticsearchException e) {
            log.error("批量插入失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public <T> boolean bulk(String indexName, List<T> ts, boolean ignoreEsId) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if (CollectionUtils.isEmpty(ts)) {
            log.warn("批量插入数据为空, 索引名称: {}", indexName);
            return false;
        }

        if (!ignoreEsId) {
            //如果强制要求document对应的id存在，则走另一个bulk方法
            return bulk(indexName, ts);
        }

        try {
            List<BulkOperation> operationList = new ArrayList<>(ts.size());
            for (T t : ts) {
                if (t == null) {
                    log.warn("批量插入数据中存在空对象, 跳过");
                    continue;
                }
                BulkOperation bulkOperation = new BulkOperation.Builder()
                        .index(new IndexOperation.Builder<>().document(t).build())
                        .build();
                operationList.add(bulkOperation);
            }
            
            if (CollectionUtils.isEmpty(operationList)) {
                log.warn("批量插入操作列表为空");
                return false;
            }

            BulkRequest bulkRequest = new BulkRequest.Builder()
                    .index(indexName)
                    .operations(operationList)
                    .build();
            BulkResponse bulkResponse = elasticsearchService.bulk(bulkRequest);
            return !bulkResponse.errors();
        } catch (ElasticsearchException e) {
            log.error("批量插入失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public <T> SearchResponse<T> search(String indexName, Query query, Class<T> tClass) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if (query == null) {
            throw new IllegalArgumentException("查询条件不能为空");
        }
        if (tClass == null) {
            throw new IllegalArgumentException("文档类型不能为空");
        }

        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(indexName)
                    .query(query)
                    .build();
            return elasticsearchService.search(searchRequest, tClass);
        } catch (ElasticsearchException e) {
            log.error("查询失败, 索引名称: {}", indexName, e);
            throw e;
        }
    }

    @Override
    public <T> SearchResponse<T> search(String indexName, Query query, Integer from, Integer size, Class<T> tClass) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if (query == null) {
            throw new IllegalArgumentException("查询条件不能为空");
        }
        if (tClass == null) {
            throw new IllegalArgumentException("文档类型不能为空");
        }
        if (from != null && from < 0) {
            throw new IllegalArgumentException("分页起始位置不能小于0");
        }
        if (size != null && size < 0) {
            throw new IllegalArgumentException("分页大小不能小于0");
        }

        try {
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(indexName)
                    .query(query)
                    .trackTotalHits(new TrackHits.Builder().enabled(true).build());
            if (from != null) {
                builder.from(from);
            }
            if (size != null) {
                builder.size(size);
            }
            return elasticsearchService.search(builder.build(), tClass);
        } catch (ElasticsearchException e) {
            log.error("查询失败, 索引名称: {}, from: {}, size: {}", indexName, from, size, e);
            throw e;
        }
    }

    @Override
    public <T> SearchResponse<T> search(SearchInput<T> input) {
        if (input == null) {
            throw new IllegalArgumentException("搜索输入参数不能为空");
        }
        if (input.getTClass() == null) {
            throw new IllegalArgumentException("搜索的泛型不能为空");
        }
        
        // 检查索引名称
        List<String> indexNameList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(input.getIndexNames())) {
            indexNameList.addAll(input.getIndexNames());
        }
        if (!StringUtils.isEmpty(input.getIndexName())) {
            indexNameList.add(input.getIndexName());
        }
        if (CollectionUtils.isEmpty(indexNameList)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        // 检查分页参数
        if (input.getFrom() != null && input.getFrom() < 0) {
            throw new IllegalArgumentException("分页起始位置不能小于0");
        }
        if (input.getSize() != null && input.getSize() < 0) {
            throw new IllegalArgumentException("分页大小不能小于0");
        }

        try {
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder().index(indexNameList);
            if (input.getQuery() != null) {
                searchBuilder.query(input.getQuery());
            }
            if (input.getAggregations() != null) {
                searchBuilder.aggregations(input.getAggregations());
            }
            if (input.getFrom() != null) {
                searchBuilder.from(input.getFrom());
            }
            if (input.getSize() != null) {
                searchBuilder.size(input.getSize());
            }
            if (input.getHighlight() != null) {
                searchBuilder.highlight(input.getHighlight());
            }
            if (input.getSortOptions() != null) {
                searchBuilder.sort(input.getSortOptions());
            }
            if (input.getCollapse() != null) {
                searchBuilder.collapse(input.getCollapse());
            }
            if (input.getTrackHits() != null) {
                searchBuilder.trackTotalHits(input.getTrackHits());
            }
            return elasticsearchService.search(searchBuilder.build(), input.getTClass());
        } catch (ElasticsearchException e) {
            log.error("搜索失败, 索引: {}", indexNameList, e);
            throw e;
        }
    }

    private <T> Field getId(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("类不能为空");
        }

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Id esId = field.getAnnotation(Id.class);
            if (esId != null) {
                return field;
            }
        }
        throw new ElasticsearchException("未发现对应的@Id注解, 类: " + clazz.getName());
    }

}
