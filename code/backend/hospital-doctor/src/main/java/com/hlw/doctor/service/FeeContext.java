package com.hlw.doctor.service;

import java.math.BigDecimal;

public record FeeContext(String title, BigDecimal doctorFee, BigDecimal departmentFee) {
}
