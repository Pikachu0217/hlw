import { http } from "./http";

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

interface BackendPageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface PatientProfile {
  id: number;
  name: string;
  maskedPhone: string;
  gender: string;
}

export interface HospitalItem {
  key: string;
  tenantId: string;
  companyName: string;
  status: string;
}

export interface DepartmentItem {
  id: number;
  name: string;
  doctorCount: number;
  queue: string;
}

export interface PatientDoctor {
  id: number;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  consultStatus: "ONLINE" | "BUSY" | "OFFLINE";
  consultFee: string;
  schedule: string;
}

interface BackendPatientDoctor {
  id: number;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  consultStatus?: "ONLINE" | "BUSY" | "OFFLINE";
  consultFee?: string;
  schedule?: string;
}

export interface ConsultMessageItem {
  id: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
}

interface BackendConsultMessage {
  id?: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
  senderId?: number;
}

export interface CreatedConsult {
  id: number;
  status: string;
  type: string;
  chiefComplaint: string;
}

export interface AppointmentItem {
  key: string;
  appointmentNo: string;
  patientName: string;
  doctorName: string;
  clinicTime: string;
  source: string;
  status: string;
}

export interface PrescriptionItem {
  key: string;
  prescriptionNo: string;
  patientName: string;
  doctorName: string;
  drugCount: number;
  issuedAt: string;
  status: string;
}

export interface OrderItem {
  key: string;
  orderNo: string;
  businessType: string;
  patientName: string;
  amount: string;
  payStatus: string;
  createdAt: string;
}

export async function fetchPatientProfile(): Promise<PatientProfile> {
  console.info("[patient] 查询患者档案");
  const response = await http.get<ApiResult<PatientProfile>>("/patient/profile");
  return response.data.data;
}

export async function fetchHospitals(): Promise<HospitalItem[]> {
  console.info("[patient] 查询医院租户列表");
  const response = await http.get<ApiResult<HospitalItem[]>>("/system/tenant/options");
  return response.data.data;
}

export async function fetchDepartments(): Promise<DepartmentItem[]> {
  console.info("[patient] 查询科室列表");
  const response = await http.get<ApiResult<DepartmentItem[]>>("/doctor/departments");
  return response.data.data;
}

export async function fetchPatientDoctors(): Promise<PatientDoctor[]> {
  console.info("[patient] 查询医生列表");
  const response = await http.get<ApiResult<BackendPatientDoctor[]>>("/doctor/doctors");

  return response.data.data.map((doctor) => ({
    id: doctor.id,
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    specialty: doctor.specialty,
    status: doctor.status,
    consultStatus: doctor.consultStatus ?? "OFFLINE",
    consultFee: doctor.consultFee ?? "0.00",
    schedule: doctor.schedule ?? "待确认"
  }));
}

export async function fetchPatientDoctorDetail(doctorId: number): Promise<PatientDoctor> {
  console.info("[patient] 查询医生详情", doctorId);
  const response = await http.get<ApiResult<BackendPatientDoctor>>(`/doctor/doctors/${doctorId}`);
  const doctor = response.data.data;

  return {
    id: doctor.id,
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    specialty: doctor.specialty,
    status: doctor.status,
    consultStatus: doctor.consultStatus ?? "OFFLINE",
    consultFee: doctor.consultFee ?? "0.00",
    schedule: doctor.schedule ?? "待确认"
  };
}

export async function createConsult(type: string, chiefComplaint: string): Promise<CreatedConsult> {
  console.info("[consult] 创建问诊", type, chiefComplaint);
  const response = await http.post<ApiResult<CreatedConsult>>("/consult/consults", {
    type,
    chiefComplaint
  });
  return response.data.data;
}

export async function fetchConsultMessages(consultId: number): Promise<ConsultMessageItem[]> {
  console.info("[consult] 查询问诊消息", consultId);
  const response = await http.get<ApiResult<BackendConsultMessage[]>>(
    `/consult/consults/${consultId}/messages`
  );

  return response.data.data.map((message, index) => ({
    id: message.id ?? index + 1,
    content: message.content,
    contentType: message.contentType
  }));
}

export async function createAppointment(doctorName: string, timeSlot: string): Promise<AppointmentItem> {
  console.info("[appointment] 创建预约", doctorName, timeSlot);
  const response = await http.post<ApiResult<AppointmentItem>>("/appointment/appointments", {
    doctorName,
    timeSlot
  });
  return response.data.data;
}

export async function fetchAppointments(): Promise<AppointmentItem[]> {
  console.info("[appointment] 查询预约列表");
  const response = await http.get<ApiResult<AppointmentItem[]>>("/appointment/appointments");
  return response.data.data;
}

export async function fetchPrescriptions(): Promise<PrescriptionItem[]> {
  console.info("[prescription] 查询处方列表");
  const response = await http.get<ApiResult<PrescriptionItem[]>>("/prescription/prescriptions");
  return response.data.data;
}

export async function fetchOrders(): Promise<OrderItem[]> {
  console.info("[order] 查询订单列表");
  const response = await http.get<ApiResult<OrderItem[]>>("/order/orders");
  return response.data.data;
}
