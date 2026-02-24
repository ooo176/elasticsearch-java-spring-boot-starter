package ooo.github.io.es.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ooo.github.io.es.anno.Id;
import ooo.github.io.es.anno.IndexName;
import ooo.github.io.es.anno.Type;

import java.io.Serializable;
import java.util.Date;

/**
 * 统一资源检索索引 dp_resource_data 对应的文档实体
 * <p>
 * 与《业务数据接入ES技术方案》2.2.5 Mapping 一致：
 * 每条文档 = 一条「目录 + 业务行」组合；doc_id = {zy_id}_{sjkbm}_{业务主键} 作为 ES _id，biz_row_key = {sjkbm}_{业务主键} 用于结果去重。
 * </p>
 *
 * @author kxdigit
 */
@Data
@IndexName("dp_resource_data")
public class DpResourceDataDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    // ---------- 来自 dwd_qlzyml 的固定维度 ----------

    @JsonProperty("zy_id")
    @Type(type = {"keyword"})
    private String zyId;

    @JsonProperty("first_level")
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String firstLevel;

    @JsonProperty("second_level")
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String secondLevel;

    @JsonProperty("third_level")
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String thirdLevel;

    @JsonProperty("fourth_level")
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String fourthLevel;

    @JsonProperty("cnbm")
    @Type(type = {"keyword"})
    private String cnbm;

    @JsonProperty("sjkbm")
    @Type(type = {"keyword"})
    private String sjkbm;

    @JsonProperty("dwzdmc")
    @Type(type = {"keyword"})
    private String dwzdmc;

    @JsonProperty("zyzdmc")
    @Type(type = {"keyword"})
    private String zyzdmc;

    @JsonProperty("zyzdz")
    @Type(type = {"keyword"})
    private String zyzdz;

    @JsonProperty("sfyz")
    @Type(type = {"keyword"})
    private String sfyz;

    @JsonProperty("sfxzxy")
    @Type(type = {"keyword"})
    private String sfxzxy;

    // ---------- 业务表写入的检索与标识字段 ----------

    @JsonProperty("dwzd_value")
    @Type(type = {"text", "keyword"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String dwzdValue;

    @JsonProperty("keyword_text")
    @Type(type = {"text"}, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String keywordText;

    @JsonProperty("doc_id")
    @Id
    @Type(type = {"keyword"})
    private String docId;

    @JsonProperty("biz_row_key")
    @Type(type = {"keyword"})
    private String bizRowKey;

    @JsonProperty("row_id")
    @Type(type = {"keyword"})
    private String rowId;

    @JsonProperty("gxrq")
    @Type(type = {"date"})
    private Date gxrq;
}
