import { List, Tag } from "antd-mobile";
import { SectionCard } from "../../../components/SectionCard";

export function PrescriptionListPage() {
  return (
    <SectionCard title="我的处方" description="查看已开具处方、审核状态和配药进度。">
      <List>
        <List.Item extra={<Tag color="success">已审核</Tag>}>处方 #10021 儿科感冒用药</List.Item>
        <List.Item extra={<Tag color="warning">待发药</Tag>}>处方 #10022 慢病复诊续方</List.Item>
      </List>
    </SectionCard>
  );
}
