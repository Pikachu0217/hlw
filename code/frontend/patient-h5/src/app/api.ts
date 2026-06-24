import { http } from "./http";

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

function asList<T>(value: T[] | null | undefined): T[] {
  return Array.isArray(value) ? value : [];
}

export interface LoginResult {
  token: string;
  tenantId: number;
  username: string;
  realName: string;
  userType: string;
}

/** 发送手机验证码。 */
export async function sendPhoneCode(phone: string, tenantId?: string): Promise<void> {
  console.info("[auth] 发送验证码", phone);
  await http.post<ApiResult<null>>("/auth/phone-code", { phone, tenantId: tenantId ? Number(tenantId) : undefined });
}

/** 手机号+验证码登录。 */
export async function phoneLogin(phone: string, smsCode: string, tenantId?: string): Promise<LoginResult> {
  console.info("[auth] 手机号登录", phone);
  const response = await http.post<ApiResult<LoginResult>>("/auth/phone-login", {
    phone,
    smsCode,
    tenantId: tenantId ? Number(tenantId) : undefined
  });
  return response.data.data;
}

/** 切换当前登录租户。 */
export async function switchTenant(tenantId: string): Promise<LoginResult> {
  console.info("[auth] 切换登录租户", tenantId);
  const response = await http.post<ApiResult<LoginResult>>("/auth/switch-tenant", { tenantId: Number(tenantId) });
  return response.data.data;
}

export interface PatientProfile {
  id: number;
  userId?: string;
  patientName: string;
  name?: string;
  phone?: string;
  maskedPhone: string;
  gender: string;
  age?: number;
  riskLevel?: string;
  idCard?: string;
  birthday?: string;
  address?: string;
  lastVisit?: string;
  healthRecordCount?: number;
  latestRecordSummary?: string;
  updateTime?: string;
}

export interface HospitalItem {
  id: number;
  tenantId: string;
  companyName: string;
  status: string;
}

let hospitalOptionsRequest: Promise<HospitalItem[]> | null = null;

export interface DepartmentItem {
  id: number;
  name: string;
  doctorCount: number;
  queue: string;
}

export interface PatientDoctor {
  id: string;
  doctorId?: number;
  userId?: string;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  consultStatus: "ONLINE" | "BUSY" | "OFFLINE";
  consultFee: string;
  schedule: string;
  patientCount?: number;
}

interface BackendPatientDoctor {
  id: string;
  doctorId?: number;
  userId?: string;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  consultStatus?: "ONLINE" | "BUSY" | "OFFLINE";
  consultFee?: string;
  schedule?: string;
  patientCount?: number;
}

export interface ConsultMessageItem {
  id?: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
  senderType?: string;
}

interface BackendConsultMessage {
  id?: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
  senderId?: number;
  senderType?: string;
}

export interface CreatedConsult {
  id: number;
  consultNo?: string;
  status: string;
  patientName?: string;
  doctorName?: string;
  channel?: string;
  updatedAt?: string;
}

export interface AppointmentItem {
  id: number;
  appointmentNo: string;
  patientName: string;
  doctorName: string;
  clinicTime: string;
  source: string;
  status: string;
  feeAmount?: string;
}

export interface NumberSourceItem {
  id: number;
  scheduleId: number;
  numberSeq: number;
  status: string;
}

export interface ScheduleItem {
  id: number;
  doctorId: number;
  doctorName: string;
  slot: string;
  scheduleDate: string;
  timeSlot: string;
  totalNumber: number;
  remain: number;
}

export interface PrescriptionItem {
  id: number;
  prescriptionNo: string;
  patientName: string;
  doctorName: string;
  drugCount: number;
  issuedAt: string;
  status: string;
}

export interface OrderItem {
  id: number;
  orderNo: string;
  businessType: string;
  patientName: string;
  amount: string;
  payStatus: string;
  createdAt: string;
}

export interface HealthRecordItem {
  id: number;
  patientId: number;
  title: string;
  summary: string;
  allergies?: string;
  history?: string;
  diagnosis?: string;
  remark?: string;
  createTime?: string;
}

export interface DrugItem {
  id: number;
  drugName: string;
  spec: string;
  inventory: number;
  unit: string;
  warningStatus: string;
}

export interface StockItem {
  id: number;
  drugName: string;
  warehouseName: string;
  inventory: number;
  warningStatus: string;
}

export interface UpdatePatientProfilePayload {
  patientName: string;
  gender: string;
  age: number;
  phone: string;
  riskLevel?: string;
  idCard?: string;
  birthday?: string;
  address?: string;
  lastVisit?: string;
}

export interface CreateHealthRecordPayload {
  patientId: number;
  title: string;
  summary: string;
  allergies?: string;
  history?: string;
  diagnosis?: string;
  remark?: string;
}

export interface CreateOrderPayload {
  bizType: string;
  businessType?: string;
  bizId?: number;
  patientId?: number;
  patientName?: string;
  amount: string;
  createdAt?: string;
}

export async function fetchPatientProfile(): Promise<PatientProfile> {
  console.info("[patient] 查询患者档案");
  const response = await http.get<ApiResult<PatientProfile>>("/patient/profile");
  const profile = response.data.data;
  return {
    ...profile,
    name: profile.name ?? profile.patientName
  };
}

export async function updatePatientProfile(payload: UpdatePatientProfilePayload): Promise<PatientProfile> {
  console.info("[patient] 更新当前患者档案", payload.patientName);
  const response = await http.put<ApiResult<PatientProfile>>("/patient/profile", payload);
  const profile = response.data.data;
  return {
    ...profile,
    name: profile.name ?? profile.patientName
  };
}

export async function fetchPatients(): Promise<PatientProfile[]> {
  console.info("[patient] 查询就诊人列表");
  const response = await http.get<ApiResult<PatientProfile[]>>("/patient/patients");
  return asList(response.data.data).map((profile) => ({
    ...profile,
    name: profile.name ?? profile.patientName
  }));
}

export async function fetchPatientDetail(patientId: number): Promise<PatientProfile> {
  console.info("[patient] 查询就诊人详情", patientId);
  const response = await http.get<ApiResult<PatientProfile>>(`/patient/patients/${patientId}`);
  const profile = response.data.data;
  return {
    ...profile,
    name: profile.name ?? profile.patientName
  };
}

export async function updatePatient(patientId: number, payload: UpdatePatientProfilePayload): Promise<PatientProfile> {
  console.info("[patient] 更新就诊人档案", patientId, payload.patientName);
  const response = await http.put<ApiResult<PatientProfile>>(`/patient/patients/${patientId}`, payload);
  const profile = response.data.data;
  return {
    ...profile,
    name: profile.name ?? profile.patientName
  };
}

export async function fetchHealthRecords(patientId?: number): Promise<HealthRecordItem[]> {
  console.info("[patient] 查询健康档案列表", patientId);
  const response = await http.get<ApiResult<HealthRecordItem[]>>("/patient/health-records", {
    params: patientId ? { patientId } : undefined
  });
  return asList(response.data.data);
}

export async function createHealthRecord(payload: CreateHealthRecordPayload): Promise<HealthRecordItem> {
  console.info("[patient] 创建健康档案", payload.patientId, payload.title);
  const response = await http.post<ApiResult<HealthRecordItem>>("/patient/health-records", payload);
  return response.data.data;
}

export async function fetchHospitals(): Promise<HospitalItem[]> {
  console.info("[patient] 查询医院租户列表");
  if (!hospitalOptionsRequest) {
    hospitalOptionsRequest = http
      .get<ApiResult<HospitalItem[]>>("/system/tenant/options")
      .then((response) => asList(response.data.data))
      .finally(() => {
        hospitalOptionsRequest = null;
      });
  }
  return hospitalOptionsRequest;
}

export async function fetchDepartments(): Promise<DepartmentItem[]> {
  console.info("[patient] 查询科室列表");
  const response = await http.get<ApiResult<DepartmentItem[]>>("/doctor/departments");
  return asList(response.data.data);
}

export async function fetchPatientDoctors(): Promise<PatientDoctor[]> {
  console.info("[patient] 查询医生列表");
  const response = await http.get<ApiResult<BackendPatientDoctor[]>>("/doctor/doctors");

  return asList(response.data.data).map((doctor) => ({
    id: doctor.id,
    doctorId: doctor.doctorId,
    userId: doctor.userId,
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    specialty: doctor.specialty,
    status: doctor.status,
    consultStatus: doctor.consultStatus ?? "OFFLINE",
    consultFee: doctor.consultFee ?? "0.00",
    schedule: doctor.schedule ?? "待确认",
    patientCount: doctor.patientCount
  }));
}

export async function fetchPatientDoctorDetail(doctorId: string): Promise<PatientDoctor> {
  console.info("[patient] 查询医生详情", doctorId);
  const response = await http.get<ApiResult<BackendPatientDoctor>>(`/doctor/doctors/${doctorId}`);
  const doctor = response.data.data;

  return {
    id: doctor.id,
    doctorId: doctor.doctorId,
    userId: doctor.userId,
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    specialty: doctor.specialty,
    status: doctor.status,
    consultStatus: doctor.consultStatus ?? "OFFLINE",
    consultFee: doctor.consultFee ?? "0.00",
    schedule: doctor.schedule ?? "待确认",
    patientCount: doctor.patientCount
  };
}

export async function fetchSchedules(): Promise<ScheduleItem[]> {
  console.info("[doctor] 查询医生排班列表");
  const response = await http.get<ApiResult<ScheduleItem[]>>("/doctor/schedules");
  return asList(response.data.data);
}

export async function resolveAppointmentFee(title?: string, doctorFee?: string): Promise<string> {
  console.info("[doctor] 计算挂号费", title, doctorFee);
  const response = await http.post<ApiResult<number | string>>("/doctor/appointment-fee/resolve", {
    title,
    doctorFee
  });
  return String(response.data.data ?? "0.00");
}

/** 字典数据项（来自系统字典表）。 */
export interface DictItem {
  /** 字典标签。 */
  dictLabel: string;
  /** 字典键值。 */
  dictValue: string;
}

/** 根据字典类型查询字典数据列表。 */
export async function fetchDictByType(dictType: string): Promise<DictItem[]> {
  console.info("[system] 查询字典数据", dictType);
  const response = await http.get<ApiResult<DictItem[]>>(`/system/dict/type/${dictType}`);
  return response.data.data ?? [];
}

export async function fetchConsults(): Promise<CreatedConsult[]> {
  console.info("[consult] 查询问诊单列表");
  const response = await http.get<ApiResult<CreatedConsult[]>>("/consult/consults");
  return response.data.data;
}

export async function createConsult(
  type: string,
  chiefComplaint: string,
  doctor?: Pick<PatientDoctor, "id" | "doctorId" | "name" | "consultFee">
): Promise<CreatedConsult> {
  console.info("[consult] 创建问诊", type, chiefComplaint);
  const response = await http.post<ApiResult<CreatedConsult>>("/consult/consults", {
    type,
    chiefComplaint,
    doctorId: doctor?.doctorId,
    doctorName: doctor?.name,
    channel: "PATIENT_H5",
    feeAmount: doctor?.consultFee
  });
  return response.data.data;
}

export async function fetchConsultMessages(consultId: number): Promise<ConsultMessageItem[]> {
  console.info("[consult] 查询问诊消息", consultId);
  const response = await http.get<ApiResult<BackendConsultMessage[]>>(
    `/consult/consults/${consultId}/messages`
  );

  return asList(response.data.data).map((message, index) => ({
    id: message.id ?? index + 1,
    content: message.content,
    contentType: message.contentType,
    senderType: message.senderType
  }));
}

export async function completeConsult(consultId: number): Promise<CreatedConsult> {
  console.info("[consult] 完成问诊", consultId);
  const response = await http.post<ApiResult<CreatedConsult>>(`/consult/consults/${consultId}/complete`);
  return response.data.data;
}

export async function extendConsult(consultId: number): Promise<CreatedConsult> {
  console.info("[consult] 延长问诊", consultId);
  const response = await http.post<ApiResult<CreatedConsult>>(`/consult/consults/${consultId}/extend`);
  return response.data.data;
}

export async function createAppointment(payload: {
  patientId?: number;
  patientName?: string;
  doctorId?: number;
  departmentId?: number;
  scheduleId?: number;
  doctorName: string;
  timeSlot: string;
  source?: string;
  appointmentType?: string;
  feeAmount?: string;
}): Promise<AppointmentItem> {
  console.info("[appointment] 创建预约", payload.doctorName, payload.timeSlot);
  const response = await http.post<ApiResult<AppointmentItem>>("/appointment/appointments", {
    source: "PATIENT_H5",
    appointmentType: "ONLINE",
    ...payload
  });
  return response.data.data;
}

export async function fetchAppointments(): Promise<AppointmentItem[]> {
  console.info("[appointment] 查询预约列表");
  const response = await http.get<ApiResult<AppointmentItem[]>>("/appointment/appointments");
  return asList(response.data.data);
}

export async function payAppointment(appointmentId: number): Promise<AppointmentItem> {
  console.info("[appointment] 支付预约单", appointmentId);
  const response = await http.post<ApiResult<AppointmentItem>>(`/appointment/appointments/${appointmentId}/pay`);
  return response.data.data;
}

export async function checkInAppointment(appointmentId: number): Promise<AppointmentItem> {
  console.info("[appointment] 预约签到", appointmentId);
  const response = await http.post<ApiResult<AppointmentItem>>(`/appointment/appointments/${appointmentId}/check-in`);
  return response.data.data;
}

export async function grabAppointment(appointmentId: number, doctorId: number): Promise<boolean> {
  console.info("[appointment] 抢便民门诊预约单", appointmentId, doctorId);
  const response = await http.post<ApiResult<boolean>>(`/appointment/appointments/${appointmentId}/grab`, { doctorId });
  return response.data.data;
}

export async function fetchNumberSources(): Promise<NumberSourceItem[]> {
  console.info("[appointment] 查询号源列表");
  const response = await http.get<ApiResult<NumberSourceItem[]>>("/appointment/number-sources");
  return asList(response.data.data);
}

export async function lockNumberSource(scheduleId: number): Promise<NumberSourceItem> {
  console.info("[appointment] 锁定号源", scheduleId);
  const response = await http.post<ApiResult<NumberSourceItem>>(`/appointment/number-sources/${scheduleId}/lock`);
  return response.data.data;
}

export async function fetchPrescriptions(): Promise<PrescriptionItem[]> {
  console.info("[prescription] 查询处方列表");
  const response = await http.get<ApiResult<PrescriptionItem[]>>("/prescription/prescriptions");
  return asList(response.data.data);
}

export async function fetchDrugs(): Promise<DrugItem[]> {
  console.info("[drug] 查询药品列表");
  const response = await http.get<ApiResult<DrugItem[]>>("/drug/drugs");
  return asList(response.data.data);
}

export async function fetchStocks(): Promise<StockItem[]> {
  console.info("[drug] 查询库存列表");
  const response = await http.get<ApiResult<StockItem[]>>("/drug/stocks");
  return asList(response.data.data);
}

export async function fetchOrders(): Promise<OrderItem[]> {
  console.info("[order] 查询订单列表");
  const response = await http.get<ApiResult<OrderItem[]>>("/order/orders");
  return asList(response.data.data);
}

export async function createOrder(payload: CreateOrderPayload): Promise<OrderItem> {
  console.info("[order] 创建订单", payload.bizType, payload.bizId);
  const response = await http.post<ApiResult<OrderItem>>("/order/orders", {
    ...payload,
    businessType: payload.businessType ?? payload.bizType
  });
  return response.data.data;
}

export async function payOrder(orderId: number, payMethod: string): Promise<OrderItem> {
  console.info("[order] 支付订单", orderId, payMethod);
  const response = await http.post<ApiResult<OrderItem>>(`/order/orders/${orderId}/pay`, { payMethod });
  return response.data.data;
}
