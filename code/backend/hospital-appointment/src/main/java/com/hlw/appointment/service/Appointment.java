package com.hlw.appointment.service;

public record Appointment(Long id, Long patientId, Long departmentId, Long doctorId, AppointmentStatus status) {
    /**
     * 创建待抢单便民门诊预约。
     *
     * @param id 预约编号
     * @param patientId 患者编号
     * @param departmentId 科室编号
     * @return 待抢单预约
     */
    public static Appointment convenient(Long id, Long patientId, Long departmentId) {
        return new Appointment(id, patientId, departmentId, null, AppointmentStatus.WAITING_GRAB);
    }

    /**
     * 返回被医生抢单后的预约副本。
     *
     * @param doctorId 医生编号
     * @return 已抢单预约
     */
    public Appointment grabbedBy(Long doctorId) {
        return new Appointment(id, patientId, departmentId, doctorId, AppointmentStatus.GRABBED);
    }
}
