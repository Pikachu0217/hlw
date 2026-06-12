package com.hlw.common.core.domain;

import java.util.List;

public record PageResult<T>(List<T> records, long total, long pageNum, long pageSize) {
}
