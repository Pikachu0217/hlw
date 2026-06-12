import { Tree } from "antd";
import { PageSection } from "../../../components/PageSection";

const treeData = [
  { title: "工作台", key: "dashboard" },
  {
    title: "业务中台",
    key: "business",
    children: [
      { title: "医生中心", key: "doctor" },
      { title: "在线问诊", key: "consult" },
      { title: "预约挂号", key: "appointment" },
      { title: "处方审核", key: "prescription" },
      { title: "药品库存", key: "drug" },
      { title: "订单中心", key: "order" }
    ]
  }
];

export function MenuPage() {
  return (
    <PageSection title="菜单配置" description="用于定义管理端导航结构与按钮级权限能力。">
      <Tree defaultExpandAll treeData={treeData} />
    </PageSection>
  );
}
