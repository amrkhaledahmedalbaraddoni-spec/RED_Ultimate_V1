import React, { useEffect, useState } from 'react';
import { Alert, Card, Col, Row, Spin, Statistic } from 'antd';
import { MessageOutlined, SafetyOutlined, UserOutlined, HddOutlined } from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import { apiFetch } from '../api';

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [error, setError] = useState('');
  useEffect(() => {
    const load = async () => {
      try {
        const [monitor, master] = await Promise.all([apiFetch('/api/admin/monitor/stats'), apiFetch('/api/master/v1/stats/realtime')]);
        if (!monitor.ok || !master.ok) throw new Error('monitor unavailable');
        setStats({ ...(await monitor.json()), ...(await master.json()) }); setError('');
      } catch { setError('تعذر قراءة المقاييس الحقيقية من الخادم'); }
    };
    load(); const timer = setInterval(load, 5000); return () => clearInterval(timer);
  }, []);

  if (!stats && !error) return <Spin size="large" style={{display:'block',margin:'100px auto'}}/>;
  if (!stats) return <Alert type="error" message={error} showIcon/>;
  const chart = { tooltip:{}, xAxis:{type:'category',data:['المستخدمون','الرسائل 24س','ذاكرة JVM %']}, yAxis:{type:'value'}, series:[{type:'bar',data:[stats.active_users||0,stats.messages_24h||0,stats.jvm_memory_percent||0],itemStyle:{color:'#F59E0B'}}] };
  return <div>
    {error && <Alert type="warning" message={error} showIcon style={{marginBottom:12}}/>}
    <Row gutter={[16,16]}>
      <Col span={6}><Card><Statistic title="المستخدمون المتصلون" value={stats.active_users||0} prefix={<UserOutlined/>}/></Card></Col>
      <Col span={6}><Card><Statistic title="رسائل آخر 24 ساعة" value={stats.messages_24h||0} prefix={<MessageOutlined/>}/></Card></Col>
      <Col span={6}><Card><Statistic title="طلبات الموافقة" value={stats.pending_approvals||0} prefix={<SafetyOutlined/>}/></Card></Col>
      <Col span={6}><Card><Statistic title="ذاكرة JVM" value={stats.jvm_memory_percent||0} suffix="%" prefix={<HddOutlined/>}/></Card></Col>
    </Row>
    <Row gutter={16} style={{marginTop:16}}><Col span={16}><Card title="مقاييس حية — لا بيانات تجريبية"><ReactECharts option={chart}/></Card></Col>
      <Col span={8}><Card title="صحة المنظومة"><Alert message={`PostgreSQL: ${stats.db_health||'UNKNOWN'}`} type={stats.db_health==='UP'?'success':'error'} showIcon/><Alert message={`Uptime: ${Math.round((stats.uptime_ms||0)/1000)} sec`} type="info" showIcon style={{marginTop:8}}/><Alert message={`CPU cores: ${stats.cpu_cores||0}`} type="info" showIcon style={{marginTop:8}}/></Card></Col></Row>
  </div>;
};
export default Dashboard;
