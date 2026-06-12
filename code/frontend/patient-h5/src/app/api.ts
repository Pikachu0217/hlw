import { http } from "./http";

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface PatientProfile {
  id: number;
  name: string;
  maskedPhone: string;
  gender: string;
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

export async function fetchPatientProfile(): Promise<PatientProfile> {
  console.info("[patient] 查询患者档案");
  const response = await http.get<ApiResult<PatientProfile>>("/patient/profile");
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
    consultFee: doctor.consultFee ?? "0.00"
  }));
}

export async function createConsult(chiefComplaint: string): Promise<CreatedConsult> {
  console.info("[consult] 创建问诊", chiefComplaint);
  const response = await http.post<ApiResult<CreatedConsult>>("/consult/consults", {
    type: "IMAGE_TEXT",
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
