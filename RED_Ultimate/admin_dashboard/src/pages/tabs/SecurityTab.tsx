import React, { useState } from 'react';
import { Card, Row, Col, Statistic, Button, Modal, Input, Alert, Tag, Space, Table, message } from 'antd';
import { SafetyOutlined, WarningOutlined, DeleteOutlined, LockOutlined, ExclamationCircleOutlined } from '@ant-design/icons';

import { apiFetch } from '../../api';
const SecurityTab: React.FC = () => {
    const [killSwitchModal, setKillSwitchModal] = useState(false);
    const [wipeModal, setWipeModal] = useState(false);
    const [targetUserId, setTargetUserId] = useState('');
    const [reason, setReason] = useState('');

    const handleKillSwitch = () => {
        if (!reason) { message.error('Reason required'); return; }
        apiFetch(`/api/admin/security/kill-switch?reason=${encodeURIComponent(reason)}`, {
            method: 'POST'
        })
        .then(() => { message.success('Kill switch activated!'); setKillSwitchModal(false); })
        .catch(() => message.error('Failed'));
    };

    const handleWipe = () => {
        if (!targetUserId) { message.error('User ID required'); return; }
        apiFetch(`/api/admin/security/wipe?userId=${encodeURIComponent(targetUserId)}`, { method: 'POST' })
            .then(() => { message.success('Wipe signal sent!'); setWipeModal(false); })
            .catch(() => message.error('Failed'));
    };

    const securityEvents: any[] = []; // Populated only when the audit-log API is connected.

    return (
        <div>
            <Alert
                message="Security Operations Center"
                description="Manage device security, remote wipe, and emergency kill switch."
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
            />

            <Row gutter={[16, 16]}>
                <Col span={6}>
                    <Card>
                        <Statistic title="Threat Level" value="UNKNOWN" prefix={<SafetyOutlined />}
                            valueStyle={{ color: '#52c41a' }} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="Blocked Devices" value="—" prefix={<LockOutlined />}
                            valueStyle={{ color: '#ff4d4f' }} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="Active Sessions" value="—" prefix={<SafetyOutlined />}
                            valueStyle={{ color: '#1890ff' }} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="Security Score" value="N/A"
                            prefix={<SafetyOutlined />} valueStyle={{ color: '#52c41a' }} />
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
                <Col span={12}>
                    <Card title="⚡ Emergency Actions">
                        <Space direction="vertical" style={{ width: '100%' }}>
                            <Button danger block icon={<WarningOutlined />} size="large"
                                onClick={() => setKillSwitchModal(true)}>
                                🔴 KILL SWITCH — Wipe All Devices
                            </Button>
                            <Button type="primary" danger block icon={<DeleteOutlined />}
                                onClick={() => setWipeModal(true)}>
                                Remote Wipe — Single Device
                            </Button>
                        </Space>
                    </Card>
                </Col>
                <Col span={12}>
                    <Card title="Recent Security Events">
                        <Table
                            dataSource={securityEvents}
                            columns={[
                                { title: 'Event', dataIndex: 'event', key: 'event' },
                                { title: 'User', dataIndex: 'user', key: 'user' },
                                { title: 'Severity', dataIndex: 'severity', key: 'severity',
                                  render: (s: string) => <Tag color={s === 'warning' ? 'orange' : 'blue'}>{s}</Tag> },
                                { title: 'Time', dataIndex: 'time', key: 'time' },
                            ]}
                            pagination={false}
                            size="small"
                        />
                    </Card>
                </Col>
            </Row>

            <Modal title="⚠️ KILL SWITCH Confirmation" open={killSwitchModal}
                onOk={handleKillSwitch} onCancel={() => setKillSwitchModal(false)}
                okButtonProps={{ danger: true }}>
                <Alert message="This will WIPE ALL DEVICES immediately!" type="error" showIcon />
                <Input.TextArea style={{ marginTop: 16 }} placeholder="Reason for kill switch..."
                    value={reason} onChange={e => setReason(e.target.value)} rows={3} />
            </Modal>

            <Modal title="Remote Wipe — Single Device" open={wipeModal}
                onOk={handleWipe} onCancel={() => setWipeModal(false)}
                okButtonProps={{ danger: true }}>
                <Input placeholder="Target User ID" value={targetUserId}
                    onChange={e => setTargetUserId(e.target.value)} />
            </Modal>
        </div>
    );
};

export default SecurityTab;
