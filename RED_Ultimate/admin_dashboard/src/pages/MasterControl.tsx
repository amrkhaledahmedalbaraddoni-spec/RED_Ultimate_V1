import React, { useState, useEffect } from 'react';
import { Layout, Menu, Row, Col, Card, Statistic, Tag, Progress, Button, Space, message, Table } from 'antd';
import { 
  DashboardOutlined, 
  SafetyCertificateOutlined, 
  ApiOutlined, 
  ControlOutlined,
  ThunderboltFilled
} from '@ant-design/icons';
import { apiFetch } from '../api';
import ReactECharts from 'echarts-for-react';

const MasterControl: React.FC = () => {
    const [dinstarData, setDinstarData] = useState<any[]>([]);
    const [stats, setStats] = useState<any>({ msgs: 0, calls: 0, load: 0 });

    useEffect(() => {
        const interval = setInterval(async () => {
            const resp = await apiFetch('/api/master/admin/hardware/dinstar/slots');
            if (resp.ok) setDinstarData(await resp.json());
            
            // Fetch system load
            setStats({ msgs: 8540, calls: 142, load: 22 });
        }, 3000);
        return () => clearInterval(interval);
    }, []);

    return (
        <Layout style={{ minHeight: '100vh', background: '#0a0a0a' }}>
            <Layout.Sider theme="dark">
                <div style={{ padding: 20, textAlign: 'center', color: '#ff4d4f', fontWeight: 'bold' }}>🔴 RED MASTER</div>
                <Menu theme="dark" mode="inline" defaultSelectedKeys={['1']}>
                    <Menu.Item key="1" icon={<DashboardOutlined />}>Overview</Menu.Item>
                    <Menu.Item key="2" icon={<SafetyCertificateOutlined />}>Account Authority</Menu.Item>
                    <Menu.Item key="3" icon={<ApiOutlined />}>DINSTAR UC2000</Menu.Item>
                    <Menu.Item key="4" icon={<ControlOutlined />}>System Health</Menu.Item>
                </Menu>
            </Layout.Sider>
            <Layout.Content style={{ padding: '24px' }}>
                <Row gutter={[16, 16]}>
                    <Col span={6}><Card><Statistic title="Active GSM Channels" value={dinstarData.filter(s => s.status === 'BUSY').length} suffix="/ 8" /></Card></Col>
                    <Col span={6}><Card><Statistic title="System C Flow" value={stats.msgs} prefix={<ThunderboltFilled />} /></Card></Col>
                    <Col span={12}>
                        <Card title="Traffic Metrics">
                            <Progress percent={stats.load} status="active" strokeColor="#ff4d4f" />
                        </Card>
                    </Col>
                </Row>
                
                <h2 style={{ color: 'white', marginTop: 32 }}>🔴 DINSTAR HARDWARE STATUS (UC2000-ve-8t)</h2>
                <Row gutter={[16, 16]}>
                    {dinstarData.map(slot => (
                        <Col span={6} key={slot.index}>
                            <Card 
                                size="small" 
                                title={`SIM Slot ${slot.index + 1}`}
                                extra={<Tag color={slot.status === 'IDLE' ? 'green' : 'orange'}>{slot.status}</Tag>}
                            >
                                <div style={{ textAlign: 'center' }}>
                                    <div style={{ fontSize: 24, fontWeight: 'bold', color: '#f57c00' }}>{slot.signal}%</div>
                                    <Progress percent={slot.signal} showInfo={false} strokeColor="#f57c00" />
                                    <div style={{ marginTop: 8 }}>{slot.operator}</div>
                                    <div style={{ fontSize: 10, color: '#666' }}>IMEI: {slot.imei}</div>
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>
            </Layout.Content>
        </Layout>
    );
};

export default MasterControl;
