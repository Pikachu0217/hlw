package com.hlw.consult.service;

/**
 * 问诊消息类型常量。
 */
public final class ConsultMessageType {
    /** 文本消息。 */
    public static final String TEXT = "TEXT";
    /** 图片消息。 */
    public static final String IMAGE = "IMAGE";
    /** 系统消息。 */
    public static final String SYSTEM = "SYSTEM";

    /**
     * 私有构造方法，防止实例化常量类。
     */
    private ConsultMessageType() {
    }

    /**
     * 判断消息类型是否允许发送。
     *
     * @param value 消息类型
     * @return 是否允许
     */
    public static boolean isSendable(String value) {
        return TEXT.equals(value) || IMAGE.equals(value);
    }
}
