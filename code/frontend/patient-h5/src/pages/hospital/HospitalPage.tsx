import { List, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchHospitals, switchTenant, type HospitalItem } from "../../app/api";
import { SectionCard } from "../../components/SectionCard";
import { useSessionStore } from "../../store/sessionStore";

export function HospitalPage() {
  const navigate = useNavigate();
  const selectedTenantId = useSessionStore((state) => state.tenantId);
  const setTenant = useSessionStore((state) => state.setTenant);
  const setToken = useSessionStore((state) => state.setToken);
  const setPatientName = useSessionStore((state) => state.setPatientName);
  const isLoggedIn = useSessionStore((state) => state.isLoggedIn);
  const [hospitals, setHospitals] = useState<HospitalItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [switchingTenantId, setSwitchingTenantId] = useState("");

  async function handleSelectHospital(hospital: HospitalItem): Promise<void> {
    if (hospital.status !== "0") {
      Toast.show("该医院暂未开放服务");
      return;
    }
    if (!isLoggedIn) {
      setTenant(hospital.tenantId, hospital.companyName);
      Toast.show(`已选择${hospital.companyName}`);
      navigate("/login", { replace: true });
      return;
    }
    if (hospital.tenantId === selectedTenantId) {
      navigate("/", { replace: true });
      return;
    }
    setSwitchingTenantId(hospital.tenantId);
    try {
      const result = await switchTenant(hospital.tenantId);
      setToken(result.token);
      setPatientName(result.realName);
      setTenant(hospital.tenantId, hospital.companyName);
      Toast.show(`已切换到${hospital.companyName}`);
      navigate("/", { replace: true });
    } catch {
      Toast.show("切换医院失败，请稍后重试");
    } finally {
      setSwitchingTenantId("");
    }
  }

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchHospitals()
      .then((records) => {
        if (!ignore) {
          setHospitals(records);
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <SectionCard title="选择医院" description="支持多租户医院切换与服务能力展示。">
      {loading ? <SpinLoading /> : null}
      <List>
        {hospitals.map((hospital) => (
          <List.Item
            key={hospital.id}
            clickable={hospital.status === "0" && !switchingTenantId}
            arrow={hospital.status === "0"}
            onClick={() => handleSelectHospital(hospital)}
            description={
              <Space>
                <Tag color={hospital.tenantId === selectedTenantId ? "success" : "primary"}>
                  {switchingTenantId === hospital.tenantId
                    ? "切换中"
                    : hospital.tenantId === selectedTenantId
                      ? "当前医院"
                      : hospital.status === "0" ? "可选租户" : "暂停服务"}
                </Tag>
                <span>{hospital.tenantId}</span>
              </Space>
            }
          >
            {hospital.companyName}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
