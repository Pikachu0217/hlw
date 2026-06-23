import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  fetchNumberSources,
  fetchPatientDoctorDetail,
  fetchPatientProfile,
  fetchSchedules,
  lockNumberSource,
  type NumberSourceItem,
  type PatientDoctor,
  type PatientProfile,
  type ScheduleItem
} from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";
import { AppointmentConfirm } from "./AppointmentConfirm";

export function AppointmentConfirmPage() {
  const [searchParams] = useSearchParams();
  const doctorId = Number(searchParams.get("doctorId") ?? 1);
  const [doctor, setDoctor] = useState<PatientDoctor | null>(null);
  const [profile, setProfile] = useState<PatientProfile | null>(null);
  const [schedules, setSchedules] = useState<ScheduleItem[]>([]);
  const [numberSources, setNumberSources] = useState<NumberSourceItem[]>([]);
  const [lockedSource, setLockedSource] = useState<NumberSourceItem | null>(null);

  useEffect(() => {
    let ignore = false;

    Promise.all([fetchPatientDoctorDetail(doctorId), fetchPatientProfile(), fetchSchedules(), fetchNumberSources()]).then(
      ([doctorRecord, profileRecord, scheduleRecords, sourceRecords]) => {
        if (!ignore) {
          const matchedDoctorIds = [doctorRecord.id, doctorRecord.doctorId].filter(Boolean);
          setDoctor(doctorRecord);
          setProfile(profileRecord);
          setSchedules(scheduleRecords.filter((schedule) => matchedDoctorIds.includes(schedule.doctorId)));
          setNumberSources(sourceRecords);
        }
      }
    );

    return () => {
      ignore = true;
    };
  }, [doctorId]);

  async function handleLockNumberSource(scheduleId: number): Promise<void> {
    const source = await lockNumberSource(scheduleId);
    setLockedSource(source);
    const records = await fetchNumberSources();
    setNumberSources(records);
  }

  return (
    <SectionCard title="预约挂号确认" description="确认医生、排班、号源和费用后即可提交预约。">
      <AppointmentConfirm
        doctor={doctor}
        patient={profile}
        schedules={schedules}
        numberSources={numberSources}
        lockedSource={lockedSource}
        onLockNumberSource={handleLockNumberSource}
      />
    </SectionCard>
  );
}
