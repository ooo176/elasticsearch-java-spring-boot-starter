package ooo.github.io.es.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.es.exception.ElasticsearchException;
import ooo.github.io.es.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 7.17.7 服务实现类
 *
 * @author kaiqin
 */
@Slf4j
@Component
public class Elasticsearch7ServiceImpl implements ElasticsearchService {

    @Autowired
    @Qualifier("esClient")
    private ElasticsearchClient client;

    @Override
    public CreateIndexResponse createIndex(CreateIndexRequest createIndexRequest) {
        if (createIndexRequest == null) {
            throw new IllegalArgumentException("创建索引请求不能为空");
        }
        if (StringUtils.isEmpty(createIndexRequest.index())) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            log.debug("创建索引开始, 索引名称: {}", createIndexRequest.index());
            CreateIndexResponse indexResponse = client.indices().create(createIndexRequest);
            log.debug("创建索引成功, 索引名称: {}, 结果: {}", createIndexRequest.index(), indexResponse.acknowledged());
            return indexResponse;
        } catch (IOException e) {
            String errorMsg = String.format("创建索引失败, 索引名称: %s", createIndexRequest.index());
            log.error(errorMsg, e);
            throw new ElasticsearchException(errorMsg, e);
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            String errorMsg = String.format("创建索引失败, 索引名称: %s, 错误: %s", createIndexRequest.index(), e.getMessage());
            log.error(errorMsg, e);
            throw new ooo.github.io.es.exception.ElasticsearchException(errorMsg, e);
        }
    }

    @Override
    public DeleteIndexResponse deleteIndex(DeleteIndexRequest deleteIndexRequest) {
        if (deleteIndexRequest == null) {
            throw new IllegalArgumentException("删除索引请求不能为空");
        }
        if (StringUtils.isEmpty(deleteIndexRequest.index())) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            log.debug("删除索引开始, 索引名称: {}", deleteIndexRequest.index());
            DeleteIndexResponse indexResponse = client.indices().delete(deleteIndexRequest);
            log.debug("删除索引成功, 索引名称: {}, 结果: {}", deleteIndexRequest.index(), indexResponse.acknowledged());
            return indexResponse;
        } catch (IOException e) {
            String errorMsg = String.format("删除索引失败, 索引名称: %s", deleteIndexRequest.index());
            log.error(errorMsg, e);
            throw new ElasticsearchException(errorMsg, e);
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            String errorMsg = String.format("删除索引失败, 索引名称: %s, 错误: %s", deleteIndexRequest.index(), e.getMessage());
            log.error(errorMsg, e);
            throw new ooo.github.io.es.exception.ElasticsearchException(errorMsg, e);
        }
    }

    @Override
    public BooleanResponse existIndex(ExistsRequest existsRequest) {
        if (existsRequest == null) {
            throw new IllegalArgumentException("查询索引请求不能为空");
        }
        if (StringUtils.isEmpty(existsRequest.index())) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            log.debug("查询索引是否存在, 索引名称: {}", existsRequest.index());
            BooleanResponse response = client.indices().exists(existsRequest);
            log.debug("查询索引是否存在, 索引名称: {}, 结果: {}", existsRequest.index(), response.value());
            return response;
        } catch (IOException e) {
            String errorMsg = String.format("查询索引是否存在失败, 索引名称: %s", existsRequest.index());
            log.error(errorMsg, e);
            throw new ElasticsearchException(errorMsg, e);
        }
    }

    @Override
    public <T> BulkResponse bulk(BulkRequest bulkRequest) {
        if (bulkRequest == null) {
            throw new IllegalArgumentException("批量操作请求不能为空");
        }

        try {
            log.debug("批量操作开始, 索引名称: {}, 操作数量: {}", 
                    bulkRequest.index(), 
                    bulkRequest.operations() != null ? bulkRequest.operations().size() : 0);
            BulkResponse bulkResponse = client.bulk(bulkRequest);
            if (bulkResponse.errors()) {
                List<BulkResponseItem> errorItems = bulkResponse.items().stream()
                        .filter(item -> item.error() != null)
                        .collect(Collectors.toList());
                log.warn("批量操作存在错误, 错误数量: {}, 错误详情: {}", errorItems.size(), errorItems);
            } else {
                log.debug("批量操作成功, 操作数量: {}", bulkResponse.items().size());
            }
            return bulkResponse;
        } catch (IOException e) {
            String errorMsg = "批量操作失败";
            log.error(errorMsg, e);
            throw new ElasticsearchException(errorMsg, e);
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            String errorMsg = String.format("批量操作失败, 错误: %s", e.getMessage());
            log.error(errorMsg, e);
            throw new ooo.github.io.es.exception.ElasticsearchException(errorMsg, e);
        }
    }

    @Override
    public <TDocument> SearchResponse<TDocument> search(SearchRequest request, Class<TDocument> tDocumentClass) {
        if (request == null) {
            throw new IllegalArgumentException("搜索请求不能为空");
        }
        if (tDocumentClass == null) {
            throw new IllegalArgumentException("文档类型不能为空");
        }

        try {
            log.debug("查询ES数据开始, 索引: {}, 文档类型: {}", 
                    request.index(), 
                    tDocumentClass.getSimpleName());
            SearchResponse<TDocument> response = client.search(request, tDocumentClass);
            log.debug("查询ES数据成功, 索引: {}, 命中数量: {}", 
                    request.index(), 
                    response.hits().total() != null ? response.hits().total().value() : 0);
            return response;
        } catch (IOException e) {
            String errorMsg = String.format("查询ES数据失败, 索引: %s", request.index());
            log.error(errorMsg, e);
            throw new ElasticsearchException(errorMsg, e);
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            String errorMsg = String.format("查询ES数据失败, 索引: %s, 错误: %s", request.index(), e.getMessage());
            log.error(errorMsg, e);
            throw new ooo.github.io.es.exception.ElasticsearchException(errorMsg, e);
        }
    }

    @Override
    public DeleteByQueryResponse delete(DeleteByQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("删除请求不能为空");
        }
        if (StringUtils.isEmpty(request.index())) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        try {
            log.debug("删除ES数据开始, 索引: {}", request.index());
            DeleteByQueryResponse deleteByQuery = client.deleteByQuery(request);
            log.debug("删除ES数据成功, 索引: {}, 删除数量: {}", 
                    request.index(), 
                    deleteByQuery.deleted());
            return deleteByQuery;
        } catch (IOException e) {
            String errorMsg = String.format("删除ES数据失败, 索引: %s", request.index());
            log.error(errorMsg, e);
            throw new ElasticsearchException(errorMsg, e);
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            String errorMsg = String.format("删除ES数据失败, 索引: %s, 错误: %s", request.index(), e.getMessage());
            log.error(errorMsg, e);
            throw new ooo.github.io.es.exception.ElasticsearchException(errorMsg, e);
        }
    }

}
