import { List, Tag } from "antd-mobile";
import { SectionCard } from "../../components/SectionCard";

const departments = [
  { name: "儿科", queue: "当前等候 8 人" },
  { name: "内科", queue: "当前等候 5 人" },
  { name: "皮肤科", queue: "当前等候 3 人" }
];

export function DepartmentPage() {
  return (
    <SectionCard title="选择科室" description="根据病情选择科室后，可继续进入医生列表。">
      <List>
        {departments.map((department) => (
          <List.Item key={department.name} extra={<Tag color="warning">{department.queue}</Tag>}>
            {department.name}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
