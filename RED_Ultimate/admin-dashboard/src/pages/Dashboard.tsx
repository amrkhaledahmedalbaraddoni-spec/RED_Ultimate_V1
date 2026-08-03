import React from 'react';
import { Layout, Menu, Card, Col, Row, Statistic, Table, Tag, Button, Space, message } from 'antd';
import { 
  UserOutlined, 
  MessageOutlined, 
  PhoneOutlined, 
  SafetyCertificateOutlined,
  BarChartOutlined,
  CloudServerOutlined 
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';

const { Header, Content, Sider } = Layout;

const Dashboard: React.FC = () => {
  // إحصائيات حية
  const stats = [
    { title: 'Active Users', value: 1254, icon: <UserOutlined />, color: '#1890ff' },
    { title: 'Messages Today', value: 85420, icon: <MessageOutlined />, color: '#52c41a' },
    { title: 'PSTN Calls', value: 342, icon: <PhoneOutlined />, color: '#fa8c16' },
    { title: 'Server Load', value: '24%', icon: <CloudServerOutlined />, color: '#eb2f96' },
  ];

  // رسم بياني للنشاط
  const chartOption = {
    title: { text: 'Real-time Activity' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'] },
    yAxis: { type: 'value' },
    series: [{ data: [820, 932, 901, 934, 1290, 1330], type: 'line', smooth: true, color: '#1890ff' }]
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible>
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', textAlign: 'center', color: 'white', fontWeight: 'bold' }}>RED Admin</div>
        <Menu theme="dark" defaultSelectedKeys={['1']} mode="inline">
          <Menu.Item key="1" icon={<BarChartOutlined />}>Dashboard</Menu.Item>
          <Menu.Item key="2" icon={<SafetyCertificateOutlined />}>Approvals</Menu.Item>
          <Menu.Item key="3" icon={<UserOutlined />}>User Management</Menu.Item>
          <Menu.Item key="4" icon={<PhoneOutlined />}>Dumin Control</Menu.Item>
          <Menu.Item key="5" icon={<CloudServerOutlined />}>Server Health</Menu.Item>
        </Menu>
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: 0 }} />
        <Content style={{ margin: '16px' }}>
          <Row gutter={16}>
            {stats.map((s, idx) => (
              <Col span={6} key={idx}>
                <Card>
                  <Statistic title={s.title} value={s.value} prefix={s.icon} valueStyle={{ color: s.color }} />
                </Card>
              </Col>
            ))}
          </Row>
          
          <Row gutter={16} style={{ marginTop: 24 }}>
            <Col span={16}>
              <Card title="System Throughput">
                <ReactECharts option={chartOption} />
              </Card>
            </Col>
            <Col span={8}>
              <Card title="Dumin Gateway Status">
                <Statistic title="SIM Status" value="Online" valueStyle={{ color: '#52c41a' }} />
                <Statistic title="Signal Strength" value="85%" style={{ marginTop: 16 }} />
                <Button type="primary" block style={{ marginTop: 24 }}>Restart Dumin Service</Button>
              </Card>
            </Col>
          </Row>
        </Content>
      </Layout>
    </Layout>
  );
};

export default Dashboard;
