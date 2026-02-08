package ooo.github.io.es.exception;

/**
 * Elasticsearch 操作异常
 *
 * @author kaiqin
 */
public class ElasticsearchException extends RuntimeException {

    public ElasticsearchException(String message) {
        super(message);
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
