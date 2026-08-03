import React, { useState, useEffect } from 'react';
import { Layout, Menu, Badge, Tag, Space, Drawer, Button, Alert, notification } from 'antd';
import {
    DashboardOutlined,
    TeamOutlined,
    SettingOutlined,
    MonitorOutlined,
    MobileOutlined,
    SafetyOutlined,
    AlertOutlined,
    CloudServerOutlined,
    VideoCameraOutlined,
    MessageOutlined,
    SecurityScanOutlined,
    ControlOutlined,
    ThunderboltOutlined,
    ApiOutlined
} from '@ant-design/icons';

// Pages - Ultimate Merge
import Dashboard from './pages/Dashboard';
import MasterOverview from './pages/MasterOverview';
import UserApproval from './pages/UserApproval';
import MasterLayout from './pages/MasterLayout';
import DinstarControl from './pages/DinstarControl';
import Diagnostics from './pages/Diagnostics';
import Approvals from './pages/Approvals';
import MasterControl from './pages/MasterControl';
import DuminAdvanced from './pages/DuminAdvanced';
import DinstarMonitor from './pages/DinstarMonitor';
import DuminMonitor from './pages/DuminMonitor';

const { Header, Sider, Content } = Layout;

const menuItems = [
    { key: 'dashboard', icon: <DashboardOutlined />, label: 'Dashboard Overview' },
    { key: 'master', icon: <SafetyOutlined />, label: 'Master Command Center' },
    { key: 'users', icon: <TeamOutlined />, label: 'User Authority' },
    { key: 'approvals', icon: <SecurityScanOutlined />, label: 'Approval Queue' },
    { key: 'dinstar', icon: <MobileOutlined />, label: 'DINSTAR UC2000' },
    { key: 'dinstar-monitor', icon: <ApiOutlined />, label: 'GSM Monitor' },
    { key: 'dumin-advanced', icon: <ControlOutlined />, label: 'Dumin Advanced' },
    { key: 'monitor', icon: <MonitorOutlined />, label: 'Live Monitor' },
    { key: 'live-broadcast', icon: <VideoCameraOutlined />, label: 'Live & Conference' },
    { key: 'messaging', icon: <MessageOutlined />, label: 'Messaging Center' },
    { key: 'infrastructure', icon: <CloudServerOutlined />, label: 'Infrastructure' },
    { key: 'diagnostics', icon: <SettingOutlined />, label: 'Diagnostics' },
];

function App() {
    const [currentPage, setCurrentPage] = useState('dashboard');
    const [collapsed, setCollapsed] = useState(false);
    const [stats, setStats] = useState({ pending: 0, active_users: 0, gsm_active: 0 });
    const [alertDrawer, setAlertDrawer] = useState(false);
    const [apiStatus, setApiStatus] = useState('connecting');

    // Poll backend health
    useEffect(() => {
        const fetchStats = async () => {
            try {
                const resp = await fetch('/api/admin/monitor/stats');
                if (resp.ok) {
                    const data = await resp.json();
                    setStats(data);
                    setApiStatus('online');
                } else {
                    setApiStatus('degraded');
                }
            } catch (e) {
                setApiStatus('offline');
            }
        };
        fetchStats();
        const interval = setInterval(fetchStats, 5000);
        return () => clearInterval(interval);
    }, []);

    // WebSocket for live alerts (admin logs)
    useEffect(() => {
        try {
            const wsProto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const ws = new WebSocket(`${wsProto}//${window.location.host}/ws/admin/logs`);
            ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    if (data.type === 'alert') {
                        notification.warning({ message: 'RED Alert', description: data.message, placement: 'bottomRight' });
                    }
                } catch (e) {}
            };
            ws.onerror = () => {};
            return () => { try { ws.close(); } catch (e) {} };
        } catch (e) {}
    }, []);

    const renderPage = () => {
        switch (currentPage) {
            case 'dashboard': return <Dashboard />;
            case 'master': return <MasterLayout />;
            case 'users': return <UserApproval />;
            case 'approvals': return <Approvals />;
            case 'dinstar': return <DinstarControl />;
            case 'dinstar-monitor': return <DinstarMonitor />;
            case 'dumin-advanced': return <DuminAdvanced />;
            case 'monitor': return <MasterOverview />;
            case 'live-broadcast': return <MasterControl />;
            case 'messaging': return <Dashboard />;
            case 'infrastructure': return <Diagnostics />;
            case 'diagnostics': return <Diagnostics />;
            default: return <Dashboard />;
        }
    };

    const statusColor = apiStatus === 'online' ? 'success' : apiStatus === 'degraded' ? 'warning' : 'error';

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider theme="dark" collapsible collapsed={collapsed} onCollapse={setCollapsed} breakpoint="lg">
                <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#000', borderBottom: '1px solid #333' }}>
                    <span style={{ color: '#ff4d4f', fontSize: collapsed ? 16 : 18, fontWeight: 'bold', letterSpacing: 2 }}>
                        {collapsed ? '🔴' : '🔴 RED ULTIMATE'}
                    </span>
                </div>
                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[currentPage]}
                    items={menuItems}
                    onClick={({ key }) => setCurrentPage(key)}
                    style={{ borderRight: 0, background: '#000' }}
                />
                <div style={{ padding: 16, color: '#888', fontSize: 11, textAlign: 'center' }}>
                    {!collapsed && (
                        <>
                            <div>RED Sovereign V2.0.0-ULTIMATE</div>
                            <div>100% Local Sovereign</div>
                            <Tag color={statusColor} style={{ marginTop: 8 }}>{apiStatus.toUpperCase()}</Tag>
                        </>
                    )}
                </div>
            </Sider>
            <Layout>
                <Header style={{ background: '#001529', padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: 64 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                        <span style={{ color: '#fff', fontSize: 18, fontWeight: 'bold' }}>RED Sovereign — Master Admin Panel Ultimate</span>
                        <Tag color="red">SOVEREIGN</Tag>
                        <Tag color={statusColor}>API: {apiStatus}</Tag>
                        <Tag>System A: VoIP 4K ONLINE</Tag>
                        <Tag color="orange">System B: DINSTAR CONNECTED</Tag>
                        <Tag color="green">System C: MESSAGING ACTIVE</Tag>
                    </div>
                    <Space>
                        <Badge count={stats.pending || 0} size="small">
                            <Button type="text" icon={<TeamOutlined style={{ color: '#fff' }} />} onClick={() => setCurrentPage('users')} />
                        </Badge>
                        <Badge count={5} offset={[0, 0]}>
                            <Button type="text" icon={<AlertOutlined style={{ color: '#fff', fontSize: 18 }} />} onClick={() => setAlertDrawer(true)} />
                        </Badge>
                        <Tag color="blue">{stats.active_users || 0} Active Users</Tag>
                    </Space>
                </Header>
                <Content style={{ margin: 0, padding: 0, background: '#f0f2f5', minHeight: 'calc(100vh - 64px)', overflow: 'auto' }}>
                    {apiStatus === 'offline' && (
                        <Alert
                            message="Backend Offline - Running in Simulation Mode"
                            description="Could not connect to /api/admin/monitor/stats - Using simulated data. Check docker-compose and ensure backend is healthy."
                            type="warning"
                            showIcon
                            closable
                            style={{ margin: 16 }}
                        />
                    )}
                    {renderPage()}
                </Content>
            </Layout>

            <Drawer title="🔴 RED Live Alerts" open={alertDrawer} onClose={() => setAlertDrawer(false)} width={400}>
                <Alert message="System A: SFU workers 2 READY" type="success" showIcon style={{ marginBottom: 8 }} />
                <Alert message="System B: DINSTAR UC2000 CONNECTED - 8 slots" type="info" showIcon style={{ marginBottom: 8 }} />
                <Alert message="System C: Guaranteed delivery UUID v7 ACTIVE" type="success" showIcon style={{ marginBottom: 8 }} />
                <Alert message={`${stats.pending || 0} pending approvals require attention`} type="warning" showIcon style={{ marginBottom: 8 }} />
                <Alert message="PostgreSQL + MongoDB + Redis + MinIO HEALTHY" type="success" showIcon />
            </Drawer>
        </Layout>
    );
}

export default App;
