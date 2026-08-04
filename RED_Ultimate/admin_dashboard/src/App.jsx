import React, { lazy, Suspense, useState } from 'react';
import { Button, Layout, Menu, Spin } from 'antd';
import { authStore } from './api';
import Login from './pages/Login';
import {
    DashboardOutlined,
    TeamOutlined,
    SettingOutlined,
    MonitorOutlined,
    MobileOutlined,
    SafetyOutlined
} from '@ant-design/icons';
const Dashboard = lazy(() => import('./pages/Dashboard'));
const MasterOverview = lazy(() => import('./pages/MasterOverview'));
const UserManagement = lazy(() => import('./pages/UserManagement'));
const MasterLayout = lazy(() => import('./pages/MasterLayout'));
const DinstarControl = lazy(() => import('./pages/DinstarControl'));
const Diagnostics = lazy(() => import('./pages/Diagnostics'));

const { Header, Sider, Content } = Layout;

const menuItems = [
    { key: 'dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
    { key: 'master', icon: <SafetyOutlined />, label: 'Master Control' },
    { key: 'users', icon: <TeamOutlined />, label: 'User Management' },
    { key: 'dinstar', icon: <MobileOutlined />, label: 'DINSTAR Control' },
    { key: 'monitor', icon: <MonitorOutlined />, label: 'Live Monitor' },
    { key: 'diagnostics', icon: <SettingOutlined />, label: 'Diagnostics' },
];

function App() {
    const [authenticated, setAuthenticated] = useState(Boolean(authStore.access() || authStore.refresh()));
    const [currentPage, setCurrentPage] = useState('dashboard');

    if (!authenticated) return <Login onSuccess={() => setAuthenticated(true)} />;

    const logout = () => { authStore.clear(); setAuthenticated(false); };

    const renderPage = () => {
        switch (currentPage) {
            case 'dashboard': return <Dashboard />;
            case 'master': return <MasterLayout />;
            case 'users': return <UserManagement />;
            case 'dinstar': return <DinstarControl />;
            case 'monitor': return <MasterOverview />;
            case 'diagnostics': return <Diagnostics />;
            default: return <Dashboard />;
        }
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider theme="dark" collapsible>
                <div style={{ height: 32, margin: 16, color: '#fff', fontSize: 18, textAlign: 'center', lineHeight: '32px' }}>
                    🔴 RED Admin
                </div>
                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[currentPage]}
                    items={menuItems}
                    onClick={({ key }) => setCurrentPage(key)}
                />
            </Sider>
            <Layout>
                <Header style={{ background: '#fff', padding: '0 16px', fontSize: 16, fontWeight: 'bold', display: 'flex', justifyContent: 'space-between' }}>
                    <span>RED Sovereign — Admin Panel</span>
                    <Button danger onClick={logout}>تسجيل الخروج</Button>
                </Header>
                <Content style={{ margin: 16, padding: 24, background: '#f5f5f5' }}>
                    <Suspense fallback={<div style={{display:'grid',placeItems:'center',minHeight:320}}><Spin size="large" /></div>}>
                        {renderPage()}
                    </Suspense>
                </Content>
            </Layout>
        </Layout>
    );
}

export default App;
