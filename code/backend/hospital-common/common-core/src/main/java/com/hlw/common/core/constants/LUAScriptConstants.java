package com.hlw.common.core.constants;

/**
 * Lua 脚本常量集合。
 */
public final class LUAScriptConstants {

    private LUAScriptConstants() {
    }

    /**
     * Redis 延迟队列消息消费 Lua 脚本：原子获取并删除指定 score 区间内的成员。
     */
    public static final String REDIS_DELAY_QUEUE_MESSAGE_CONSUME = ""
            + "local messages = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2]); "
            + "if (#messages > 0) then "
            + "    redis.call('zremrangebyscore', KEYS[1], ARGV[1], ARGV[2]); "
            + "end; "
            + "return messages;";
}
