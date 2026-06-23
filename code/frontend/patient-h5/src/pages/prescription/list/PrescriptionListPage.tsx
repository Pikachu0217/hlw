import { List, SpinLoading, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import {
  fetchDrugs,
  fetchPrescriptions,
  fetchStocks,
  type DrugItem,
  type PrescriptionItem,
  type StockItem
} from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function PrescriptionListPage() {
  const [prescriptions, setPrescriptions] = useState<PrescriptionItem[]>([]);
  const [drugs, setDrugs] = useState<DrugItem[]>([]);
  const [stocks, setStocks] = useState<StockItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    Promise.all([fetchPrescriptions(), fetchDrugs(), fetchStocks()])
      .then(([prescriptionRecords, drugRecords, stockRecords]) => {
        if (!ignore) {
          setPrescriptions(prescriptionRecords);
          setDrugs(drugRecords);
          setStocks(stockRecords);
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
    <SectionCard title="我的处方" description="查看已开具处方、审核状态和配药进度。">
      {loading ? <SpinLoading /> : null}
      <List>
        {prescriptions.map((prescription) => (
          <List.Item key={prescription.id} extra={<Tag color={prescription.status.includes("待") ? "warning" : "success"}>{prescription.status}</Tag>}>
            {prescription.prescriptionNo} {prescription.doctorName}
          </List.Item>
        ))}
      </List>
      <List header="药品目录">
        {drugs.slice(0, 5).map((drug) => (
          <List.Item
            key={drug.id}
            description={`${drug.spec} · 库存 ${drug.inventory}${drug.unit}`}
            extra={<Tag color={drug.warningStatus.includes("预警") ? "warning" : "success"}>{drug.warningStatus}</Tag>}
          >
            {drug.drugName}
          </List.Item>
        ))}
      </List>
      <List header="库存状态">
        {stocks.slice(0, 5).map((stock) => (
          <List.Item key={stock.id} description={stock.warehouseName} extra={<Tag color="primary">{stock.warningStatus}</Tag>}>
            {stock.drugName} · {stock.inventory}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
