package ooo.github.io.es.util;

import co.elastic.clients.elasticsearch._types.mapping.*;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.es.anno.Type;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 支持的类型：
     * - 文本类型：text, keyword, search_as_you_type
     * - 数值类型：long, integer, short, byte, double, float, half_float, scaled_float
     * - 布尔类型：boolean
     * - 日期类型：date
     * - 对象类型：object, nested
     * - 地理类型：geo_point, geo_shape
     * - 特殊类型：ip, completion, token_count, percolator, join, rank_feature, rank_features, dense_vector, sparse_vector
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
            // ========== 文本类型 ==========
            case "text":
                TextProperty.Builder textBuilder = new TextProperty.Builder();
                if (StringUtils.hasText(esType.analyzer())) {
                    textBuilder.analyzer(esType.analyzer());
                }
                if (StringUtils.hasText(esType.searchAnalyzer())) {
                    textBuilder.searchAnalyzer(esType.searchAnalyzer());
                }
                propertyBuilder.text(textBuilder.build());
                break;
            case "keyword":
                KeywordProperty.Builder keywordBuilder = new KeywordProperty.Builder();
                if (esType.ignoreAbove() > 0) {
                    keywordBuilder.ignoreAbove(esType.ignoreAbove());
                }
                propertyBuilder.keyword(keywordBuilder.build());
                break;
            case "search_as_you_type":
                propertyBuilder.searchAsYouType(new SearchAsYouTypeProperty.Builder().build());
                break;
            
            // ========== 数值类型 - 整数 ==========
            case "long":
                propertyBuilder.long_(new LongNumberProperty.Builder().build());
                break;
            case "integer":
            case "int":
                propertyBuilder.integer(new IntegerNumberProperty.Builder().build());
                break;
            case "short":
                propertyBuilder.short_(new ShortNumberProperty.Builder().build());
                break;
            case "byte":
                propertyBuilder.byte_(new ByteNumberProperty.Builder().build());
                break;
            
            // ========== 数值类型 - 浮点数 ==========
            case "double":
                propertyBuilder.double_(new DoubleNumberProperty.Builder().build());
                break;
            case "float":
                propertyBuilder.float_(new FloatNumberProperty.Builder().build());
                break;
            case "half_float":
                propertyBuilder.halfFloat(new HalfFloatNumberProperty.Builder().build());
                break;
            case "scaled_float":
                ScaledFloatNumberProperty.Builder scaledFloatBuilder = new ScaledFloatNumberProperty.Builder();
                scaledFloatBuilder.scalingFactor(esType.scalingFactor());
                propertyBuilder.scaledFloat(scaledFloatBuilder.build());
                break;
            
            // ========== 布尔类型 ==========
            case "boolean":
            case "bool":
                propertyBuilder.boolean_(new BooleanProperty.Builder().build());
                break;
            
            // ========== 日期类型 ==========
            case "date":
                DateProperty.Builder dateBuilder = new DateProperty.Builder();
                if (StringUtils.hasText(esType.format())) {
                    dateBuilder.format(esType.format());
                }
                propertyBuilder.date(dateBuilder.build());
                break;
            
            // ========== 对象类型 ==========
            case "object":
                propertyBuilder.object(new ObjectProperty.Builder().build());
                break;
            case "nested":
                propertyBuilder.nested(new NestedProperty.Builder().build());
                break;
            
            // ========== 地理类型 ==========
            case "geo_point":
                propertyBuilder.geoPoint(new GeoPointProperty.Builder().build());
                break;
            case "geo_shape":
                propertyBuilder.geoShape(new GeoShapeProperty.Builder().build());
                break;
            
            // ========== 特殊类型 ==========
            case "ip":
                propertyBuilder.ip(new IpProperty.Builder().build());
                break;
            case "completion":
                propertyBuilder.completion(new CompletionProperty.Builder().build());
                break;
            case "token_count":
                TokenCountProperty.Builder tokenCountBuilder = new TokenCountProperty.Builder();
                // token_count 需要 analyzer 参数，如果注解中配置了则使用
                if (StringUtils.hasText(esType.analyzer())) {
                    tokenCountBuilder.analyzer(esType.analyzer());
                } else {
                    // 默认使用 standard analyzer
                    tokenCountBuilder.analyzer("standard");
                }
                propertyBuilder.tokenCount(tokenCountBuilder.build());
                break;
            case "percolator":
                propertyBuilder.percolator(new PercolatorProperty.Builder().build());
                break;
            case "join":
                JoinProperty.Builder joinBuilder = new JoinProperty.Builder();
                if (StringUtils.hasText(esType.relations())) {
                    // 解析 relations 字符串，格式：parent:child1,child2 或 parent1:child1;parent2:child2
                    // Elasticsearch 中 relations 的格式是 Map<String, String> 或 Map<String, List<String>>
                    String relationsStr = esType.relations();
                    Map<String, List<String>> relationsMap = new HashMap<>();
                    
                    if (relationsStr.contains(";")) {
                        // 多个关系，格式：parent1:child1;parent2:child2
                        String[] relationPairs = relationsStr.split(";");
                        for (String pair : relationPairs) {
                            if (pair.contains(":")) {
                                String[] parts = pair.split(":", 2);
                                if (parts.length == 2) {
                                    String parent = parts[0].trim();
                                    String[] children = parts[1].split(",");
                                    List<String> childList = new ArrayList<>();
                                    for (String child : children) {
                                        childList.add(child.trim());
                                    }
                                    relationsMap.put(parent, childList);
                                }
                            }
                        }
                    } else if (relationsStr.contains(":")) {
                        // 单个关系，格式：parent:child1,child2
                        String[] parts = relationsStr.split(":", 2);
                        if (parts.length == 2) {
                            String parent = parts[0].trim();
                            String[] children = parts[1].split(",");
                            List<String> childList = new ArrayList<>();
                            for (String child : children) {
                                childList.add(child.trim());
                            }
                            relationsMap.put(parent, childList);
                        }
                    }
                    
                    if (!relationsMap.isEmpty()) {
                        // 使用 relations 方法设置 Map<String, List<String>>
                        joinBuilder.relations(relationsMap);
                    }
                } else {
                    log.warn("字段 {} 使用 join 类型，但 relations 参数未配置，请手动配置", fieldName);
                }
                propertyBuilder.join(joinBuilder.build());
                break;
            case "rank_feature":
                propertyBuilder.rankFeature(new RankFeatureProperty.Builder().build());
                break;
            case "rank_features":
                propertyBuilder.rankFeatures(new RankFeaturesProperty.Builder().build());
                break;
            case "dense_vector":
                DenseVectorProperty.Builder denseVectorBuilder = new DenseVectorProperty.Builder();
                denseVectorBuilder.dims(esType.dims());
                propertyBuilder.denseVector(denseVectorBuilder.build());
                break;
            case "sparse_vector":
                // 注意：sparse_vector 类型在 Elasticsearch Java API Client 7.17 中可能不支持
                // 如果编译报错，请使用 object 类型代替或手动配置映射
                log.warn("字段 {} 使用 sparse_vector 类型，但 Java API Client 7.17 可能不支持，请使用 object 类型代替或手动配置映射", fieldName);
                // 使用 object 类型代替
                propertyBuilder.object(new ObjectProperty.Builder().build());
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
