import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Tag, Space, Modal, message, Input } from 'antd';
import { CheckOutlined, CloseOutlined, DeleteOutlined } from '@ant-design/icons';

const AuthorityTab: React.FC = () => {
    const [pendingUsers, setPendingUsers] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);

    const fetchPending = () => {
        setLoading(true);
        fetch('/api/admin/users/pending')
            .then(res => res.json())
            .then(setPendingUsers)
            .catch(console.error)
            .finally(() => setLoading(false));
    };

    useEffect(() => { fetchPending(); }, []);

    const handleApprove = (userId: string) => {
        fetch(`/api/admin/users/approve/${userId}`, { method: 'POST' })
            .then(() => { message.success('User approved'); fetchPending(); })
            .catch(() => message.error('Failed to approve'));
    };

    const handleReject = (userId: string) => {
        fetch(`/api/admin/users/reject/${userId}`, { method: 'POST' })
            .then(() => { message.success('User rejected'); fetchPending(); })
            .catch(() => message.error('Failed to reject'));
    };

    const columns = [
        { title: 'User ID', dataIndex: 'userId', key: 'userId' },
        { title: 'Email', dataIndex: 'email', key: 'email' },
        { title: 'Registered', dataIndex: 'registeredAt', key: 'registeredAt',
          render: (v: string) => v ? new Date(v).toLocaleDateString() : '—' },
        { title: 'Status', dataIndex: 'status', key: 'status',
          render: (s: string) => <Tag color="orange">{s}</Tag> },
        { title: 'Actions', key: 'actions', render: (_: any, record: any) => (
            <Space>
                <Button type="primary" icon={<CheckOutlined />}
                    onClick={() => handleApprove(record.userId)}>Approve</Button>
                <Button danger icon={<CloseOutlined />}
                    onClick={() => handleReject(record.userId)}>Reject</Button>
            </Space>
        )},
    ];

    return (
        <Card title="User Approval Authority" extra={<Button onClick={fetchPending}>Refresh</Button>}>
            <Table
                dataSource={pendingUsers}
                columns={columns}
                rowKey="userId"
                loading={loading}
                pagination={{ pageSize: 10 }}
            />
        </Card>
    );
};

export default AuthorityTab;
