import { apiClient } from '@/api/client';
import type { HealthRecord, PatientRecord } from '@/pages/patient';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface CreatePatientPayload {
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

export interface UpdatePatientPayload extends CreatePatientPayload {}

export interface CreateHealthRecordPayload {
  patientId: number;
  title: string;
  summary: string;
  allergies?: string;
  history?: string;
  diagnosis?: string;
  remark?: string;
}

// 查询患者列表。
export async function fetchPatients(): Promise<PatientRecord[]> {
  console.info('[patient] 查询患者列表');
  const response = await apiClient.get<ApiResult<PatientRecord[]>>('/patient/patients');
  return response.data.data.map((patient) => ({
    ...patient,
    key: patient.key ?? String(patient.id),
  }));
}

// 查询患者详情。
export async function fetchPatientDetail(id: number): Promise<PatientRecord> {
  console.info('[patient] 查询患者详情', id);
  const response = await apiClient.get<ApiResult<PatientRecord>>(`/patient/patients/${id}`);
  const patient = response.data.data;
  return {
    ...patient,
    key: patient.key ?? String(patient.id),
  };
}

// 创建患者档案。
export async function createPatient(payload: CreatePatientPayload): Promise<PatientRecord> {
  console.info('[patient] 创建患者档案', payload);
  const response = await apiClient.post<ApiResult<PatientRecord>>('/patient/patients', payload);
  return response.data.data;
}

// 更新患者档案。
export async function updatePatient(id: number, payload: UpdatePatientPayload): Promise<PatientRecord> {
  console.info('[patient] 更新患者档案', id, payload);
  const response = await apiClient.put<ApiResult<PatientRecord>>(`/patient/patients/${id}`, payload);
  return response.data.data;
}

// 查询健康档案列表。
export async function fetchHealthRecords(patientId?: number): Promise<HealthRecord[]> {
  console.info('[patient] 查询健康档案列表', patientId);
  const response = await apiClient.get<ApiResult<HealthRecord[]>>('/patient/health-records', {
    params: patientId ? { patientId } : undefined,
  });
  return response.data.data.map((record) => ({
    ...record,
    key: record.key ?? String(record.id),
  }));
}

// 创建健康档案。
export async function createHealthRecord(payload: CreateHealthRecordPayload): Promise<HealthRecord> {
  console.info('[patient] 创建健康档案', payload);
  const response = await apiClient.post<ApiResult<HealthRecord>>('/patient/health-records', payload);
  return response.data.data;
}
