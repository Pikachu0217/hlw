package com.hlw.common.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * JSON 序列化工具类。
 */
@Slf4j
public final class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonUtil() {
    }

    /**
     * 获取全局 ObjectMapper。
     *
     * @return ObjectMapper 实例
     */
    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将 JSON 字符串反序列化为指定对象。
     *
     * @param jsonStr JSON 字符串
     * @param clazz 对象类型
     * @param <T> 对象类型
     * @return 反序列化对象
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        if (!StringUtils.hasText(jsonStr) || clazz == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonStr, clazz);
        } catch (Exception exception) {
            log.error("JSON 反序列化失败，class={}", clazz.getName(), exception);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为泛型对象。
     *
     * @param jsonStr JSON 字符串
     * @param valueTypeRef 泛型类型引用
     * @param <T> 对象类型
     * @return 反序列化对象
     */
    public static <T> T fromJson(String jsonStr, TypeReference<T> valueTypeRef) {
        if (!StringUtils.hasText(jsonStr) || valueTypeRef == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonStr, valueTypeRef);
        } catch (Exception exception) {
            log.error("JSON 泛型反序列化失败，type={}", valueTypeRef.getType(), exception);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为一层泛型对象。
     *
     * @param jsonStr JSON 字符串
     * @param rawClass 外层对象类型
     * @param parameterClass 泛型参数类型
     * @param <T> 对象类型
     * @return 反序列化对象
     */
    public static <T> T fromJson(String jsonStr, Class<T> rawClass, Class<?> parameterClass) {
        if (!StringUtils.hasText(jsonStr) || rawClass == null || parameterClass == null) {
            return null;
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(rawClass, parameterClass);
        return fromJson(jsonStr, javaType);
    }

    /**
     * 将 JSON 字符串反序列化为指定 JavaType。
     *
     * @param jsonStr JSON 字符串
     * @param javaType Jackson JavaType
     * @param <T> 对象类型
     * @return 反序列化对象
     */
    public static <T> T fromJson(String jsonStr, JavaType javaType) {
        if (!StringUtils.hasText(jsonStr) || javaType == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonStr, javaType);
        } catch (Exception exception) {
            log.error("JSON JavaType 反序列化失败，type={}", javaType, exception);
            return null;
        }
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param object 待序列化对象
     * @return JSON 字符串
     */
    public static String toJsonString(Object object) {
        return toJson(object, false);
    }

    /**
     * 将对象序列化为格式化 JSON 字符串。
     *
     * @param object 待序列化对象
     * @return 格式化 JSON 字符串
     */
    public static String toJsonStringWithDefaultPrettyPrinter(Object object) {
        return toJson(object, true);
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param object 待序列化对象
     * @param prettyPrinter 是否格式化输出
     * @return JSON 字符串
     */
    public static String toJson(Object object, boolean prettyPrinter) {
        if (object == null) {
            return null;
        }
        try {
            if (prettyPrinter) {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            }
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            log.error("JSON 序列化失败，class={}", object.getClass().getName(), exception);
            return null;
        }
    }

    /**
     * 创建 JSON 对象映射器。
     *
     * @return ObjectMapper 实例
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
