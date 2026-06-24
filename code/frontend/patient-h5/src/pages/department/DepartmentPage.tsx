import { List, SpinLoading, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchDepartments, type DepartmentItem } from "../../app/api";
import { SectionCard } from "../../components/SectionCard";

export function DepartmentPage() {
  const navigate = useNavigate();
  const [departments, setDepartments] = useState<DepartmentItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchDepartments()
      .then((records) => {
        if (!ignore) {
          setDepartments(records);
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
    <SectionCard title="选择科室" description="根据病情选择科室后，可继续进入医生列表。">
      {loading ? <SpinLoading /> : null}
      <List>
        {departments.map((department) => (
          <List.Item
            key={department.id}
            extra={<Tag color="warning">{department.queue}</Tag>}
            onClick={() => navigate(`/doctor/list?deptId=${department.id}&departmentName=${encodeURIComponent(department.name)}`)}
          >
            {department.name}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
