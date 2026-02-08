package ooo.github.io.es.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Elasticsearch 配置属性
 *
 * @author kaiqin
 */
@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {

    /**
     * Elasticsearch 版本号，用于决定是否启用 Starter
     */
    private String version;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号，默认 9200
     */
    private Integer port = 9200;

    /**
     * 用户名（可选）
     */
    private String username;

    /**
     * 密码（可选）
     */
    private String password;

    /**
     * 连接超时时间（毫秒），默认 5000
     */
    private Integer connectTimeout = 5000;

    /**
     * Socket 超时时间（毫秒），默认 60000
     */
    private Integer socketTimeout = 60000;

    /**
     * 索引配置
     */
    private Index index = new Index();

    @Data
    public static class Index {
        /**
         * 分片数，默认 1
         */
        private String numberOfShards = "1";

        /**
         * 最大结果窗口，默认 1000000
         */
        private Integer maxResultWindow = 1000000;
    }
}
