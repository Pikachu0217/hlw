CREATE DATABASE hospital_auth;
CREATE DATABASE hospital_system;
CREATE DATABASE hospital_patient;
CREATE DATABASE hospital_doctor;
CREATE DATABASE hospital_consult;
CREATE DATABASE hospital_appointment;
CREATE DATABASE hospital_prescription;
CREATE DATABASE hospital_drug;
CREATE DATABASE hospital_order;

\connect hospital_system;

CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    tenant_name VARCHAR(128) NOT NULL,
    package_name VARCHAR(64) NOT NULL,
    admin_name VARCHAR(64) NOT NULL,
    expire_at DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_tenant IS '租户信息表';
COMMENT ON COLUMN sys_tenant.id IS '主键编号';
COMMENT ON COLUMN sys_tenant.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN sys_tenant.package_name IS '套餐名称';
COMMENT ON COLUMN sys_tenant.admin_name IS '管理员名称';
COMMENT ON COLUMN sys_tenant.expire_at IS '到期日期';
COMMENT ON COLUMN sys_tenant.status IS '租户状态';
COMMENT ON COLUMN sys_tenant.create_time IS '创建时间';
COMMENT ON COLUMN sys_tenant.update_time IS '更新时间';
COMMENT ON COLUMN sys_tenant.create_by IS '创建人编号';
COMMENT ON COLUMN sys_tenant.update_by IS '更新人编号';
COMMENT ON COLUMN sys_tenant.deleted IS '逻辑删除标识';

INSERT INTO sys_tenant (tenant_id, tenant_name, package_name, admin_name, expire_at, status)
VALUES
    (100, '海岚门诊', '标准医疗版', '刘院长', '2026-12-31', '正常'),
    (200, '青禾互联网医院', '集团旗舰版', '姜主任', '2026-08-16', '续费跟进')
ON CONFLICT DO NOTHING;

\connect hospital_auth;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    user_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键编号';
COMMENT ON COLUMN sys_user.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_user.username IS '登录账号';
COMMENT ON COLUMN sys_user.password IS '登录密码';
COMMENT ON COLUMN sys_user.phone IS '联系电话';
COMMENT ON COLUMN sys_user.user_type IS '用户类型';
COMMENT ON COLUMN sys_user.status IS '账号状态';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';
COMMENT ON COLUMN sys_user.create_by IS '创建人编号';
COMMENT ON COLUMN sys_user.update_by IS '更新人编号';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_role IS '系统角色表';
COMMENT ON COLUMN sys_role.id IS '主键编号';
COMMENT ON COLUMN sys_role.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.status IS '角色状态';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_role.create_by IS '创建人编号';
COMMENT ON COLUMN sys_role.update_by IS '更新人编号';
COMMENT ON COLUMN sys_role.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    menu_name VARCHAR(64) NOT NULL,
    permission VARCHAR(128) NOT NULL,
    route_path VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_menu IS '系统菜单表';
COMMENT ON COLUMN sys_menu.id IS '主键编号';
COMMENT ON COLUMN sys_menu.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.permission IS '权限标识';
COMMENT ON COLUMN sys_menu.route_path IS '路由路径';
COMMENT ON COLUMN sys_menu.status IS '菜单状态';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_menu.create_by IS '创建人编号';
COMMENT ON COLUMN sys_menu.update_by IS '更新人编号';
COMMENT ON COLUMN sys_menu.deleted IS '逻辑删除标识';

\connect hospital_doctor;

CREATE TABLE IF NOT EXISTS doc_doctor (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    title VARCHAR(64) NOT NULL,
    department VARCHAR(64) NOT NULL,
    specialty VARCHAR(256) NOT NULL,
    consult_fee NUMERIC(10, 2) NOT NULL,
    consult_status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE doc_doctor IS '医生信息表';
COMMENT ON COLUMN doc_doctor.id IS '主键编号';
COMMENT ON COLUMN doc_doctor.tenant_id IS '租户编号';
COMMENT ON COLUMN doc_doctor.doctor_name IS '医生姓名';
COMMENT ON COLUMN doc_doctor.title IS '医生职称';
COMMENT ON COLUMN doc_doctor.department IS '所属科室';
COMMENT ON COLUMN doc_doctor.specialty IS '擅长方向';
COMMENT ON COLUMN doc_doctor.consult_fee IS '问诊费用';
COMMENT ON COLUMN doc_doctor.consult_status IS '接诊状态';
COMMENT ON COLUMN doc_doctor.create_time IS '创建时间';
COMMENT ON COLUMN doc_doctor.update_time IS '更新时间';
COMMENT ON COLUMN doc_doctor.create_by IS '创建人编号';
COMMENT ON COLUMN doc_doctor.update_by IS '更新人编号';
COMMENT ON COLUMN doc_doctor.deleted IS '逻辑删除标识';

\connect hospital_patient;

CREATE TABLE IF NOT EXISTS pat_patient (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    gender VARCHAR(16) NOT NULL,
    age INT NOT NULL,
    phone VARCHAR(32) NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE pat_patient IS '患者档案表';
COMMENT ON COLUMN pat_patient.id IS '主键编号';
COMMENT ON COLUMN pat_patient.tenant_id IS '租户编号';
COMMENT ON COLUMN pat_patient.patient_name IS '患者姓名';
COMMENT ON COLUMN pat_patient.gender IS '患者性别';
COMMENT ON COLUMN pat_patient.age IS '患者年龄';
COMMENT ON COLUMN pat_patient.phone IS '联系电话';
COMMENT ON COLUMN pat_patient.risk_level IS '风险等级';
COMMENT ON COLUMN pat_patient.create_time IS '创建时间';
COMMENT ON COLUMN pat_patient.update_time IS '更新时间';
COMMENT ON COLUMN pat_patient.create_by IS '创建人编号';
COMMENT ON COLUMN pat_patient.update_by IS '更新人编号';
COMMENT ON COLUMN pat_patient.deleted IS '逻辑删除标识';

\connect hospital_appointment;

CREATE TABLE IF NOT EXISTS apt_appointment (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    appointment_no VARCHAR(64) NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    clinic_time VARCHAR(64) NOT NULL,
    source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE apt_appointment IS '预约单表';
COMMENT ON COLUMN apt_appointment.id IS '主键编号';
COMMENT ON COLUMN apt_appointment.tenant_id IS '租户编号';
COMMENT ON COLUMN apt_appointment.appointment_no IS '预约单号';
COMMENT ON COLUMN apt_appointment.patient_name IS '患者姓名';
COMMENT ON COLUMN apt_appointment.doctor_name IS '医生姓名';
COMMENT ON COLUMN apt_appointment.clinic_time IS '就诊时间';
COMMENT ON COLUMN apt_appointment.source IS '预约来源';
COMMENT ON COLUMN apt_appointment.status IS '预约状态';
COMMENT ON COLUMN apt_appointment.create_time IS '创建时间';
COMMENT ON COLUMN apt_appointment.update_time IS '更新时间';
COMMENT ON COLUMN apt_appointment.create_by IS '创建人编号';
COMMENT ON COLUMN apt_appointment.update_by IS '更新人编号';
COMMENT ON COLUMN apt_appointment.deleted IS '逻辑删除标识';

\connect hospital_consult;

CREATE TABLE IF NOT EXISTS con_consult (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    consult_no VARCHAR(64) NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE con_consult IS '问诊单表';
COMMENT ON COLUMN con_consult.id IS '主键编号';
COMMENT ON COLUMN con_consult.tenant_id IS '租户编号';
COMMENT ON COLUMN con_consult.consult_no IS '问诊单号';
COMMENT ON COLUMN con_consult.patient_name IS '患者姓名';
COMMENT ON COLUMN con_consult.doctor_name IS '医生姓名';
COMMENT ON COLUMN con_consult.channel IS '问诊渠道';
COMMENT ON COLUMN con_consult.status IS '问诊状态';
COMMENT ON COLUMN con_consult.create_time IS '创建时间';
COMMENT ON COLUMN con_consult.update_time IS '更新时间';
COMMENT ON COLUMN con_consult.create_by IS '创建人编号';
COMMENT ON COLUMN con_consult.update_by IS '更新人编号';
COMMENT ON COLUMN con_consult.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS local_message (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    topic VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE local_message IS '本地消息表';
COMMENT ON COLUMN local_message.id IS '主键编号';
COMMENT ON COLUMN local_message.tenant_id IS '租户编号';
COMMENT ON COLUMN local_message.topic IS '消息主题';
COMMENT ON COLUMN local_message.payload IS '消息内容';
COMMENT ON COLUMN local_message.retry_count IS '重试次数';
COMMENT ON COLUMN local_message.status IS '消息状态';
COMMENT ON COLUMN local_message.create_time IS '创建时间';
COMMENT ON COLUMN local_message.update_time IS '更新时间';
COMMENT ON COLUMN local_message.create_by IS '创建人编号';
COMMENT ON COLUMN local_message.update_by IS '更新人编号';
COMMENT ON COLUMN local_message.deleted IS '逻辑删除标识';

\connect hospital_prescription;

CREATE TABLE IF NOT EXISTS pre_prescription (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    prescription_no VARCHAR(64) NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    drug_count INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE pre_prescription IS '处方表';
COMMENT ON COLUMN pre_prescription.id IS '主键编号';
COMMENT ON COLUMN pre_prescription.tenant_id IS '租户编号';
COMMENT ON COLUMN pre_prescription.prescription_no IS '处方编号';
COMMENT ON COLUMN pre_prescription.patient_name IS '患者姓名';
COMMENT ON COLUMN pre_prescription.doctor_name IS '医生姓名';
COMMENT ON COLUMN pre_prescription.drug_count IS '药品数量';
COMMENT ON COLUMN pre_prescription.status IS '处方状态';
COMMENT ON COLUMN pre_prescription.create_time IS '创建时间';
COMMENT ON COLUMN pre_prescription.update_time IS '更新时间';
COMMENT ON COLUMN pre_prescription.create_by IS '创建人编号';
COMMENT ON COLUMN pre_prescription.update_by IS '更新人编号';
COMMENT ON COLUMN pre_prescription.deleted IS '逻辑删除标识';

\connect hospital_drug;

CREATE TABLE IF NOT EXISTS drug_info (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    spec VARCHAR(128) NOT NULL,
    inventory INT NOT NULL,
    unit VARCHAR(16) NOT NULL,
    warning_status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE drug_info IS '药品信息表';
COMMENT ON COLUMN drug_info.id IS '主键编号';
COMMENT ON COLUMN drug_info.tenant_id IS '租户编号';
COMMENT ON COLUMN drug_info.drug_name IS '药品名称';
COMMENT ON COLUMN drug_info.spec IS '药品规格';
COMMENT ON COLUMN drug_info.inventory IS '库存数量';
COMMENT ON COLUMN drug_info.unit IS '库存单位';
COMMENT ON COLUMN drug_info.warning_status IS '预警状态';
COMMENT ON COLUMN drug_info.create_time IS '创建时间';
COMMENT ON COLUMN drug_info.update_time IS '更新时间';
COMMENT ON COLUMN drug_info.create_by IS '创建人编号';
COMMENT ON COLUMN drug_info.update_by IS '更新人编号';
COMMENT ON COLUMN drug_info.deleted IS '逻辑删除标识';

\connect hospital_order;

CREATE TABLE IF NOT EXISTS ord_order (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    pay_status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE ord_order IS '订单表';
COMMENT ON COLUMN ord_order.id IS '主键编号';
COMMENT ON COLUMN ord_order.tenant_id IS '租户编号';
COMMENT ON COLUMN ord_order.order_no IS '订单号';
COMMENT ON COLUMN ord_order.business_type IS '业务类型';
COMMENT ON COLUMN ord_order.patient_name IS '患者姓名';
COMMENT ON COLUMN ord_order.amount IS '订单金额';
COMMENT ON COLUMN ord_order.pay_status IS '支付状态';
COMMENT ON COLUMN ord_order.create_time IS '创建时间';
COMMENT ON COLUMN ord_order.update_time IS '更新时间';
COMMENT ON COLUMN ord_order.create_by IS '创建人编号';
COMMENT ON COLUMN ord_order.update_by IS '更新人编号';
COMMENT ON COLUMN ord_order.deleted IS '逻辑删除标识';
