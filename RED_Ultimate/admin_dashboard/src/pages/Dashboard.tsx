import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Spin, Alert } from 'antd';
import { UserOutlined, MessageOutlined, PhoneOutlined, SafetyOutlined } from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';

import { apiFetch } from '../api';
const Dashboard: React.FC = () => {
    const [stats, setStats] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const resp = await apiFetch('/api/admin/monitor/stats');
                const data = await resp.json();
                setStats(data);
                setLoading(false);
            } catch (err) {
                console.error("RED Admin: Connection failed.");
            }
        };
        fetchStats();
        const interval = setInterval(fetchStats, 5000);
        return () => clearInterval(interval);
    }, []);

    if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;

    const chartOption = {
        xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
        yAxis: { type: 'value' },
        series: [{ data: stats.weekly_messages || [150, 230, 224, 218, 135, 147, 260], type: 'line', color: '#ff4d4f' }]
    };

    return (
        <div style={{ padding: '24px' }}>
            <h1>🔴 RED SOVEREIGN MASTER DASHBOARD</h1>
            <Row gutter={16}>
                <Col span={6}>
                    <Card><Statistic title="Active Users" value={stats.active_users} prefix={<UserOutlined />} /></Card>
                </Col>
                <Col span={6}>
                    <Card><Statistic title="Messages Today" value={stats.messages_24h} prefix={<MessageOutlined />} /></Card>
                </Col>
                <Col span={6}>
                    <Card><Statistic title="GSM Active Calls" value={stats.gsm_active} prefix={<PhoneOutlined />} valueStyle={{ color: '#fa8c16' }} /></Card>
                </Col>
                <Col span={6}>
                    <Card><Statistic title="Pending Approvals" value={stats.pending_users} prefix={<SafetyOutlined />} valueStyle={{ color: '#f5222d' }} /></Card>
                </Col>
            </Row>

            <Row gutter={16} style={{ marginTop: 24 }}>
                <Col span={16}>
                    <Card title="Traffic Throughput (System C)"><ReactECharts option={chartOption} /></Card>
                </Col>
                <Col span={8}>
                    <Card title="Server Health">
                        <Alert message={`CPU: ${stats.cpu_load}%`} type="info" showIcon style={{ marginBottom: 8 }} />
                        <Alert message={`RAM: ${stats.ram_usage}MB`} type="info" showIcon />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default Dashboard;
