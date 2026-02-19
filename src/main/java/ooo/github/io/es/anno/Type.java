package ooo.github.io.es.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Elasticsearch 字段类型注解
 * 用于定义字段在 Elasticsearch 中的映射类型和配置
 *
 * @author kaiqin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Type {

    /**
     * 字段类型，支持多个类型时第一个为主类型，其他作为多字段
     * 支持的类型：
     * - 文本类型：text, keyword, search_as_you_type
     * - 数值类型：long, integer, short, byte, double, float, half_float, scaled_float
     * - 布尔类型：boolean
     * - 日期类型：date
     * - 对象类型：object, nested
     * - 地理类型：geo_point, geo_shape
     * - 特殊类型：ip, completion, token_count, percolator, join, rank_feature, rank_features, dense_vector, sparse_vector
     */
    String[] type() default {"text", "keyword"};

    /**
     * 复制到其他字段
     */
    String copyTo() default "";

    /**
     * 关联的类（用于 object/nested 类型）
     */
    Class<?> clazz() default Object.class;

    /**
     * 分词器（用于 text、token_count 类型）
     */
    String analyzer() default "";

    /**
     * 搜索分词器（用于 text 类型）
     */
    String searchAnalyzer() default "";

    /**
     * 日期格式（用于 date 类型）
     * 例如：yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis
     */
    String format() default "";

    /**
     * 缩放因子（用于 scaled_float 类型）
     * 例如：100 表示实际值 × 100 后存储
     */
    double scalingFactor() default 1.0;

    /**
     * 向量维度（用于 dense_vector 类型）
     */
    int dims() default 128;

    /**
     * 父子关系（用于 join 类型）
     * 格式：parent:child1,child2 或 parent1:child1;parent2:child2
     * 例如：question:answer 或 question:answer;comment:reply
     */
    String relations() default "";

    /**
     * 忽略超过指定字符数的值（用于 keyword 类型）
     * 默认值 0 表示使用 Elasticsearch 默认值 256
     */
    int ignoreAbove() default 0;
}
