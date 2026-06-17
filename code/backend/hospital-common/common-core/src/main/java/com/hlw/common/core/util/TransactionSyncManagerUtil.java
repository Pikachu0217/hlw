package com.hlw.common.core.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务同步辅助工具，用于在事务提交后挂载回调任务。
 */
public final class TransactionSyncManagerUtil {

    private TransactionSyncManagerUtil() {
    }

    /**
     * 事务提交后执行回调；若当前无事务，则立即执行。
     *
     * @param runnable 回调任务
     */
    public static void afterCommit(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
            return;
        }
        runnable.run();
    }
}
