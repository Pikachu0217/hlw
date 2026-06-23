import { Grid, NoticeBar, Space, Swiper, Tag } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { SectionCard } from "../../components/SectionCard";

const services = [
  { text: "选择医院", path: "/hospital" },
  { text: "科室导诊", path: "/department" },
  { text: "预约挂号", path: "/appointment/list" },
  { text: "图文问诊", path: "/consult/create" },
  { text: "我的处方", path: "/prescription/list" },
  { text: "我的订单", path: "/order/list" }
];

export function HomePage() {
  const navigate = useNavigate();

  return (
    <Space direction="vertical" block className="mobile-stack">
      <div className="hero-card">
        <Tag color="success">24 小时在线服务</Tag>
        <div className="hero-title">把挂号、问诊、购药装进手机里</div>
        <div className="hero-copy">覆盖医院选择、科室导诊、预约挂号、问诊聊天、处方查询与订单追踪。</div>
      </div>
      <NoticeBar content="儿童夜间门诊已开启线上咨询，请优先选择图文问诊。" color="alert" />
      <SectionCard title="热门入口" description="按照患者最常访问的主流程快速进入。">
        <Grid columns={3} gap={12}>
          {services.map((item) => (
            <Grid.Item key={item.text}>
              <div className="quick-entry" onClick={() => navigate(item.path)}>
                {item.text}
              </div>
            </Grid.Item>
          ))}
        </Grid>
      </SectionCard>
      <SectionCard title="本周推荐" description="围绕常见儿科与内科需求提供快捷服务。">
        <Swiper autoplay loop>
          {["儿科在线问诊", "慢病复诊续方", "便民门诊预约"].map((item) => (
            <Swiper.Item key={item}>
              <div className="promo-slide">{item}</div>
            </Swiper.Item>
          ))}
        </Swiper>
      </SectionCard>
    </Space>
  );
}
