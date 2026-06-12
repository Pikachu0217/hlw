package com.hlw.drug.service;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 药品配送服务，负责发货并发送配送事件。
 */
public class DrugDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(DrugDeliveryService.class);

    private final MqProducer mqProducer;

    /**
     * 构造药品配送服务。
     *
     * @param mqProducer 消息生产者
     */
    public DrugDeliveryService(MqProducer mqProducer) {
        this.mqProducer = mqProducer;
    }

    /**
     * 发货并发送配送完成事件。
     *
     * @param deliveryId 配送单编号
     */
    public void ship(Long deliveryId) {
        log.info("药品配送发货，deliveryId={}", deliveryId);
        mqProducer.publish(new MqMessage("drug.shipped", "{\"deliveryId\":" + deliveryId + "}", 0, 0, 3));
        log.info("药品配送事件已发布，deliveryId={}", deliveryId);
    }
}
