import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Progress, Switch, Button, Modal, Input, message, Table, Tag } from 'antd';
import { SettingOutlined, PoweroffOutlined, REDFilled, HistoryOutlined } from '@ant-design/icons';

const DinstarControl: React.FC = () => {
    const [ports, setPorts] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);

    const fetchStatus = async () => {
        const resp = await fetch('/api/admin/dinstar/status');
        if (resp.ok) setPorts(await resp.json());
    };

    useEffect(() => { fetchStatus(); }, []);

    const handleReboot = () => {
        Modal.confirm({
            title: 'Confirm System Reboot',
            content: 'This will terminate all active GSM calls. Continue?',
            okText: 'Reboot Now',
            okType: 'danger',
            onOk: async () => {
                await fetch('/api/admin/dinstar/reboot', { method: 'POST' });
                message.warning('DINSTAR: Reboot command sent.');
            }
        });
    };

    return (
        <div style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
                <h1>🔴 DINSTAR UC2000 Command Center</h1>
                <Space>
                    <Button icon={<SettingOutlined />} onClick={() => {/* Open Config Modal */}}>Global Config</Button>
                    <Button type="primary" danger icon={<PoweroffOutlined />} onClick={handleReboot}>Reboot Device</Button>
                </Space>
            </div>

            <Row gutter={[16, 16]}>
                {ports.map((port) => (
                    <Col span={6} key={port.index}>
                        <Card 
                            title={`SIM Slot ${port.index + 1}`} 
                            extra={<Switch checked={port.status === 'READY'} size="small" />}
                            style={{ border: port.status === 'READY' ? '1px solid #52c41a' : '1px solid #f5222d' }}
                        >
                            <div style={{ textAlign: 'center' }}>
                                <REDFilled style={{ fontSize: 40, color: port.signal > 50 ? '#52c41a' : '#fadb14' }} />
                                <div style={{ marginTop: 8 }}>
                                    <Tag color="blue">{port.operator}</Tag>
                                    <Tag color="cyan">{port.signal}% RED</Tag>
                                </div>
                            </div>
                            <div style={{ marginTop: 16 }}>
                                <small>Status:</small> <b>{port.status}</b>
                                <Progress percent={port.signal} status={port.status === 'READY' ? 'active' : 'exception'} strokeColor="#f57c00" />
                            </div>
                        </Card>
                    </Col>
                ))}
            </Row>

            <Card title={<><HistoryOutlined /> GSM Call Activity</>} style={{ marginTop: 24 }}>
                <Table 
                    size="small"
                    dataSource={[]} // Real logs from backend
                    columns={[
                        { title: 'Time', dataIndex: 'time' },
                        { title: 'Slot', dataIndex: 'slot' },
                        { title: 'Target', dataIndex: 'target' },
                        { title: 'Duration', dataIndex: 'duration' },
                        { title: 'Operator', dataIndex: 'op' }
                    ]} 
                />
            </Card>
        </div>
    );
};

export default DinstarControl;
