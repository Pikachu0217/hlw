SELECT 'CREATE DATABASE hospital_auth' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_auth')\gexec
SELECT 'CREATE DATABASE hospital_system' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_system')\gexec
SELECT 'CREATE DATABASE hospital_patient' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_patient')\gexec
SELECT 'CREATE DATABASE hospital_doctor' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_doctor')\gexec
SELECT 'CREATE DATABASE hospital_consult' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_consult')\gexec
SELECT 'CREATE DATABASE hospital_appointment' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_appointment')\gexec
SELECT 'CREATE DATABASE hospital_prescription' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_prescription')\gexec
SELECT 'CREATE DATABASE hospital_drug' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_drug')\gexec
SELECT 'CREATE DATABASE hospital_order' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_order')\gexec

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

ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS tenant_name VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS name VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS package_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS admin_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS expire_at DATE NOT NULL DEFAULT '2099-12-31';
ALTER TABLE sys_tenant ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '1' THEN '正常' ELSE status::text END;

COMMENT ON TABLE sys_tenant IS '租户信息表';
COMMENT ON COLUMN sys_tenant.id IS '主键编号';
COMMENT ON COLUMN sys_tenant.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_tenant.name IS '兼容旧表租户名称';
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

INSERT INTO sys_tenant (id, tenant_id, name, tenant_name, package_name, admin_name, expire_at, status)
VALUES
    (1, 100, '海岚门诊', '海岚门诊', '标准医疗版', '刘院长', '2026-12-31', '正常'),
    (2, 200, '青禾互联网医院', '青禾互联网医院', '集团旗舰版', '姜主任', '2026-08-16', '续费跟进')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name,
                               tenant_name = EXCLUDED.tenant_name,
                               package_name = EXCLUDED.package_name,
                               admin_name = EXCLUDED.admin_name,
                               expire_at = EXCLUDED.expire_at,
                               status = EXCLUDED.status;

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

ALTER TABLE sys_user ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '1' THEN '启用' ELSE status::text END;

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

ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS role_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS role_code VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS code VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_role ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '1' THEN '启用' ELSE status::text END;

COMMENT ON TABLE sys_role IS '系统角色表';
COMMENT ON COLUMN sys_role.id IS '主键编号';
COMMENT ON COLUMN sys_role.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_role.name IS '兼容旧表角色名称';
COMMENT ON COLUMN sys_role.code IS '兼容旧表角色编码';
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

ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS menu_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS permission VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS route_path VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT '启用';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS perms VARCHAR(128);
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS type VARCHAR(16) NOT NULL DEFAULT '菜单';

COMMENT ON TABLE sys_menu IS '系统菜单表';
COMMENT ON COLUMN sys_menu.id IS '主键编号';
COMMENT ON COLUMN sys_menu.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_menu.name IS '兼容旧表菜单名称';
COMMENT ON COLUMN sys_menu.perms IS '兼容旧表权限标识';
COMMENT ON COLUMN sys_menu.type IS '兼容旧表菜单类型';
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

ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS department VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE doc_doctor ALTER COLUMN user_id SET DEFAULT 0;
ALTER TABLE doc_doctor ALTER COLUMN name SET DEFAULT '';
ALTER TABLE doc_doctor ALTER COLUMN consult_status TYPE VARCHAR(32) USING CASE WHEN consult_status::text = '1' THEN 'ONLINE' WHEN consult_status::text = '0' THEN 'OFFLINE' ELSE consult_status::text END;

COMMENT ON TABLE doc_doctor IS '医生信息表';
COMMENT ON COLUMN doc_doctor.id IS '主键编号';
COMMENT ON COLUMN doc_doctor.user_id IS '关联用户编号';
COMMENT ON COLUMN doc_doctor.name IS '兼容旧表医生姓名';
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

ALTER TABLE pat_patient ADD COLUMN IF NOT EXISTS patient_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE pat_patient ADD COLUMN IF NOT EXISTS age INT NOT NULL DEFAULT 0;
ALTER TABLE pat_patient ADD COLUMN IF NOT EXISTS risk_level VARCHAR(32) NOT NULL DEFAULT '低风险';
ALTER TABLE pat_patient ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE pat_patient ADD COLUMN IF NOT EXISTS name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE pat_patient ALTER COLUMN user_id SET DEFAULT 0;
ALTER TABLE pat_patient ALTER COLUMN name SET DEFAULT '';
ALTER TABLE pat_patient ALTER COLUMN gender TYPE VARCHAR(16) USING CASE WHEN gender::text = '1' THEN '男' WHEN gender::text = '2' THEN '女' ELSE gender::text END;

COMMENT ON TABLE pat_patient IS '患者档案表';
COMMENT ON COLUMN pat_patient.id IS '主键编号';
COMMENT ON COLUMN pat_patient.tenant_id IS '租户编号';
COMMENT ON COLUMN pat_patient.user_id IS '关联用户编号';
COMMENT ON COLUMN pat_patient.name IS '兼容旧表患者姓名';
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

ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS appointment_no VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS patient_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS clinic_time VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS source VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS patient_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS doctor_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS department_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS schedule_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS appointment_type VARCHAR(32) NOT NULL DEFAULT '普通门诊';
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS number_source_id BIGINT;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS fee_amount DECIMAL(10, 2) NOT NULL DEFAULT 0;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS pay_time TIMESTAMP;
ALTER TABLE apt_appointment ADD COLUMN IF NOT EXISTS check_in_time TIMESTAMP;
ALTER TABLE apt_appointment ALTER COLUMN patient_id SET DEFAULT 0;
ALTER TABLE apt_appointment ALTER COLUMN doctor_id SET DEFAULT 0;
ALTER TABLE apt_appointment ALTER COLUMN department_id SET DEFAULT 0;
ALTER TABLE apt_appointment ALTER COLUMN schedule_id SET DEFAULT 0;
ALTER TABLE apt_appointment ALTER COLUMN appointment_type SET DEFAULT '普通门诊';
ALTER TABLE apt_appointment ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '0' THEN '待支付' WHEN status::text = '1' THEN '已支付' WHEN status::text = '2' THEN '已签到' WHEN status::text = '3' THEN '已完成' WHEN status::text = '4' THEN '已取消' ELSE status::text END;

COMMENT ON TABLE apt_appointment IS '预约单表';
COMMENT ON COLUMN apt_appointment.id IS '主键编号';
COMMENT ON COLUMN apt_appointment.tenant_id IS '租户编号';
COMMENT ON COLUMN apt_appointment.patient_id IS '患者编号';
COMMENT ON COLUMN apt_appointment.doctor_id IS '医生编号';
COMMENT ON COLUMN apt_appointment.department_id IS '科室编号';
COMMENT ON COLUMN apt_appointment.schedule_id IS '排班编号';
COMMENT ON COLUMN apt_appointment.number_source_id IS '号源编号';
COMMENT ON COLUMN apt_appointment.appointment_type IS '预约类型';
COMMENT ON COLUMN apt_appointment.appointment_no IS '预约单号';
COMMENT ON COLUMN apt_appointment.patient_name IS '患者姓名';
COMMENT ON COLUMN apt_appointment.doctor_name IS '医生姓名';
COMMENT ON COLUMN apt_appointment.clinic_time IS '就诊时间';
COMMENT ON COLUMN apt_appointment.source IS '预约来源';
COMMENT ON COLUMN apt_appointment.status IS '预约状态';
COMMENT ON COLUMN apt_appointment.fee_amount IS '预约费用';
COMMENT ON COLUMN apt_appointment.pay_time IS '支付时间';
COMMENT ON COLUMN apt_appointment.check_in_time IS '签到时间';
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

ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS consult_no VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS patient_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS channel VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS patient_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS doctor_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS consult_type VARCHAR(32) NOT NULL DEFAULT 'IMAGE_TEXT';
ALTER TABLE con_consult ALTER COLUMN patient_id SET DEFAULT 0;
ALTER TABLE con_consult ALTER COLUMN doctor_id SET DEFAULT 0;
ALTER TABLE con_consult ALTER COLUMN consult_type SET DEFAULT 'IMAGE_TEXT';
ALTER TABLE con_consult ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '0' THEN '待接单' ELSE status::text END;

COMMENT ON TABLE con_consult IS '问诊单表';
COMMENT ON COLUMN con_consult.id IS '主键编号';
COMMENT ON COLUMN con_consult.tenant_id IS '租户编号';
COMMENT ON COLUMN con_consult.patient_id IS '患者编号';
COMMENT ON COLUMN con_consult.doctor_id IS '医生编号';
COMMENT ON COLUMN con_consult.consult_type IS '问诊类型';
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

ALTER TABLE local_message ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE local_message ADD COLUMN IF NOT EXISTS payload TEXT NOT NULL DEFAULT '';
ALTER TABLE local_message ADD COLUMN IF NOT EXISTS create_by BIGINT;
ALTER TABLE local_message ADD COLUMN IF NOT EXISTS update_by BIGINT;
ALTER TABLE local_message ADD COLUMN IF NOT EXISTS deleted SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE local_message ADD COLUMN IF NOT EXISTS body TEXT NOT NULL DEFAULT '';
COMMENT ON COLUMN local_message.body IS '兼容旧表消息内容';

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

ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS prescription_no VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS patient_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS drug_count INT NOT NULL DEFAULT 0;
ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS consult_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS patient_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS doctor_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE pre_prescription ALTER COLUMN consult_id SET DEFAULT 0;
ALTER TABLE pre_prescription ALTER COLUMN patient_id SET DEFAULT 0;
ALTER TABLE pre_prescription ALTER COLUMN doctor_id SET DEFAULT 0;
ALTER TABLE pre_prescription ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '0' THEN '待审方' ELSE status::text END;

COMMENT ON TABLE pre_prescription IS '处方表';
COMMENT ON COLUMN pre_prescription.id IS '主键编号';
COMMENT ON COLUMN pre_prescription.tenant_id IS '租户编号';
COMMENT ON COLUMN pre_prescription.consult_id IS '问诊编号';
COMMENT ON COLUMN pre_prescription.patient_id IS '患者编号';
COMMENT ON COLUMN pre_prescription.doctor_id IS '医生编号';
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

ALTER TABLE drug_info ADD COLUMN IF NOT EXISTS drug_name VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE drug_info ADD COLUMN IF NOT EXISTS inventory INT NOT NULL DEFAULT 0;
ALTER TABLE drug_info ADD COLUMN IF NOT EXISTS warning_status VARCHAR(32) NOT NULL DEFAULT '正常';
ALTER TABLE drug_info ADD COLUMN IF NOT EXISTS name VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE drug_info ALTER COLUMN name SET DEFAULT '';
UPDATE drug_info SET drug_name = name WHERE drug_name = '' AND name <> '';

COMMENT ON TABLE drug_info IS '药品信息表';
COMMENT ON COLUMN drug_info.id IS '主键编号';
COMMENT ON COLUMN drug_info.tenant_id IS '租户编号';
COMMENT ON COLUMN drug_info.name IS '兼容旧表药品名称';
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
    status VARCHAR(32) NOT NULL DEFAULT '待支付',
    pay_status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS business_type VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS patient_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS pay_status VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT '待支付';
ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS biz_type VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS biz_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS patient_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE ord_order ALTER COLUMN biz_type SET DEFAULT '';
ALTER TABLE ord_order ALTER COLUMN biz_id SET DEFAULT 0;
ALTER TABLE ord_order ALTER COLUMN patient_id SET DEFAULT 0;
ALTER TABLE ord_order ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '0' THEN '待支付' WHEN status::text = '1' THEN '已支付' ELSE status::text END;

COMMENT ON TABLE ord_order IS '订单表';
COMMENT ON COLUMN ord_order.id IS '主键编号';
COMMENT ON COLUMN ord_order.tenant_id IS '租户编号';
COMMENT ON COLUMN ord_order.biz_type IS '兼容旧表业务类型';
COMMENT ON COLUMN ord_order.biz_id IS '兼容旧表业务编号';
COMMENT ON COLUMN ord_order.patient_id IS '患者编号';
COMMENT ON COLUMN ord_order.order_no IS '订单号';
COMMENT ON COLUMN ord_order.business_type IS '业务类型';
COMMENT ON COLUMN ord_order.patient_name IS '患者姓名';
COMMENT ON COLUMN ord_order.amount IS '订单金额';
COMMENT ON COLUMN ord_order.status IS '订单状态';
COMMENT ON COLUMN ord_order.pay_status IS '支付状态';
COMMENT ON COLUMN ord_order.create_time IS '创建时间';
COMMENT ON COLUMN ord_order.update_time IS '更新时间';
COMMENT ON COLUMN ord_order.create_by IS '创建人编号';
COMMENT ON COLUMN ord_order.update_by IS '更新人编号';
COMMENT ON COLUMN ord_order.deleted IS '逻辑删除标识';

\connect hospital_system;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    user_type VARCHAR(32) NOT NULL,
    dept_name VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    last_login VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user IS '系统管理用户表';
COMMENT ON COLUMN sys_user.id IS '主键编号';
COMMENT ON COLUMN sys_user.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_user.username IS '用户名称';
COMMENT ON COLUMN sys_user.password IS '登录密码';
COMMENT ON COLUMN sys_user.phone IS '联系电话';
COMMENT ON COLUMN sys_user.user_type IS '用户类型';
COMMENT ON COLUMN sys_user.dept_name IS '部门名称';
COMMENT ON COLUMN sys_user.role_name IS '角色名称';
COMMENT ON COLUMN sys_user.last_login IS '最近登录时间描述';
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

COMMENT ON TABLE sys_role IS '系统管理角色表';
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

ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS data_scope VARCHAR(64) NOT NULL DEFAULT '本租户数据';
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS member_count INT NOT NULL DEFAULT 0;
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围';
COMMENT ON COLUMN sys_role.member_count IS '成员数量';

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

COMMENT ON TABLE sys_menu IS '系统管理菜单表';
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

ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS menu_type VARCHAR(32) NOT NULL DEFAULT '菜单';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS parent_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS sort INT NOT NULL DEFAULT 0;
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型';
COMMENT ON COLUMN sys_menu.parent_id IS '父级菜单编号';
COMMENT ON COLUMN sys_menu.sort IS '菜单排序';

CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    dict_type VARCHAR(64) NOT NULL,
    dict_label VARCHAR(128) NOT NULL,
    dict_value VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(512),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE sys_dict ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '1' THEN '启用' WHEN status::text = '0' THEN '停用' ELSE status::text END;
ALTER TABLE sys_dict ADD COLUMN IF NOT EXISTS remark VARCHAR(512);

COMMENT ON TABLE sys_dict IS '系统字典表';
COMMENT ON COLUMN sys_dict.id IS '主键编号';
COMMENT ON COLUMN sys_dict.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_dict.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict.dict_value IS '字典键值';
COMMENT ON COLUMN sys_dict.status IS '字典状态';
COMMENT ON COLUMN sys_dict.sort IS '显示排序';
COMMENT ON COLUMN sys_dict.remark IS '备注';
COMMENT ON COLUMN sys_dict.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict.update_time IS '更新时间';
COMMENT ON COLUMN sys_dict.create_by IS '创建人编号';
COMMENT ON COLUMN sys_dict.update_by IS '更新人编号';
COMMENT ON COLUMN sys_dict.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    config_key VARCHAR(128) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(64) NOT NULL DEFAULT '业务参数',
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    remark VARCHAR(512),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE sys_config ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT '启用';
ALTER TABLE sys_config ALTER COLUMN config_type SET DEFAULT '业务参数';

COMMENT ON TABLE sys_config IS '系统参数配置表';
COMMENT ON COLUMN sys_config.id IS '主键编号';
COMMENT ON COLUMN sys_config.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_config.config_key IS '配置键';
COMMENT ON COLUMN sys_config.config_value IS '配置值';
COMMENT ON COLUMN sys_config.config_type IS '配置类型';
COMMENT ON COLUMN sys_config.status IS '配置状态';
COMMENT ON COLUMN sys_config.remark IS '备注';
COMMENT ON COLUMN sys_config.create_time IS '创建时间';
COMMENT ON COLUMN sys_config.update_time IS '更新时间';
COMMENT ON COLUMN sys_config.create_by IS '创建人编号';
COMMENT ON COLUMN sys_config.update_by IS '更新人编号';
COMMENT ON COLUMN sys_config.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_post (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    post_name VARCHAR(64) NOT NULL,
    post_code VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    remark VARCHAR(512),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_post IS '系统岗位表';
COMMENT ON COLUMN sys_post.id IS '主键编号';
COMMENT ON COLUMN sys_post.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_post.post_name IS '岗位名称';
COMMENT ON COLUMN sys_post.post_code IS '岗位编码';
COMMENT ON COLUMN sys_post.sort IS '显示排序';
COMMENT ON COLUMN sys_post.status IS '岗位状态';
COMMENT ON COLUMN sys_post.remark IS '备注';
COMMENT ON COLUMN sys_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_post.update_time IS '更新时间';
COMMENT ON COLUMN sys_post.create_by IS '创建人编号';
COMMENT ON COLUMN sys_post.update_by IS '更新人编号';
COMMENT ON COLUMN sys_post.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_user_post (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user_post IS '用户岗位关联表';
COMMENT ON COLUMN sys_user_post.id IS '主键编号';
COMMENT ON COLUMN sys_user_post.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_user_post.user_id IS '用户编号';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位编号';
COMMENT ON COLUMN sys_user_post.status IS '关联状态';
COMMENT ON COLUMN sys_user_post.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_post.update_time IS '更新时间';
COMMENT ON COLUMN sys_user_post.create_by IS '创建人编号';
COMMENT ON COLUMN sys_user_post.update_by IS '更新人编号';
COMMENT ON COLUMN sys_user_post.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    permission_name VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    menu_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_permission IS '系统权限码表';
COMMENT ON COLUMN sys_permission.id IS '主键编号';
COMMENT ON COLUMN sys_permission.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_permission.permission_name IS '权限名称';
COMMENT ON COLUMN sys_permission.permission_code IS '权限编码';
COMMENT ON COLUMN sys_permission.resource_type IS '资源类型';
COMMENT ON COLUMN sys_permission.menu_id IS '关联菜单编号';
COMMENT ON COLUMN sys_permission.status IS '权限状态';
COMMENT ON COLUMN sys_permission.create_time IS '创建时间';
COMMENT ON COLUMN sys_permission.update_time IS '更新时间';
COMMENT ON COLUMN sys_permission.create_by IS '创建人编号';
COMMENT ON COLUMN sys_permission.update_by IS '更新人编号';
COMMENT ON COLUMN sys_permission.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';
COMMENT ON COLUMN sys_user_role.id IS '主键编号';
COMMENT ON COLUMN sys_user_role.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_user_role.user_id IS '用户编号';
COMMENT ON COLUMN sys_user_role.role_id IS '角色编号';
COMMENT ON COLUMN sys_user_role.status IS '关联状态';
COMMENT ON COLUMN sys_user_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_user_role.update_time IS '更新时间';
COMMENT ON COLUMN sys_user_role.create_by IS '创建人编号';
COMMENT ON COLUMN sys_user_role.update_by IS '更新人编号';
COMMENT ON COLUMN sys_user_role.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS '主键编号';
COMMENT ON COLUMN sys_role_menu.tenant_id IS '租户编号';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色编号';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单编号';
COMMENT ON COLUMN sys_role_menu.status IS '关联状态';
COMMENT ON COLUMN sys_role_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_role_menu.update_time IS '更新时间';
COMMENT ON COLUMN sys_role_menu.create_by IS '创建人编号';
COMMENT ON COLUMN sys_role_menu.update_by IS '更新人编号';
COMMENT ON COLUMN sys_role_menu.deleted IS '逻辑删除标识';

INSERT INTO sys_user (id, tenant_id, username, password, phone, user_type, dept_name, role_name, last_login, status)
VALUES
    (1, 100, '门诊运营', '{noop}123456', '13800001111', 'ADMIN', '运营中心', '运营管理员', '今天 08:40', '启用'),
    (2, 100, '药房主管', '{noop}123456', '13800002222', 'ADMIN', '药房组', '库存专员', '今天 07:58', '启用')
ON CONFLICT DO NOTHING;

INSERT INTO sys_role (id, tenant_id, role_name, role_code, data_scope, member_count, status, update_time)
VALUES
    (1, 100, '系统管理员', 'SYSTEM_ADMIN', '全部数据', 3, '启用', '2026-06-10 11:20:00'),
    (2, 100, '运营管理员', 'OPERATOR_ADMIN', '本租户数据', 11, '启用', '2026-06-09 17:45:00')
ON CONFLICT DO NOTHING;

INSERT INTO sys_menu (id, tenant_id, menu_name, menu_type, permission, route_path, status)
VALUES
    (1, 100, '工作台', '菜单', 'dashboard:view', '/dashboard', '启用'),
    (2, 100, '医生管理', '菜单', 'doctor:list', '/doctor', '启用'),
    (3, 100, '用户管理', '菜单', 'system:user:list', '/system/users', '启用'),
    (4, 100, '角色管理', '菜单', 'system:role:list', '/system/roles', '启用'),
    (5, 100, '菜单管理', '菜单', 'system:menu:list', '/system/menus', '启用'),
    (6, 100, '字典管理', '菜单', 'system:dict:list', '/system/dicts', '启用'),
    (7, 100, '参数配置', '菜单', 'system:config:list', '/system/configs', '启用'),
    (8, 100, '岗位管理', '菜单', 'system:post:list', '/system/posts', '启用'),
    (9, 100, '权限管理', '菜单', 'system:permission:list', '/system/permissions', '启用'),
    (10, 100, '科室管理', '菜单', 'doctor:department:list', '/doctor/departments', '启用')
ON CONFLICT (id) DO UPDATE SET menu_name = EXCLUDED.menu_name,
                               menu_type = EXCLUDED.menu_type,
                               permission = EXCLUDED.permission,
                               route_path = EXCLUDED.route_path,
                               status = EXCLUDED.status;

INSERT INTO sys_dict (id, tenant_id, dict_type, dict_label, dict_value, sort, status, remark)
VALUES
    (1, 100, 'account_status', '启用', '启用', 1, '启用', '后台账号可登录'),
    (2, 100, 'account_status', '停用', '停用', 2, '启用', '后台账号禁止登录'),
    (3, 100, 'menu_type', '目录', '目录', 1, '启用', '菜单目录节点'),
    (4, 100, 'menu_type', '菜单', '菜单', 2, '启用', '可访问页面菜单'),
    (5, 100, 'menu_type', '按钮', '按钮', 3, '启用', '页面按钮权限')
ON CONFLICT (id) DO UPDATE SET dict_type = EXCLUDED.dict_type,
                               dict_label = EXCLUDED.dict_label,
                               dict_value = EXCLUDED.dict_value,
                               sort = EXCLUDED.sort,
                               status = EXCLUDED.status,
                               remark = EXCLUDED.remark;

INSERT INTO sys_config (id, tenant_id, config_key, config_value, config_type, status, remark)
VALUES
    (1, 100, 'consult.default_duration_minutes', '30', '问诊配置', '启用', '默认问诊时长'),
    (2, 100, 'appointment.release_window_minutes', '15', '预约配置', '启用', '放号提前窗口'),
    (3, 100, 'security.password_expire_days', '90', '安全配置', '启用', '密码过期天数')
ON CONFLICT (id) DO UPDATE SET config_key = EXCLUDED.config_key,
                               config_value = EXCLUDED.config_value,
                               config_type = EXCLUDED.config_type,
                               status = EXCLUDED.status,
                               remark = EXCLUDED.remark;

INSERT INTO sys_post (id, tenant_id, post_name, post_code, sort, status, remark)
VALUES
    (1, 100, '运营管理员', 'OPERATIONS_ADMIN', 1, '启用', '负责平台日常运营'),
    (2, 100, '药房主管', 'PHARMACY_MANAGER', 2, '启用', '负责药品库存和发药'),
    (3, 100, '客服专员', 'SERVICE_AGENT', 3, '启用', '负责患者咨询和预约协助')
ON CONFLICT (id) DO UPDATE SET post_name = EXCLUDED.post_name,
                               post_code = EXCLUDED.post_code,
                               sort = EXCLUDED.sort,
                               status = EXCLUDED.status,
                               remark = EXCLUDED.remark;

INSERT INTO sys_user_post (id, tenant_id, user_id, post_id, status)
VALUES
    (1, 100, 1, 1, '启用'),
    (2, 100, 2, 2, '启用')
ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id,
                               post_id = EXCLUDED.post_id,
                               status = EXCLUDED.status;

INSERT INTO sys_permission (id, tenant_id, permission_name, permission_code, resource_type, menu_id, status)
VALUES
    (1, 100, '查看用户', 'system:user:list', '菜单', 3, '启用'),
    (2, 100, '维护角色', 'system:role:edit', '按钮', 4, '启用'),
    (3, 100, '维护菜单', 'system:menu:edit', '按钮', 5, '启用'),
    (4, 100, '维护字典', 'system:dict:edit', '按钮', 6, '启用'),
    (5, 100, '维护岗位', 'system:post:edit', '按钮', 8, '启用')
ON CONFLICT (id) DO UPDATE SET permission_name = EXCLUDED.permission_name,
                               permission_code = EXCLUDED.permission_code,
                               resource_type = EXCLUDED.resource_type,
                               menu_id = EXCLUDED.menu_id,
                               status = EXCLUDED.status;

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, status)
VALUES
    (1, 100, 1, 1, '启用'),
    (2, 100, 2, 2, '启用')
ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id,
                               role_id = EXCLUDED.role_id,
                               status = EXCLUDED.status;

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status)
VALUES
    (1, 100, 1, 1, '启用'),
    (2, 100, 1, 3, '启用'),
    (3, 100, 1, 4, '启用'),
    (4, 100, 1, 5, '启用'),
    (5, 100, 1, 6, '启用'),
    (6, 100, 1, 7, '启用'),
    (7, 100, 1, 8, '启用'),
    (8, 100, 1, 9, '启用'),
    (9, 100, 2, 1, '启用'),
    (10, 100, 2, 3, '启用')
ON CONFLICT (id) DO UPDATE SET role_id = EXCLUDED.role_id,
                               menu_id = EXCLUDED.menu_id,
                               status = EXCLUDED.status;

\connect hospital_auth;

INSERT INTO sys_user (id, tenant_id, username, password, phone, user_type, status)
VALUES
    (1, 100, 'admin', '{noop}admin123', '13800000001', 'ADMIN', '启用'),
    (2, 100, 'patient', '{noop}patient123', '13900000001', 'PATIENT', '启用'),
    (3, 100, '运营主任', '{noop}123456', '13800000002', 'ADMIN', '启用')
ON CONFLICT DO NOTHING;

\connect hospital_doctor;

ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT '接诊中';
ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS schedule_desc VARCHAR(64) NOT NULL DEFAULT '上午门诊';
ALTER TABLE doc_doctor ADD COLUMN IF NOT EXISTS patient_count INT NOT NULL DEFAULT 0;
COMMENT ON COLUMN doc_doctor.status IS '医生展示状态';
COMMENT ON COLUMN doc_doctor.schedule_desc IS '排班描述';
COMMENT ON COLUMN doc_doctor.patient_count IS '当前接诊患者数';

CREATE TABLE IF NOT EXISTS doc_department (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    department_name VARCHAR(64) NOT NULL,
    doctor_count INT NOT NULL,
    queue_desc VARCHAR(64) NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    description VARCHAR(512),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS department_name VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS doctor_count INT NOT NULL DEFAULT 0;
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS queue_desc VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS name VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS parent_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS sort INT NOT NULL DEFAULT 0;
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT '启用';
ALTER TABLE doc_department ADD COLUMN IF NOT EXISTS description VARCHAR(512);
ALTER TABLE doc_department ALTER COLUMN name SET DEFAULT '';
ALTER TABLE doc_department ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '1' THEN '启用' WHEN status::text = '0' THEN '停用' ELSE status::text END;
ALTER TABLE doc_department ALTER COLUMN status SET DEFAULT '启用';
UPDATE doc_department SET department_name = name WHERE department_name = '' AND name <> '';

COMMENT ON TABLE doc_department IS '科室信息表';
COMMENT ON COLUMN doc_department.id IS '主键编号';
COMMENT ON COLUMN doc_department.tenant_id IS '租户编号';
COMMENT ON COLUMN doc_department.name IS '兼容旧表科室名称';
COMMENT ON COLUMN doc_department.department_name IS '科室名称';
COMMENT ON COLUMN doc_department.doctor_count IS '医生数量';
COMMENT ON COLUMN doc_department.queue_desc IS '排队描述';
COMMENT ON COLUMN doc_department.parent_id IS '父级科室编号';
COMMENT ON COLUMN doc_department.sort IS '科室排序';
COMMENT ON COLUMN doc_department.status IS '科室状态';
COMMENT ON COLUMN doc_department.description IS '科室说明';
COMMENT ON COLUMN doc_department.create_time IS '创建时间';
COMMENT ON COLUMN doc_department.update_time IS '更新时间';
COMMENT ON COLUMN doc_department.create_by IS '创建人编号';
COMMENT ON COLUMN doc_department.update_by IS '更新人编号';
COMMENT ON COLUMN doc_department.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS doc_doctor_department (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    is_free SMALLINT NOT NULL DEFAULT 0,
    appointment_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE doc_doctor_department IS '医生科室关联表';
COMMENT ON COLUMN doc_doctor_department.id IS '主键编号';
COMMENT ON COLUMN doc_doctor_department.tenant_id IS '租户编号';
COMMENT ON COLUMN doc_doctor_department.doctor_id IS '医生编号';
COMMENT ON COLUMN doc_doctor_department.department_id IS '科室编号';
COMMENT ON COLUMN doc_doctor_department.is_free IS '是否免挂号费';
COMMENT ON COLUMN doc_doctor_department.appointment_fee IS '挂号费用';
COMMENT ON COLUMN doc_doctor_department.create_time IS '创建时间';
COMMENT ON COLUMN doc_doctor_department.update_time IS '更新时间';
COMMENT ON COLUMN doc_doctor_department.create_by IS '创建人编号';
COMMENT ON COLUMN doc_doctor_department.update_by IS '更新人编号';
COMMENT ON COLUMN doc_doctor_department.deleted IS '逻辑删除标识';
DELETE FROM doc_doctor_department current_row
USING doc_doctor_department kept_row
WHERE current_row.id > kept_row.id
  AND current_row.doctor_id = kept_row.doctor_id
  AND current_row.department_id = kept_row.department_id
  AND current_row.deleted = 0
  AND kept_row.deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_doc_doctor_department_active
ON doc_doctor_department (doctor_id, department_id)
WHERE deleted = 0;

CREATE TABLE IF NOT EXISTS doc_schedule (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    slot VARCHAR(32) NOT NULL,
    remain_number INT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE doc_schedule ADD COLUMN IF NOT EXISTS slot VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE doc_schedule ADD COLUMN IF NOT EXISTS schedule_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE doc_schedule ADD COLUMN IF NOT EXISTS time_slot VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE doc_schedule ADD COLUMN IF NOT EXISTS total_number INT NOT NULL DEFAULT 0;
ALTER TABLE doc_schedule ALTER COLUMN schedule_date SET DEFAULT CURRENT_DATE;
ALTER TABLE doc_schedule ALTER COLUMN time_slot SET DEFAULT '';

COMMENT ON TABLE doc_schedule IS '医生排班表';
COMMENT ON COLUMN doc_schedule.id IS '主键编号';
COMMENT ON COLUMN doc_schedule.tenant_id IS '租户编号';
COMMENT ON COLUMN doc_schedule.doctor_id IS '医生编号';
COMMENT ON COLUMN doc_schedule.slot IS '出诊时段';
COMMENT ON COLUMN doc_schedule.schedule_date IS '排班日期';
COMMENT ON COLUMN doc_schedule.time_slot IS '排班时间段';
COMMENT ON COLUMN doc_schedule.total_number IS '总号源数量';
COMMENT ON COLUMN doc_schedule.remain_number IS '剩余号源数量';
COMMENT ON COLUMN doc_schedule.create_time IS '创建时间';
COMMENT ON COLUMN doc_schedule.update_time IS '更新时间';
COMMENT ON COLUMN doc_schedule.create_by IS '创建人编号';
COMMENT ON COLUMN doc_schedule.update_by IS '更新人编号';
COMMENT ON COLUMN doc_schedule.deleted IS '逻辑删除标识';

INSERT INTO doc_department (id, tenant_id, name, department_name, doctor_count, queue_desc, parent_id, sort, status)
VALUES
    (10, 100, '心内科', '心内科', 1, '当前等候 6 人', 0, 1, '启用'),
    (20, 100, '儿科', '儿科', 1, '当前等候 8 人', 0, 2, '启用'),
    (30, 100, '皮肤科', '皮肤科', 0, '当前等候 3 人', 0, 3, '启用')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name,
                               department_name = EXCLUDED.department_name,
                               doctor_count = EXCLUDED.doctor_count,
                               queue_desc = EXCLUDED.queue_desc,
                               parent_id = EXCLUDED.parent_id,
                               sort = EXCLUDED.sort,
                               status = EXCLUDED.status;

INSERT INTO doc_doctor (id, tenant_id, user_id, name, doctor_name, title, department, specialty, consult_fee, consult_status, status, schedule_desc, patient_count)
VALUES
    (1, 100, 1, '陈知衡', '陈知衡', '主任医师', '心内科', '冠脉慢病管理', 50.00, 'ONLINE', '接诊中', '上午门诊', 16),
    (2, 100, 2, '顾清和', '顾清和', '副主任医师', '内分泌科', '糖尿病营养干预', 30.00, 'BUSY', '候诊', '下午门诊', 9)
ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id,
                               name = EXCLUDED.name,
                               doctor_name = EXCLUDED.doctor_name,
                               title = EXCLUDED.title,
                               department = EXCLUDED.department,
                               specialty = EXCLUDED.specialty,
                               consult_fee = EXCLUDED.consult_fee,
                               consult_status = EXCLUDED.consult_status,
                               status = EXCLUDED.status,
                               schedule_desc = EXCLUDED.schedule_desc,
                               patient_count = EXCLUDED.patient_count;

INSERT INTO doc_schedule (id, tenant_id, doctor_id, schedule_date, time_slot, slot, total_number, remain_number)
VALUES
    (1, 100, 1, '2026-06-13', '上午', '上午', 20, 6),
    (2, 100, 2, '2026-06-13', '下午', '下午', 10, 1)
ON CONFLICT (id) DO UPDATE SET schedule_date = EXCLUDED.schedule_date,
                               time_slot = EXCLUDED.time_slot,
                               slot = EXCLUDED.slot,
                               total_number = EXCLUDED.total_number,
                               remain_number = EXCLUDED.remain_number;

INSERT INTO doc_doctor_department (id, tenant_id, doctor_id, department_id, is_free, appointment_fee)
VALUES
    (1, 100, 1, 10, 0, 50.00),
    (2, 100, 2, 20, 0, 30.00)
ON CONFLICT (id) DO UPDATE SET doctor_id = EXCLUDED.doctor_id,
                               department_id = EXCLUDED.department_id,
                               is_free = EXCLUDED.is_free,
                               appointment_fee = EXCLUDED.appointment_fee;

\connect hospital_patient;

ALTER TABLE pat_patient ADD COLUMN IF NOT EXISTS last_visit DATE;
COMMENT ON COLUMN pat_patient.last_visit IS '最近就诊日期';

CREATE TABLE IF NOT EXISTS pat_health_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    summary VARCHAR(256) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE pat_health_record ADD COLUMN IF NOT EXISTS title VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE pat_health_record ADD COLUMN IF NOT EXISTS summary VARCHAR(256) NOT NULL DEFAULT '';

COMMENT ON TABLE pat_health_record IS '健康档案表';
COMMENT ON COLUMN pat_health_record.id IS '主键编号';
COMMENT ON COLUMN pat_health_record.tenant_id IS '租户编号';
COMMENT ON COLUMN pat_health_record.patient_id IS '患者编号';
COMMENT ON COLUMN pat_health_record.title IS '档案标题';
COMMENT ON COLUMN pat_health_record.summary IS '档案摘要';
COMMENT ON COLUMN pat_health_record.create_time IS '创建时间';
COMMENT ON COLUMN pat_health_record.update_time IS '更新时间';
COMMENT ON COLUMN pat_health_record.create_by IS '创建人编号';
COMMENT ON COLUMN pat_health_record.update_by IS '更新人编号';
COMMENT ON COLUMN pat_health_record.deleted IS '逻辑删除标识';

INSERT INTO pat_patient (id, tenant_id, user_id, name, patient_name, gender, age, phone, risk_level, last_visit)
VALUES
    (1, 100, 1, '赵晓岚', '赵晓岚', '女', 34, '13900001111', '中风险', '2026-06-11'),
    (2, 100, 2, '沈博远', '沈博远', '男', 58, '13900002222', '高风险', '2026-06-10')
ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id,
                               name = EXCLUDED.name,
                               patient_name = EXCLUDED.patient_name,
                               gender = EXCLUDED.gender,
                               age = EXCLUDED.age,
                               phone = EXCLUDED.phone,
                               risk_level = EXCLUDED.risk_level,
                               last_visit = EXCLUDED.last_visit;

INSERT INTO pat_health_record (id, tenant_id, patient_id, title, summary)
VALUES
    (1, 100, 1, '发热问诊', '儿童发热 12 小时，已线上问诊'),
    (2, 100, 2, '复诊续方', '慢病用药复诊记录')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('pat_health_record', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM pat_health_record), 1), true);

\connect hospital_appointment;

CREATE TABLE IF NOT EXISTS apt_number_source (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    number_seq INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE apt_number_source ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '0' THEN 'AVAILABLE' WHEN status::text = '1' THEN 'LOCKED' WHEN status::text = '2' THEN 'USED' WHEN status::text = '3' THEN 'RELEASED' ELSE status::text END;
ALTER TABLE apt_number_source ADD COLUMN IF NOT EXISTS lock_time TIMESTAMP;

COMMENT ON TABLE apt_number_source IS '预约号源表';
COMMENT ON COLUMN apt_number_source.id IS '主键编号';
COMMENT ON COLUMN apt_number_source.tenant_id IS '租户编号';
COMMENT ON COLUMN apt_number_source.schedule_id IS '排班编号';
COMMENT ON COLUMN apt_number_source.number_seq IS '号源序号';
COMMENT ON COLUMN apt_number_source.status IS '号源状态';
COMMENT ON COLUMN apt_number_source.lock_time IS '锁定时间';
COMMENT ON COLUMN apt_number_source.create_time IS '创建时间';
COMMENT ON COLUMN apt_number_source.update_time IS '更新时间';
COMMENT ON COLUMN apt_number_source.create_by IS '创建人编号';
COMMENT ON COLUMN apt_number_source.update_by IS '更新人编号';
COMMENT ON COLUMN apt_number_source.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS apt_number_source_release_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    release_time TIMESTAMP NOT NULL,
    release_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT '启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE apt_number_source_release_config ADD COLUMN IF NOT EXISTS release_count INT NOT NULL DEFAULT 0;
ALTER TABLE apt_number_source_release_config ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT '启用';
ALTER TABLE apt_number_source_release_config ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '1' THEN '启用' WHEN status::text = '0' THEN '停用' WHEN status::text = '2' THEN '已完成' ELSE status::text END;

COMMENT ON TABLE apt_number_source_release_config IS '放号配置表';
COMMENT ON COLUMN apt_number_source_release_config.id IS '主键编号';
COMMENT ON COLUMN apt_number_source_release_config.tenant_id IS '租户编号';
COMMENT ON COLUMN apt_number_source_release_config.schedule_id IS '排班编号';
COMMENT ON COLUMN apt_number_source_release_config.release_time IS '放号时间';
COMMENT ON COLUMN apt_number_source_release_config.release_count IS '放号数量';
COMMENT ON COLUMN apt_number_source_release_config.status IS '配置状态';
COMMENT ON COLUMN apt_number_source_release_config.create_time IS '创建时间';
COMMENT ON COLUMN apt_number_source_release_config.update_time IS '更新时间';
COMMENT ON COLUMN apt_number_source_release_config.create_by IS '创建人编号';
COMMENT ON COLUMN apt_number_source_release_config.update_by IS '更新人编号';
COMMENT ON COLUMN apt_number_source_release_config.deleted IS '逻辑删除标识';

INSERT INTO apt_appointment (id, tenant_id, patient_id, doctor_id, department_id, schedule_id, appointment_type, appointment_no, patient_name, doctor_name, clinic_time, source, status)
VALUES
    (1, 100, 1, 1, 10, 1, '普通门诊', 'YY20260612001', '赵晓岚', '陈知衡', '2026-06-13 14:00', '小程序', '待支付'),
    (2, 100, 2, 2, 20, 2, '普通门诊', 'YY20260612002', '沈博远', '顾清和', '2026-06-13 15:30', '客服代约', '已签到')
ON CONFLICT DO NOTHING;

INSERT INTO apt_number_source (id, tenant_id, schedule_id, number_seq, status)
VALUES
    (1, 100, 1, 1, 'AVAILABLE'),
    (2, 100, 1, 2, 'AVAILABLE')
ON CONFLICT DO NOTHING;

INSERT INTO apt_number_source_release_config (id, tenant_id, schedule_id, release_time, release_count, status)
VALUES
    (1, 100, 1, '2026-06-13 08:00:00', 10, '启用')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('apt_appointment', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM apt_appointment), 1), true);
SELECT setval(pg_get_serial_sequence('apt_number_source', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM apt_number_source), 1), true);
SELECT setval(pg_get_serial_sequence('apt_number_source_release_config', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM apt_number_source_release_config), 1), true);

\connect hospital_consult;

ALTER TABLE con_consult ADD COLUMN IF NOT EXISTS updated_at VARCHAR(64) NOT NULL DEFAULT '10:00';
COMMENT ON COLUMN con_consult.updated_at IS '前端展示更新时间';

CREATE TABLE IF NOT EXISTS con_message (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    consult_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_type VARCHAR(32) NOT NULL,
    content VARCHAR(512) NOT NULL,
    content_type VARCHAR(32) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE con_message ADD COLUMN IF NOT EXISTS read_flag BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE con_message ADD COLUMN IF NOT EXISTS is_read SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE con_message ALTER COLUMN is_read SET DEFAULT 0;
UPDATE con_message SET read_flag = (is_read <> 0);

COMMENT ON TABLE con_message IS '问诊消息表';
COMMENT ON COLUMN con_message.id IS '主键编号';
COMMENT ON COLUMN con_message.tenant_id IS '租户编号';
COMMENT ON COLUMN con_message.consult_id IS '问诊编号';
COMMENT ON COLUMN con_message.sender_id IS '发送人编号';
COMMENT ON COLUMN con_message.sender_type IS '发送人类型';
COMMENT ON COLUMN con_message.content IS '消息内容';
COMMENT ON COLUMN con_message.content_type IS '消息内容类型';
COMMENT ON COLUMN con_message.read_flag IS '已读标识';
COMMENT ON COLUMN con_message.is_read IS '兼容旧表已读标识';
COMMENT ON COLUMN con_message.create_time IS '创建时间';
COMMENT ON COLUMN con_message.update_time IS '更新时间';
COMMENT ON COLUMN con_message.create_by IS '创建人编号';
COMMENT ON COLUMN con_message.update_by IS '更新人编号';
COMMENT ON COLUMN con_message.deleted IS '逻辑删除标识';

INSERT INTO con_consult (id, tenant_id, patient_id, doctor_id, consult_type, consult_no, patient_name, doctor_name, channel, status, updated_at)
VALUES
    (1, 100, 1, 1, 'IMAGE_TEXT', 'ZX20260612001', '赵晓岚', '陈知衡', '图文', '待接单', '10:18'),
    (2, 100, 2, 2, 'VIDEO', 'ZX20260612002', '沈博远', '顾清和', '视频', '咨询中', '10:07')
ON CONFLICT (id) DO UPDATE SET patient_id = EXCLUDED.patient_id,
                               doctor_id = EXCLUDED.doctor_id,
                               consult_type = EXCLUDED.consult_type,
                               consult_no = EXCLUDED.consult_no,
                               patient_name = EXCLUDED.patient_name,
                               doctor_name = EXCLUDED.doctor_name,
                               channel = EXCLUDED.channel,
                               status = EXCLUDED.status,
                               updated_at = EXCLUDED.updated_at;

INSERT INTO con_message (id, tenant_id, consult_id, sender_id, sender_type, content, content_type, read_flag, create_time)
VALUES
    (1, 100, 1, 2, 'DOCTOR', '哪里不舒服', 'TEXT', FALSE, '2026-06-13 10:15:00'),
    (2, 100, 1, 1, 'PATIENT', '孩子从昨晚开始发烧', 'TEXT', FALSE, '2026-06-13 10:16:00')
ON CONFLICT DO NOTHING;

\connect hospital_prescription;

ALTER TABLE pre_prescription ADD COLUMN IF NOT EXISTS issued_at VARCHAR(32) NOT NULL DEFAULT '09:00';
COMMENT ON COLUMN pre_prescription.issued_at IS '开方时间展示值';

INSERT INTO pre_prescription (id, tenant_id, consult_id, patient_id, doctor_id, prescription_no, patient_name, doctor_name, drug_count, issued_at, status)
VALUES
    (1, 100, 1, 1, 1, 'CF20260612001', '赵晓岚', '陈知衡', 3, '09:42', '待审方'),
    (2, 100, 2, 2, 2, 'CF20260612002', '沈博远', '顾清和', 5, '09:18', '待发药')
ON CONFLICT (id) DO UPDATE SET consult_id = EXCLUDED.consult_id,
                               patient_id = EXCLUDED.patient_id,
                               doctor_id = EXCLUDED.doctor_id,
                               prescription_no = EXCLUDED.prescription_no,
                               patient_name = EXCLUDED.patient_name,
                               doctor_name = EXCLUDED.doctor_name,
                               drug_count = EXCLUDED.drug_count,
                               issued_at = EXCLUDED.issued_at,
                               status = EXCLUDED.status;

\connect hospital_drug;

CREATE TABLE IF NOT EXISTS drug_stock (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    warehouse_name VARCHAR(64) NOT NULL,
    inventory INT NOT NULL,
    warning_status VARCHAR(32) NOT NULL,
    stock_qty DECIMAL(10, 2) NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE drug_stock ADD COLUMN IF NOT EXISTS warehouse_name VARCHAR(64) NOT NULL DEFAULT '中心药房';
ALTER TABLE drug_stock ADD COLUMN IF NOT EXISTS inventory INT NOT NULL DEFAULT 0;
ALTER TABLE drug_stock ADD COLUMN IF NOT EXISTS warning_status VARCHAR(32) NOT NULL DEFAULT '正常';
ALTER TABLE drug_stock ADD COLUMN IF NOT EXISTS stock_qty DECIMAL(10, 2) NOT NULL DEFAULT 0;

COMMENT ON TABLE drug_stock IS '药品库存表';
COMMENT ON COLUMN drug_stock.id IS '主键编号';
COMMENT ON COLUMN drug_stock.tenant_id IS '租户编号';
COMMENT ON COLUMN drug_stock.drug_id IS '药品编号';
COMMENT ON COLUMN drug_stock.warehouse_name IS '仓库名称';
COMMENT ON COLUMN drug_stock.inventory IS '库存数量';
COMMENT ON COLUMN drug_stock.warning_status IS '预警状态';
COMMENT ON COLUMN drug_stock.stock_qty IS '库存数量兼容字段';
COMMENT ON COLUMN drug_stock.create_time IS '创建时间';
COMMENT ON COLUMN drug_stock.update_time IS '更新时间';
COMMENT ON COLUMN drug_stock.create_by IS '创建人编号';
COMMENT ON COLUMN drug_stock.update_by IS '更新人编号';
COMMENT ON COLUMN drug_stock.deleted IS '逻辑删除标识';

CREATE TABLE IF NOT EXISTS drug_delivery (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    prescription_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    receiver_name VARCHAR(64) NOT NULL,
    receiver_phone VARCHAR(32) NOT NULL,
    receiver_address VARCHAR(512) NOT NULL,
    tracking_no VARCHAR(128),
    ship_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE drug_delivery ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PENDING';
ALTER TABLE drug_delivery ALTER COLUMN status TYPE VARCHAR(32) USING CASE WHEN status::text = '0' THEN 'PENDING' WHEN status::text = '1' THEN 'SHIPPED' WHEN status::text = '2' THEN 'DELIVERED' WHEN status::text = '3' THEN 'CANCELLED' ELSE status::text END;
ALTER TABLE drug_delivery ALTER COLUMN status SET DEFAULT 'PENDING';

COMMENT ON TABLE drug_delivery IS '药品配送表';
COMMENT ON COLUMN drug_delivery.id IS '主键编号';
COMMENT ON COLUMN drug_delivery.tenant_id IS '租户编号';
COMMENT ON COLUMN drug_delivery.order_id IS '订单编号';
COMMENT ON COLUMN drug_delivery.prescription_id IS '处方编号';
COMMENT ON COLUMN drug_delivery.status IS '配送状态';
COMMENT ON COLUMN drug_delivery.receiver_name IS '收货人姓名';
COMMENT ON COLUMN drug_delivery.receiver_phone IS '收货人电话';
COMMENT ON COLUMN drug_delivery.receiver_address IS '收货地址';
COMMENT ON COLUMN drug_delivery.tracking_no IS '物流单号';
COMMENT ON COLUMN drug_delivery.ship_time IS '发货时间';
COMMENT ON COLUMN drug_delivery.create_time IS '创建时间';
COMMENT ON COLUMN drug_delivery.update_time IS '更新时间';
COMMENT ON COLUMN drug_delivery.create_by IS '创建人编号';
COMMENT ON COLUMN drug_delivery.update_by IS '更新人编号';
COMMENT ON COLUMN drug_delivery.deleted IS '逻辑删除标识';

INSERT INTO drug_info (id, tenant_id, name, drug_name, spec, inventory, unit, warning_status)
VALUES
    (1, 100, '阿托伐他汀钙片', '阿托伐他汀钙片', '20mg*14片', 124, '盒', '正常'),
    (2, 100, '盐酸二甲双胍缓释片', '盐酸二甲双胍缓释片', '0.5g*30片', 42, '盒', '预警')
ON CONFLICT (id) DO NOTHING;

INSERT INTO drug_stock (id, tenant_id, drug_id, warehouse_name, inventory, warning_status, stock_qty)
VALUES
    (1, 100, 1, '中心药房', 124, '正常', 124),
    (2, 100, 2, '中心药房', 42, '预警', 42)
ON CONFLICT DO NOTHING;

INSERT INTO drug_delivery (id, tenant_id, order_id, prescription_id, status, receiver_name, receiver_phone, receiver_address, tracking_no)
VALUES
    (1, 100, 1, 1, 'PENDING', '赵晓岚', '13800000009', '杭州市西湖区互联网医院体验点', '')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('drug_info', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM drug_info), 1), true);
SELECT setval(pg_get_serial_sequence('drug_stock', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM drug_stock), 1), true);
SELECT setval(pg_get_serial_sequence('drug_delivery', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM drug_delivery), 1), true);

\connect hospital_order;

ALTER TABLE ord_order ADD COLUMN IF NOT EXISTS created_at VARCHAR(32) NOT NULL DEFAULT '09:00';
COMMENT ON COLUMN ord_order.created_at IS '订单创建时间展示值';

INSERT INTO ord_order (id, tenant_id, order_no, biz_type, biz_id, patient_id, business_type, patient_name, amount, pay_status, created_at, status)
VALUES
    (1, 100, 'DD20260612001', 'APPOINTMENT', 1, 1, '门诊预约', '赵晓岚', 58.00, '已支付', '09:12', '已支付'),
    (2, 100, 'DD20260612002', 'CONSULT', 2, 2, '图文咨询', '沈博远', 39.90, '待支付', '09:35', '待支付')
ON CONFLICT (id) DO UPDATE SET biz_type = EXCLUDED.biz_type,
                               biz_id = EXCLUDED.biz_id,
                               patient_id = EXCLUDED.patient_id,
                               business_type = EXCLUDED.business_type,
                               patient_name = EXCLUDED.patient_name,
                               amount = EXCLUDED.amount,
                               pay_status = EXCLUDED.pay_status,
                               created_at = EXCLUDED.created_at,
                               status = EXCLUDED.status;
