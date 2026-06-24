import { Button, List, Picker, Space, Tag, Toast } from "antd-mobile";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  createAppointment,
  type NumberSourceItem,
  type PatientDoctor,
  type PatientProfile,
  type ScheduleItem
} from "../../../app/api";

type AppointmentConfirmProps = {
  doctor: PatientDoctor | null;
  patient: PatientProfile | null;
  schedules: ScheduleItem[];
  numberSources: NumberSourceItem[];
  lockedSource: NumberSourceItem | null;
  onLockNumberSource: (scheduleId: number) => Promise<void>;
};

export function AppointmentConfirm({
  doctor,
  patient,
  schedules,
  numberSources,
  lockedSource,
  onLockNumberSource
}: AppointmentConfirmProps) {
  const navigate = useNavigate();
  const [scheduleId, setScheduleId] = useState<number | null>(null);
  const [locking, setLocking] = useState(false);
  const selectedSchedule = schedules.find((schedule) => schedule.id === scheduleId) ?? schedules[0];
  const scheduleOptions = useMemo(
    () =>
      schedules.map((schedule) => ({
        label: `${schedule.scheduleDate} ${schedule.timeSlot || schedule.slot}｜余号 ${schedule.remain}`,
        value: schedule.id
      })),
    [schedules]
  );

  async function handleLock(): Promise<void> {
    const nextScheduleId = selectedSchedule?.id;
    if (!nextScheduleId) {
      Toast.show("暂无可锁定排班");
      return;
    }

    try {
      setLocking(true);
      await onLockNumberSource(nextScheduleId);
      Toast.show("号源已锁定");
    } catch {
      Toast.show("号源锁定失败");
    } finally {
      setLocking(false);
    }
  }

  async function handleSubmit(): Promise<void> {
    if (!doctor || !selectedSchedule) {
      Toast.show("请先选择医生排班");
      return;
    }

    try {
      const appointment = await createAppointment({
        patientId: patient?.id,
        patientName: patient?.patientName,
        doctorId: doctor.doctorId,
        scheduleId: selectedSchedule.id,
        doctorName: doctor.name,
        timeSlot: selectedSchedule.timeSlot || selectedSchedule.slot || doctor.schedule,
        feeAmount: doctor.consultFee
      });
      navigate(`/appointment/result?appointmentNo=${appointment.appointmentNo}&status=${appointment.status}&appointmentId=${appointment.id}`);
    } catch {
      Toast.show("预约提交失败，请稍后重试");
    }
  }

  return (
    <div className="confirm-card">
      <Space direction="vertical" block>
        <Tag color="primary">预约确认</Tag>
        <div className="detail-title">{doctor?.name ?? "加载中"}</div>
        <div className="detail-copy">{doctor?.department ?? "正在读取科室"} · {doctor?.title ?? "医生"}</div>
        <div className="detail-copy">就诊人 {patient?.patientName ?? "加载中"}</div>
        <div className="detail-copy">挂号费 {doctor?.consultFee ?? "0.00"} 元</div>
        <Picker columns={[scheduleOptions]} value={selectedSchedule ? [selectedSchedule.id] : []} onConfirm={(values) => setScheduleId(Number(values[0]))}>
          {(items, { open }) => (
            <Button block onClick={open}>
              {items[0]?.label ?? "选择排班"}
            </Button>
          )}
        </Picker>
        <List className="appointment-source-list">
          {numberSources
            .filter((source) => !selectedSchedule || source.scheduleId === selectedSchedule.id)
            .slice(0, 4)
            .map((source) => (
              <List.Item key={source.id} extra={<Tag color={source.status === "LOCKED" ? "warning" : "success"}>{source.status}</Tag>}>
                {source.numberSeq} 号
              </List.Item>
            ))}
        </List>
        <Button block loading={locking} onClick={handleLock}>
          {lockedSource ? `已锁定 ${lockedSource.numberSeq} 号` : "锁定号源"}
        </Button>
        <Button color="primary" block onClick={handleSubmit}>
          确认提交
        </Button>
      </Space>
    </div>
  );
}
