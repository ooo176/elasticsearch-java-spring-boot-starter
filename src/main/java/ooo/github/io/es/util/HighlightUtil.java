package ooo.github.io.es.util;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 高亮工具类
 * 用于将 Elasticsearch 返回的高亮结果替换到文档对象中
 *
 * @author kaiqin
 */
@Slf4j
public class HighlightUtil {

    /**
     * 高亮替换
     * 递归查找字段（包括当前类和所有父类），将高亮结果替换到文档对象中
     *
     * @param searchResponse 搜索响应结果
     * @param <T>            文档类型
     */
    public static <T> void convert(SearchResponse<T> searchResponse) {
        if (searchResponse == null) {
            log.warn("搜索响应结果为空，跳过高亮处理");
            return;
        }

        List<Hit<T>> hits = searchResponse.hits().hits();
        if (CollectionUtils.isEmpty(hits)) {
            log.debug("搜索结果为空，跳过高亮处理");
            return;
        }

        for (Hit<T> hit : hits) {
            T source = hit.source();
            if (source == null) {
                log.debug("文档源数据为空，跳过高亮处理");
                continue;
            }

            Map<String, List<String>> highlight = hit.highlight();
            if (CollectionUtils.isEmpty(highlight)) {
                continue;
            }

            for (Map.Entry<String, List<String>> entry : highlight.entrySet()) {
                String fieldName = entry.getKey();
                List<String> highlightValues = entry.getValue();
                if (CollectionUtils.isEmpty(highlightValues)) {
                    continue;
                }

                try {
                    Field field = findField(source.getClass(), fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        // 取第一个高亮值
                        field.set(source, highlightValues.get(0));
                        log.debug("高亮替换成功, 字段: {}, 类: {}", fieldName, source.getClass().getSimpleName());
                    } else {
                        log.warn("未找到字段: {}, 类: {}", fieldName, source.getClass().getName());
                    }
                } catch (IllegalAccessException e) {
                    log.error("设置高亮字段失败, 字段: {}, 类: {}", fieldName, source.getClass().getName(), e);
                }
            }
        }
    }

    /**
     * 递归查找字段（包括当前类和所有父类）
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段对象，如果未找到返回 null
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }

        // 在当前类中查找
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果当前类未找到，递归查找父类
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !superClass.equals(Object.class)) {
                return findField(superClass, fieldName);
            }
        }

        return null;
    }

}
