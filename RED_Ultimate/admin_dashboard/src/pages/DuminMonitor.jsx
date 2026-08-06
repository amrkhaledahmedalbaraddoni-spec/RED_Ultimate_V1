import React, { useState, useEffect } from 'react';
import { Card, Progress, Statistic, Row, Col, Tag, Button } from 'antd';
import { REDFilled, MobileOutlined, ThunderboltFilled } from '@ant-design/icons';

const DuminMonitor = () => {
    const [duminState, setDuminState] = useState({
        simStatus: 'Active',
        signal: 85,
        balance: '120.50',
        operator: 'GSM-Global',
        temp: 38
    });

    return (
        <div style={{ padding: '24px' }}>
            <h1>PSTN / Dumin Hardware Monitor</h1>
            <Row gutter={16}>
                <Col span={8}>
                    <Card>
                        <Statistic title="SIM Status" value={duminState.simStatus} prefix={<MobileOutlined />} 
                            valueStyle={{ color: '#3f51b5' }} />
                        <Tag color="green" style={{ marginTop: 8 }}>ONLINE</Tag>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card>
                        <Statistic title="YOUNES Strength" value={duminState.signal} suffix="/ 100" prefix={<REDFilled />} />
                        <Progress percent={duminState.signal} status="active" strokeColor="#f57c00" />
                    </Card>
                </Col>
                <Col span={8}>
                    <Card>
                        <Statistic title="SIM Balance" value={duminState.balance} precision={2} prefix="$" />
                        <Button type="primary" size="small" style={{ marginTop: 8 }}>Recharge Now</Button>
                    </Card>
                </Col>
            </Row>
            <Card title="Hardware Health" style={{ marginTop: 24 }}>
                <p>Temperature: {duminState.temp}°C <Tag color="orange">Normal</Tag></p>
                <p>Uptime: 14 days, 5 hours, 22 minutes</p>
                <Button danger>Restart Dumin Hardware</Button>
            </Card>
        </div>
    );
};

export default DuminMonitor;
