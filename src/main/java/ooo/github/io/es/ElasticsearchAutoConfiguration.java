package ooo.github.io.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.es.config.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.Objects;


/**
 * Elasticsearch自动装配类
 *
 * @author qinkai
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "elasticsearch.version")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@ComponentScan("ooo.github.io.es")
public class ElasticsearchAutoConfiguration {

    private static final String DEFAULT_STR = "-1";

    private final ElasticsearchProperties properties;

    public ElasticsearchAutoConfiguration(ElasticsearchProperties properties) {
        this.properties = properties;
    }

    /**
     * create the API client
     *
     * @return ElasticsearchClient
     */
    @Bean("esClient")
    @Primary
    public ElasticsearchClient client() {
        if (StringUtils.isEmpty(properties.getVersion())) {
            throw new IllegalArgumentException("elasticsearch.version 不能为空");
        }
        if (StringUtils.isEmpty(properties.getHost())) {
            throw new IllegalArgumentException("elasticsearch.host 不能为空");
        }

        log.info("创建 Elasticsearch 客户端，版本: {}, host: {}, port: {}", 
                properties.getVersion(), properties.getHost(), properties.getPort());
        RestClientBuilder builder = RestClient.builder(new HttpHost(properties.getHost(), properties.getPort()));
        
        // 配置认证
        if (StringUtils.hasText(properties.getUsername()) && StringUtils.hasText(properties.getPassword())
                && !Objects.equals(DEFAULT_STR, properties.getUsername()) 
                && !Objects.equals(DEFAULT_STR, properties.getPassword())) {
            //参见elasticsearch的基本认证 https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/master/_basic_authentication.html
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, 
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
            log.debug("已配置 Elasticsearch 基本认证");
        }
        
        // 配置超时
        builder.setRequestConfigCallback(builder1 -> builder1
                .setConnectTimeout(properties.getConnectTimeout())
                .setSocketTimeout(properties.getSocketTimeout()));
        
        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        //elasticsearch 客户端
        return new ElasticsearchClient(transport);
    }


}

