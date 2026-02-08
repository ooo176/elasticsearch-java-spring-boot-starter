package ooo.github.io.es.util;

import co.elastic.clients.elasticsearch._types.mapping.*;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.es.anno.Type;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 类型映射构建器
 * 根据 Java 类的注解自动生成 Elasticsearch 索引映射
 *
 * @author kaiqin
 */
@Slf4j
public class TypeMappingBuilder {

    /**
     * 根据类构建类型映射
     *
     * @param clazz 文档类
     * @param <T>   泛型
     * @return 类型映射
     */
    public static <T> TypeMapping mapBuilder(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("类不能为空");
        }

        TypeMapping.Builder builder = new TypeMapping.Builder();
        
        // 处理当前类的字段
        processFields(clazz, builder);
        
        // 递归处理父类的字段
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && !superClass.equals(Object.class)) {
            processFields(superClass, builder);
            superClass = superClass.getSuperclass();
        }

        return builder.build();
    }

    /**
     * 处理类的字段
     *
     * @param clazz   类
     * @param builder 映射构建器
     */
    private static void processFields(Class<?> clazz, TypeMapping.Builder builder) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Type esType = field.getAnnotation(Type.class);
            if (esType != null) {
                String fieldName = field.getName();
                typeMapping(builder, fieldName, esType);
            }
        }
    }

    /**
     * 构建字段类型映射
     * 支持的类型：text, keyword, geo_point, long, integer, double, float, boolean, date
     *
     * @param builder   构造器
     * @param fieldName 字段名
     * @param esType    类型注解
     */
    private static void typeMapping(TypeMapping.Builder builder, String fieldName, Type esType) {
        if (builder == null || StringUtils.isEmpty(fieldName) || esType == null) {
            return;
        }

        String[] types = esType.type();
        if (types == null || types.length == 0) {
            log.warn("字段 {} 的类型配置为空，使用默认类型", fieldName);
            return;
        }

        // 如果配置了多个类型，使用第一个作为主类型，其他作为多字段
        String primaryType = types[0];
        Property.Builder propertyBuilder = new Property.Builder();

        switch (primaryType.toLowerCase()) {
            case "text":
                TextProperty.Builder textBuilder = new TextProperty.Builder();
                if (StringUtils.hasText(esType.analyzer())) {
                    textBuilder.analyzer(esType.analyzer());
                }
                propertyBuilder.text(textBuilder.build());
                break;
            case "keyword":
                KeywordProperty.Builder keywordBuilder = new KeywordProperty.Builder();
                propertyBuilder.keyword(keywordBuilder.build());
                break;
            case "geo_point":
                propertyBuilder.geoPoint(new GeoPointProperty.Builder().build());
                break;
            case "long":
                propertyBuilder.long_(new LongNumberProperty.Builder().build());
                break;
            case "integer":
            case "int":
                propertyBuilder.integer(new IntegerNumberProperty.Builder().build());
                break;
            case "double":
                propertyBuilder.double_(new DoubleNumberProperty.Builder().build());
                break;
            case "float":
                propertyBuilder.float_(new FloatNumberProperty.Builder().build());
                break;
            case "boolean":
            case "bool":
                // 注意：Elasticsearch Java API Client 中 boolean 类型可能需要使用其他方法
                // 如果 bool 方法不存在，可以暂时使用 text 类型或移除此类型支持
                log.warn("boolean 类型暂不支持自动映射，字段: {}, 请手动配置", fieldName);
                propertyBuilder.text(new TextProperty.Builder().build());
                break;
            case "date":
                DateProperty.Builder dateBuilder = new DateProperty.Builder();
                propertyBuilder.date(dateBuilder.build());
                break;
            default:
                log.warn("不支持的字段类型: {}, 字段: {}, 使用默认 text 类型", primaryType, fieldName);
                propertyBuilder.text(new TextProperty.Builder().build());
                break;
        }

        Property property = propertyBuilder.build();
        builder.properties(fieldName, property);

        // 如果配置了多个类型，添加 keyword 作为子字段（用于 text 类型）
        if (types.length > 1 && Objects.equals(primaryType, "text")) {
            for (int i = 1; i < types.length; i++) {
                if (Objects.equals(types[i], "keyword")) {
                    // 添加 keyword 子字段
                    Property keywordProperty = new Property.Builder()
                            .keyword(new KeywordProperty.Builder().build())
                            .build();
                    builder.properties(fieldName + ".keyword", keywordProperty);
                    log.debug("为字段 {} 添加 keyword 子字段", fieldName);
                }
            }
        }
    }

}
