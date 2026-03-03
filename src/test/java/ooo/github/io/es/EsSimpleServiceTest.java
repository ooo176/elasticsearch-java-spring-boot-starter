package ooo.github.io.es;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import ooo.github.io.es.dto.DpResourceDataDocument;
import ooo.github.io.es.dto.QyDocument;
import ooo.github.io.es.dto.SearchInput;
import ooo.github.io.es.service.ElasticsearchSimpleService;
import ooo.github.io.es.util.SearchResponseUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ElasticsearchAutoConfiguration.class})
public class EsSimpleServiceTest {

    private static final String indexName = "aaa";
    @Autowired
    private ElasticsearchSimpleService simpleService;

    @Before
    public void createIndexIfNotExist() {
        boolean existIndex = simpleService.existIndex(indexName);
        if (existIndex) {
            return;
        }
        boolean createIndex = simpleService.createIndex(indexName, QyDocument.class);
    }

    @org.junit.Test
    public void testCreate() {
        boolean qk1 = simpleService.createIndex("qk123");
        Assert.assertEquals(qk1, Boolean.TRUE);
    }

    @org.junit.Test
    public void testDpCreate() {
        boolean created = simpleService.createIndex("qk111", DpResourceDataDocument.class);
        Assert.assertEquals(created, Boolean.TRUE);
    }


    @org.junit.Test
    public void bulk() {
        List<QyDocument> tList = QyDocument.mock();
        simpleService.bulk(indexName, tList);
    }

    @Test
    public void testSearch() {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        long pageNo = 1L;
        long pageSize = 10L;
        List<String> qyidList = new ArrayList<>();
        qyidList.add("1");
        qyidList.add("2");
        qyidList.add("3");
        List<FieldValue> fieldValueList = qyidList.stream().map(FieldValue::of).collect(Collectors.toList());
        // TermsQuery.Builder termsBuilder = QueryBuilders.terms().field("qyId").terms(new TermsQueryField.Builder().value(fieldValueList).build());
        Query match = QueryBuilders.match().field("qyId").query("1").build()._toQuery();
        Query query = QueryBuilders.matchPhrase().field("qyMc").query("企业1").build()._toQuery();
        boolQuery.must(match);
        // boolQuery.must(termsBuilder.build()._toQuery());
        SearchInput searchInput = new SearchInput();
        searchInput.setIndexName("aaa");
        searchInput.setQuery(boolQuery.build()._toQuery());
        searchInput.setTClass(QyDocument.class);
        searchInput.setFrom((int) ((pageNo - 1) * pageSize));
        searchInput.setSize((int) pageSize);
        SearchResponse<QyDocument> search = simpleService.search(searchInput);
        List<QyDocument> collect = search.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        collect.forEach(System.out::println);


    }

    @Test
    public void testAggregation() {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        long pageNo = 1L;
        long pageSize = 10L;
        List<String> qyidList = new ArrayList<>();
        qyidList.add("4");
        qyidList.add("5");
        qyidList.add("10");
        qyidList.add("11");
        qyidList.add("12");
        qyidList.add("13");
        qyidList.add("14");

        List<FieldValue> fieldValueList = qyidList.stream().map(FieldValue::of).collect(Collectors.toList());
        TermsQuery.Builder termsBuilder = QueryBuilders.terms().field("qyId").terms(new TermsQueryField.Builder().value(fieldValueList).build());
        boolQuery.must(termsBuilder.build()._toQuery());
        SearchInput searchInput = new SearchInput();
        searchInput.setIndexName("bbb");
        searchInput.setQuery(boolQuery.build()._toQuery());
        searchInput.setTClass(QyDocument.class);
        searchInput.setFrom((int) ((pageNo - 1) * pageSize));
        searchInput.setSize((int) pageSize);

        searchInput.addStringTermsTypeAggregation("qyMcAgg", "qyMc", 50);
        SearchResponse<QyDocument> searchResponse = simpleService.search(searchInput);

        Map<Object, Long> result = SearchResponseUtil.readStreamTypeAggregation(searchResponse, "qyMcAgg");
        result.forEach((k, v) -> System.out.println(k.toString() + " " + v));

    }


}
