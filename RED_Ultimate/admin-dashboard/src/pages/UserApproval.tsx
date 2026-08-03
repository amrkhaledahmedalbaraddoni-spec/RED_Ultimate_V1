import React, { useState } from 'react';
import { Table, Tag, Button, Space, message, Modal } from 'antd';
import { CheckCircleOutlined, StopOutlined, DeleteOutlined } from '@ant-design/icons';

interface UserRecord {
  key: string;
  name: string;
  email: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'BANNED';
  regDate: string;
}

const UserApproval: React.FC = () => {
  const [data, setData] = useState<UserRecord[]>([
    { key: '1', name: 'John Doe', email: 'john@example.com', status: 'PENDING', regDate: '2026-07-31 10:00' },
    { key: '2', name: 'Alice Smith', email: 'alice@example.com', status: 'PENDING', regDate: '2026-07-31 10:05' },
  ]);

  const handleAction = (key: string, newStatus: string) => {
    message.success(`User status updated to ${newStatus}`);
    setData(data.map(item => item.key === key ? { ...item, status: newStatus as any } : item));
  };

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (status: string) => (
      <Tag color={status === 'PENDING' ? 'orange' : status === 'APPROVED' ? 'green' : 'red'}>{status}</Tag>
    )},
    { title: 'Registration Date', dataIndex: 'regDate', key: 'regDate' },
    { title: 'Action', key: 'action', render: (_: any, record: UserRecord) => (
      <Space size="middle">
        {record.status === 'PENDING' && (
          <>
            <Button type="primary" icon={<CheckCircleOutlined />} onClick={() => handleAction(record.key, 'APPROVED')}>Approve</Button>
            <Button danger icon={<StopOutlined />} onClick={() => handleAction(record.key, 'REJECTED')}>Reject</Button>
          </>
        )}
        {record.status === 'APPROVED' && (
          <Button danger type="dashed" onClick={() => handleAction(record.key, 'BANNED')}>Ban User</Button>
        )}
        <Button type="text" icon={<DeleteOutlined />} danger />
      </Space>
    )},
  ];

  return (
    <div style={{ padding: 24, background: '#fff' }}>
      <h2>Pending Account Approvals</h2>
      <Table columns={columns} dataSource={data.filter(u => u.status === 'PENDING')} />
      
      <h2 style={{ marginTop: 48 }}>All Users</h2>
      <Table columns={columns} dataSource={data} />
    </div>
  );
};

export default UserApproval;
