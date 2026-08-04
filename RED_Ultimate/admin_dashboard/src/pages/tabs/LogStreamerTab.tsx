import React, { useEffect, useState, useRef } from 'react';
import { Card, Tag } from 'antd';
import { authStore } from '../../api';

const LogStreamerTab: React.FC = () => {
    const [logs, setLogs] = useState<string[]>([]);
    const scrollRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws';
        const token = encodeURIComponent(authStore.access() || '');
        const ws = new WebSocket(`${scheme}://${window.location.host}/ws/admin/logs?access_token=${token}`);
        ws.onmessage = (event) => {
            setLogs(prev => [...prev.slice(-100), event.data]); // Keep last 100 logs
        };
        return () => ws.close();
    }, []);

    useEffect(() => {
        if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }, [logs]);

    return (
        <Card title="🔴 LIVE SYSTEM LOGS (Sovereign Monitor)" style={{ background: '#000', border: '1px solid #333' }}>
            <div 
                ref={scrollRef}
                style={{ 
                    height: '400px', 
                    overflowY: 'auto', 
                    fontFamily: 'monospace', 
                    background: '#050505', 
                    padding: '16px',
                    color: '#00ff00' 
                }}
            >
                {logs.map((log, i) => (
                    <div key={i} style={{ marginBottom: '4px', borderBottom: '1px solid #111' }}>
                        <span style={{ color: '#888' }}>[{new Date().toLocaleTimeString()}]</span> {log}
                    </div>
                ))}
            </div>
        </Card>
    );
};

export default LogStreamerTab;
