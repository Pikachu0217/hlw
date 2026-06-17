package com.hlw.common.redis.service;

import com.hlw.common.core.util.JsonUtil;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 通用工具服务。
 */
@Slf4j
@Service
public class RedisService {
    private static final Long RELEASE_SUCCESS = 1L;
    private static final RedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>("""
        if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
        else
            return 0
        end
        """, Long.class);

    // Redis 操作模板。
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 构造 Redis 通用工具服务。
     *
     * @param redisTemplate Redis 操作模板
     */
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发布 Redis 订阅消息。
     *
     * @param channel 频道名称
     * @param message 消息内容
     * @return 是否发布成功
     */
    public boolean publish(String channel, String message) {
        if (!StringUtils.hasText(channel)) {
            log.warn("Redis 发布消息失败，频道为空");
            return false;
        }
        try {
            redisTemplate.convertAndSend(channel, message);
            return true;
        } catch (Exception exception) {
            log.error("Redis 发布消息异常，channel={}", channel, exception);
            return false;
        }
    }

    /**
     * 执行 Lua 脚本。
     *
     * @param script Redis 脚本
     * @param keys 键列表
     * @param args 参数列表
     * @param <T> 返回值类型
     * @return 脚本执行结果
     */
    public <T> T executeScript(RedisScript<T> script, List<String> keys, Object... args) {
        try {
            return redisTemplate.execute(script, keys, args);
        } catch (Exception exception) {
            log.error("Redis 执行脚本异常，keys={}", keys, exception);
            return null;
        }
    }

    /**
     * 写入字符串缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @return 是否写入成功
     */
    public boolean set(String key, String value) {
        return set(key, value, null);
    }

    /**
     * 写入带过期时间的字符串缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否写入成功
     */
    public boolean set(String key, String value, long timeout, TimeUnit unit) {
        if (unit == null) {
            log.warn("Redis 写入缓存失败，时间单位为空，key={}", key);
            return false;
        }
        return set(key, value, Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * 写入带过期时间的字符串缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时长
     * @return 是否写入成功
     */
    public boolean set(String key, String value, Duration ttl) {
        if (!StringUtils.hasText(key)) {
            log.warn("Redis 写入缓存失败，key 为空");
            return false;
        }
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
                operations.set(key, value, ttl);
            } else {
                operations.set(key, value);
            }
            return true;
        } catch (Exception exception) {
            log.error("Redis 写入缓存异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 写入对象缓存，值会序列化为 JSON。
     *
     * @param key 缓存键
     * @param value 缓存对象
     * @return 是否写入成功
     */
    public boolean setObject(String key, Object value) {
        return setObject(key, value, null);
    }

    /**
     * 写入带过期时间的对象缓存，值会序列化为 JSON。
     *
     * @param key 缓存键
     * @param value 缓存对象
     * @param ttl 过期时长
     * @return 是否写入成功
     */
    public boolean setObject(String key, Object value, Duration ttl) {
        if (value == null) {
            log.warn("Redis 写入对象缓存失败，对象为空，key={}", key);
            return false;
        }
        return set(key, JsonUtil.toJsonString(value), ttl);
    }

    /**
     * 获取字符串缓存。
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public String get(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception exception) {
            log.error("Redis 读取缓存异常，key={}", key, exception);
            return null;
        }
    }

    /**
     * 获取对象缓存。
     *
     * @param key 缓存键
     * @param clazz 对象类型
     * @param <T> 对象类型
     * @return 缓存对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String value = get(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return JsonUtil.fromJson(value, clazz);
    }

    /**
     * 批量获取字符串缓存。
     *
     * @param keys 缓存键列表
     * @return 缓存值列表
     */
    public List<String> multiGet(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = redisTemplate.opsForValue().multiGet(new ArrayList<>(keys));
            return values == null ? Collections.emptyList() : values;
        } catch (Exception exception) {
            log.error("Redis 批量读取缓存异常，keys={}", keys, exception);
            return Collections.emptyList();
        }
    }

    /**
     * 仅当键不存在时写入字符串缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时长
     * @return 是否写入成功
     */
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        try {
            Boolean success;
            if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
                success = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
            } else {
                success = redisTemplate.opsForValue().setIfAbsent(key, value);
            }
            return Boolean.TRUE.equals(success);
        } catch (Exception exception) {
            log.error("Redis setIfAbsent 异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 递增缓存值。
     *
     * @param key 缓存键
     * @param delta 递增步长
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception exception) {
            log.error("Redis 递增异常，key={}", key, exception);
            return null;
        }
    }

    /**
     * 递减缓存值。
     *
     * @param key 缓存键
     * @param delta 递减步长
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return increment(key, -delta);
    }

    /**
     * 设置键过期时间。
     *
     * @param key 缓存键
     * @param ttl 过期时长
     * @return 是否设置成功
     */
    public boolean expire(String key, Duration ttl) {
        if (!StringUtils.hasText(key) || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, ttl));
        } catch (Exception exception) {
            log.error("Redis 设置过期时间异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 查询键剩余过期时间。
     *
     * @param key 缓存键
     * @return 剩余过期时间，单位秒
     */
    public Long getExpire(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception exception) {
            log.error("Redis 查询过期时间异常，key={}", key, exception);
            return null;
        }
    }

    /**
     * 判断缓存键是否存在。
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception exception) {
            log.error("Redis 判断键存在异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 删除缓存键。
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception exception) {
            log.error("Redis 删除缓存异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 批量删除缓存键。
     *
     * @param keys 缓存键列表
     * @return 删除数量
     */
    public Long delete(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return 0L;
        }
        try {
            return redisTemplate.delete(keys);
        } catch (Exception exception) {
            log.error("Redis 批量删除缓存异常，keys={}", keys, exception);
            return 0L;
        }
    }

    /**
     * 扫描匹配模式的缓存键。
     *
     * @param pattern 匹配模式
     * @param count 每批扫描数量
     * @return 缓存键集合
     */
    public Set<String> scan(String pattern, long count) {
        if (!StringUtils.hasText(pattern)) {
            return Collections.emptySet();
        }
        Set<String> keys = new LinkedHashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                }
                return null;
            });
            return keys;
        } catch (Exception exception) {
            log.error("Redis 扫描键异常，pattern={}", pattern, exception);
            return Collections.emptySet();
        }
    }

    /**
     * 写入 Hash 字段。
     *
     * @param key 缓存键
     * @param field 字段名
     * @param value 字段值
     * @return 是否写入成功
     */
    public boolean hSet(String key, String field, String value) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(field)) {
            return false;
        }
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception exception) {
            log.error("Redis 写入 Hash 异常，key={}, field={}", key, field, exception);
            return false;
        }
    }

    /**
     * 批量写入 Hash 字段。
     *
     * @param key 缓存键
     * @param values 字段值映射
     * @return 是否写入成功
     */
    public boolean hSetAll(String key, Map<String, String> values) {
        if (!StringUtils.hasText(key) || CollectionUtils.isEmpty(values)) {
            return false;
        }
        try {
            redisTemplate.opsForHash().putAll(key, values);
            return true;
        } catch (Exception exception) {
            log.error("Redis 批量写入 Hash 异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 获取 Hash 字段。
     *
     * @param key 缓存键
     * @param field 字段名
     * @return 字段值
     */
    public String hGet(String key, String field) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(field)) {
            return null;
        }
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            return value == null ? null : String.valueOf(value);
        } catch (Exception exception) {
            log.error("Redis 读取 Hash 异常，key={}, field={}", key, field, exception);
            return null;
        }
    }

    /**
     * 获取 Hash 全部字段。
     *
     * @param key 缓存键
     * @return 字段值映射
     */
    public Map<Object, Object> hGetAll(String key) {
        if (!StringUtils.hasText(key)) {
            return Collections.emptyMap();
        }
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception exception) {
            log.error("Redis 读取全部 Hash 异常，key={}", key, exception);
            return Collections.emptyMap();
        }
    }

    /**
     * 批量获取 Hash 字段。
     *
     * @param key 缓存键
     * @param fields 字段列表
     * @return 字段值列表
     */
    public List<Object> hMultiGet(String key, Collection<String> fields) {
        if (!StringUtils.hasText(key) || CollectionUtils.isEmpty(fields)) {
            return Collections.emptyList();
        }
        try {
            HashOperations<String, Object, Object> operations = redisTemplate.opsForHash();
            return operations.multiGet(key, new ArrayList<>(fields));
        } catch (Exception exception) {
            log.error("Redis 批量读取 Hash 异常，key={}, fields={}", key, fields, exception);
            return Collections.emptyList();
        }
    }

    /**
     * 删除 Hash 字段。
     *
     * @param key 缓存键
     * @param fields 字段列表
     * @return 删除数量
     */
    public Long hDelete(String key, Object... fields) {
        if (!StringUtils.hasText(key) || fields == null || fields.length == 0) {
            return 0L;
        }
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception exception) {
            log.error("Redis 删除 Hash 字段异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 判断 Hash 字段是否存在。
     *
     * @param key 缓存键
     * @param field 字段名
     * @return 是否存在
     */
    public boolean hHasKey(String key, String field) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(field)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, field));
        } catch (Exception exception) {
            log.error("Redis 判断 Hash 字段异常，key={}, field={}", key, field, exception);
            return false;
        }
    }

    /**
     * 递增 Hash 字段值。
     *
     * @param key 缓存键
     * @param field 字段名
     * @param delta 递增步长
     * @return 递增后的值
     */
    public Long hIncrement(String key, String field, long delta) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(field)) {
            return null;
        }
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception exception) {
            log.error("Redis 递增 Hash 字段异常，key={}, field={}", key, field, exception);
            return null;
        }
    }

    /**
     * 添加 Set 元素。
     *
     * @param key 缓存键
     * @param values 元素列表
     * @return 添加数量
     */
    public Long sAdd(String key, String... values) {
        if (!StringUtils.hasText(key) || values == null || values.length == 0) {
            return 0L;
        }
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception exception) {
            log.error("Redis 添加 Set 元素异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 获取 Set 全部元素。
     *
     * @param key 缓存键
     * @return 元素集合
     */
    public Set<String> sMembers(String key) {
        if (!StringUtils.hasText(key)) {
            return Collections.emptySet();
        }
        try {
            Set<String> values = redisTemplate.opsForSet().members(key);
            return values == null ? Collections.emptySet() : values;
        } catch (Exception exception) {
            log.error("Redis 获取 Set 元素异常，key={}", key, exception);
            return Collections.emptySet();
        }
    }

    /**
     * 判断 Set 是否包含元素。
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 是否包含
     */
    public boolean sIsMember(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception exception) {
            log.error("Redis 判断 Set 元素异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 删除 Set 元素。
     *
     * @param key 缓存键
     * @param values 元素列表
     * @return 删除数量
     */
    public Long sRemove(String key, Object... values) {
        if (!StringUtils.hasText(key) || values == null || values.length == 0) {
            return 0L;
        }
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception exception) {
            log.error("Redis 删除 Set 元素异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 获取 Set 元素数量。
     *
     * @param key 缓存键
     * @return 元素数量
     */
    public Long sSize(String key) {
        if (!StringUtils.hasText(key)) {
            return 0L;
        }
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception exception) {
            log.error("Redis 获取 Set 数量异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 求两个 Set 的交集。
     *
     * @param leftKey 左侧缓存键
     * @param rightKey 右侧缓存键
     * @return 交集元素
     */
    public Set<String> sIntersect(String leftKey, String rightKey) {
        if (!StringUtils.hasText(leftKey) || !StringUtils.hasText(rightKey)) {
            return Collections.emptySet();
        }
        try {
            SetOperations<String, String> operations = redisTemplate.opsForSet();
            Set<String> values = operations.intersect(leftKey, rightKey);
            return values == null ? Collections.emptySet() : values;
        } catch (Exception exception) {
            log.error("Redis 求 Set 交集异常，leftKey={}, rightKey={}", leftKey, rightKey, exception);
            return Collections.emptySet();
        }
    }

    /**
     * 左侧推入 List 元素。
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 推入后列表长度
     */
    public Long lLeftPush(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return 0L;
        }
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception exception) {
            log.error("Redis 左侧推入 List 异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 右侧推入 List 元素。
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 推入后列表长度
     */
    public Long lRightPush(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return 0L;
        }
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception exception) {
            log.error("Redis 右侧推入 List 异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 查询 List 范围元素。
     *
     * @param key 缓存键
     * @param start 起始下标
     * @param end 结束下标
     * @return 元素列表
     */
    public List<String> lRange(String key, long start, long end) {
        if (!StringUtils.hasText(key)) {
            return Collections.emptyList();
        }
        try {
            ListOperations<String, String> operations = redisTemplate.opsForList();
            List<String> values = operations.range(key, start, end);
            return values == null ? Collections.emptyList() : values;
        } catch (Exception exception) {
            log.error("Redis 查询 List 范围异常，key={}", key, exception);
            return Collections.emptyList();
        }
    }

    /**
     * 左侧弹出 List 元素。
     *
     * @param key 缓存键
     * @return 元素值
     */
    public String lLeftPop(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception exception) {
            log.error("Redis 左侧弹出 List 异常，key={}", key, exception);
            return null;
        }
    }

    /**
     * 右侧弹出 List 元素。
     *
     * @param key 缓存键
     * @return 元素值
     */
    public String lRightPop(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception exception) {
            log.error("Redis 右侧弹出 List 异常，key={}", key, exception);
            return null;
        }
    }

    /**
     * 获取 List 长度。
     *
     * @param key 缓存键
     * @return 列表长度
     */
    public Long lSize(String key) {
        if (!StringUtils.hasText(key)) {
            return 0L;
        }
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception exception) {
            log.error("Redis 获取 List 长度异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 添加 ZSet 元素。
     *
     * @param key 缓存键
     * @param value 元素值
     * @param score 分数
     * @return 是否添加成功
     */
    public boolean zAdd(String key, String value, double score) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        } catch (Exception exception) {
            log.error("Redis 添加 ZSet 元素异常，key={}", key, exception);
            return false;
        }
    }

    /**
     * 查询 ZSet 正序范围元素。
     *
     * @param key 缓存键
     * @param start 起始下标
     * @param end 结束下标
     * @return 元素集合
     */
    public Set<String> zRange(String key, long start, long end) {
        if (!StringUtils.hasText(key)) {
            return Collections.emptySet();
        }
        try {
            Set<String> values = redisTemplate.opsForZSet().range(key, start, end);
            return values == null ? Collections.emptySet() : values;
        } catch (Exception exception) {
            log.error("Redis 查询 ZSet 正序范围异常，key={}", key, exception);
            return Collections.emptySet();
        }
    }

    /**
     * 查询 ZSet 倒序范围元素。
     *
     * @param key 缓存键
     * @param start 起始下标
     * @param end 结束下标
     * @return 元素集合
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        if (!StringUtils.hasText(key)) {
            return Collections.emptySet();
        }
        try {
            Set<String> values = redisTemplate.opsForZSet().reverseRange(key, start, end);
            return values == null ? Collections.emptySet() : values;
        } catch (Exception exception) {
            log.error("Redis 查询 ZSet 倒序范围异常，key={}", key, exception);
            return Collections.emptySet();
        }
    }

    /**
     * 按分数区间查询 ZSet 元素。
     *
     * @param key 缓存键
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 元素集合
     */
    public Set<String> zRangeByScore(String key, double minScore, double maxScore) {
        if (!StringUtils.hasText(key)) {
            return Collections.emptySet();
        }
        try {
            Set<String> values = redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
            return values == null ? Collections.emptySet() : values;
        } catch (Exception exception) {
            log.error("Redis 按分数查询 ZSet 异常，key={}", key, exception);
            return Collections.emptySet();
        }
    }

    /**
     * 获取 ZSet 元素分数。
     *
     * @param key 缓存键
     * @param value 元素值
     * @return 元素分数
     */
    public Double zScore(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception exception) {
            log.error("Redis 获取 ZSet 分数异常，key={}", key, exception);
            return null;
        }
    }

    /**
     * 删除 ZSet 元素。
     *
     * @param key 缓存键
     * @param values 元素列表
     * @return 删除数量
     */
    public Long zRemove(String key, Object... values) {
        if (!StringUtils.hasText(key) || values == null || values.length == 0) {
            return 0L;
        }
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception exception) {
            log.error("Redis 删除 ZSet 元素异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 获取 ZSet 元素数量。
     *
     * @param key 缓存键
     * @return 元素数量
     */
    public Long zSize(String key) {
        if (!StringUtils.hasText(key)) {
            return 0L;
        }
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception exception) {
            log.error("Redis 获取 ZSet 数量异常，key={}", key, exception);
            return 0L;
        }
    }

    /**
     * 尝试获取分布式锁。
     *
     * @param key 锁键
     * @param requestId 请求标识
     * @param ttl 锁过期时长
     * @return 是否获取成功
     */
    public boolean tryLock(String key, String requestId, Duration ttl) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(requestId)) {
            return false;
        }
        return setIfAbsent(key, requestId, ttl);
    }

    /**
     * 释放分布式锁，仅释放匹配请求标识的锁。
     *
     * @param key 锁键
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public boolean releaseLock(String key, String requestId) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(requestId)) {
            return false;
        }
        try {
            Long result = redisTemplate.execute(RELEASE_LOCK_SCRIPT, Collections.singletonList(key), requestId);
            return Objects.equals(RELEASE_SUCCESS, result);
        } catch (Exception exception) {
            log.error("Redis 释放分布式锁异常，key={}", key, exception);
            return false;
        }
    }
}
