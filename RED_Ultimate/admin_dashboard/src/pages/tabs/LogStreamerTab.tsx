import React, { useEffect, useState, useRef } from 'react';
import { Card, Tag } from 'antd';

const LogStreamerTab: React.FC = () => {
    const [logs, setLogs] = useState<string[]>([]);
    const scrollRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const ws = new WebSocket(`ws://${window.location.hostname}:8080/ws/admin/logs`);
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
                    padding: '16.dp',
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
