import React, { useState, useEffect } from 'react';
import { Card, Progress, Row, Col, Statistic, Table, Tag } from 'antd';

import { apiFetch } from '../api';
const DuminAdvanced: React.FC = () => {
    const [hwData, setHwData] = useState<any>(null);

    useEffect(() => {
        const timer = setInterval(async () => {
            const resp = await apiFetch('/api/admin/dumin/telemetry');
            if (resp.ok) setHwData(await resp.json());
        }, 5000);
        return () => clearInterval(timer);
    }, []);

    return (
        <div style={{ padding: '24px' }}>
            <h2>🔴 YOUNES Sovereign Hardware (System B)</h2>
            <Row gutter={16}>
                <Col span={8}>
                    <Card title="GSM يونس">
                        <Progress type="dashboard" percent={hwData?.signal || 0} strokeColor="#f57c00" />
                        <div style={{ textAlign: 'center' }}>Operator: {hwData?.operator || 'Searching...'}</div>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card title="SIM Status">
                        <Statistic title="Balance" value={hwData?.balance || 0} precision={2} prefix="$" />
                        <Tag color={hwData?.simPresent ? "green" : "red"}>
                            {hwData?.simPresent ? "SIM ACTIVE" : "SIM ERROR"}
                        </Tag>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card title="Hardware Load">
                        <Statistic title="Temp" value={hwData?.temp || 0} suffix="°C" />
                        <Progress percent={hwData?.load || 0} status="active" />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default DuminAdvanced;
