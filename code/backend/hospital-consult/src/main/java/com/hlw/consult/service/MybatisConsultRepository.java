package com.hlw.consult.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.consult.entity.ConConsultEntity;
import com.hlw.consult.mapper.ConConsultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis 问诊仓储实现。
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MybatisConsultRepository implements ConsultRepository {
    private final ConConsultMapper conConsultMapper;

    @Override
    public void save(Consult consult) {
        ConConsultEntity entity = conConsultMapper.selectById(consult.id());
        if (entity == null) {
            log.warn("未找到问诊单，无法更新状态，consultId={}", consult.id());
            return;
        }
        entity.setStatus(consult.status().dbValue());
        entity.setDurationLimit(consult.durationLimit());
        entity.setRemainingSeconds(consult.remainingSeconds());
        conConsultMapper.updateById(entity);
    }

    @Override
    public Consult findById(Long consultId) {
        ConConsultEntity entity = conConsultMapper.selectById(consultId);
        if (entity == null) {
            return null;
        }
        return toConsult(entity);
    }

    @Override
    public List<Consult> findInProgress() {
        return conConsultMapper.selectList(new LambdaQueryWrapper<ConConsultEntity>()
                .eq(ConConsultEntity::getDeleted, 0)
                .eq(ConConsultEntity::getStatus, ConsultStatus.IN_PROGRESS.dbValue()))
                .stream()
                .map(this::toConsult)
                .toList();
    }

    private Consult toConsult(ConConsultEntity entity) {
        ConsultStatus status = ConsultStatus.fromDbValue(entity.getStatus());
        return new Consult(
                entity.getId(),
                entity.getTenantId(),
                status,
                entity.getDurationLimit() != null ? entity.getDurationLimit() : 30,
                entity.getRemainingSeconds() != null ? entity.getRemainingSeconds() : 0
        );
    }
}
