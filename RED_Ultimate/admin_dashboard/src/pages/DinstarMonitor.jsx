import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Badge, Progress, Table, Tag } from 'antd';
import { MobileOutlined, REDFilled } from '@ant-design/icons';

const DinstarMonitor = () => {
    const [slots, setSlots] = useState(Array(8).fill(null).map((_, i) => ({
        index: i + 1,
        status: 'IDLE',
        operator: 'Yemen Mobile',
        signal: 80,
        calls: 124
    })));

    return (
        <div style={{ padding: '24px' }}>
            <h1>🔴 DINSTAR UC2000-VE-8T Status</h1>
            <Row gutter={[16, 16]}>
                {slots.map(slot => (
                    <Col span={6} key={slot.index}>
                        <Card title={`SIM Slot ${slot.index}`} size="small">
                            <div style={{ textAlign: 'center', marginBottom: 12 }}>
                                <MobileOutlined style={{ fontSize: 32, color: slot.status === 'IDLE' ? '#52c41a' : '#f57c00' }} />
                                <div><Tag color={slot.status === 'IDLE' ? 'green' : 'orange'}>{slot.status}</Tag></div>
                            </div>
                            <Progress percent={slot.signal} size="small" strokeColor="#f57c00" title="RED" />
                            <div style={{ marginTop: 8, fontSize: 12 }}>
                                <b>Operator:</b> {slot.operator}<br/>
                                <b>Calls Today:</b> {slot.calls}
                            </div>
                        </Card>
                    </Col>
                ))}
            </Row>
        </div>
    );
};

export default DinstarMonitor;
