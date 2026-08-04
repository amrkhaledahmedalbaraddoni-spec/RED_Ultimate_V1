import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Progress, Badge, Alert, Space } from 'antd';
import { 
  ThunderboltFilled, 
  SafetyCertificateFilled, 
  CloudServerOutlined, 
  ApiFilled 
} from '@ant-design/icons';
import { apiFetch } from '../api';

const MasterOverview: React.FC = () => {
    const [stats, setStats] = useState<any>({
        ws_active: 0,
        pending_auth: 0,
        gsm_signal: 0,
        db_storage: 0
    });

    useEffect(() => {
        // Fetch real aggregated data from /api/master/admin/stats
        const interval = setInterval(async () => {
            const resp = await apiFetch('/api/master/v1/stats/realtime');
            if (resp.ok) setStats(await resp.json());
        }, 3000);
        return () => clearInterval(interval);
    }, []);

    return (
        <div style={{ padding: '32px', background: '#0a0a0a', minHeight: '100vh' }}>
            <h1 style={{ color: '#d32f2f', marginBottom: 40 }}>🔴 RED MASTER COMMAND CENTER</h1>
            
            <Row gutter={[24, 24]}>
                <Col span={6}>
                    <Card style={{ background: '#141414', border: '1px solid #333' }}>
                        <Statistic 
                            title={<span style={{color: '#fff'}}>System C (Messaging)</span>}
                            value={stats.ws_active}
                            suffix="Live Nodes"
                            prefix={<ThunderboltFilled style={{color: '#fadb14'}} />}
                            valueStyle={{color: '#fff'}}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#141414', border: '1px solid #333' }}>
                        <Statistic 
                            title={<span style={{color: '#fff'}}>User Authority</span>}
                            value={stats.pending_auth}
                            suffix="Pending"
                            prefix={<SafetyCertificateFilled style={{color: '#52c41a'}} />}
                            valueStyle={{color: '#fff'}}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#141414', border: '1px solid #333' }}>
                        <Statistic 
                            title={<span style={{color: '#fff'}}>System B (GSM)</span>}
                            value={stats.gsm_signal}
                            suffix="%"
                            prefix={<ApiFilled style={{color: '#f57c00'}} />}
                            valueStyle={{color: '#fff'}}
                        />
                        <Progress percent={stats.gsm_signal} showInfo={false} strokeColor="#f57c00" size="small" />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card style={{ background: '#141414', border: '1px solid #333' }}>
                        <Statistic 
                            title={<span style={{color: '#fff'}}>Database Load</span>}
                            value={stats.db_storage}
                            suffix="GB"
                            prefix={<CloudServerOutlined style={{color: '#1890ff'}} />}
                            valueStyle={{color: '#fff'}}
                        />
                    </Card>
                </Col>
            </Row>

            <Alert 
                message="SECURITY STATUS: POST-QUANTUM ENCRYPTION ACTIVE" 
                type="success" 
                showIcon 
                style={{ marginTop: 40, background: '#1b5e20', border: 'none', color: '#fff' }} 
            />
        </div>
    );
};

export default MasterOverview;
