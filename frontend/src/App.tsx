import { useState, useEffect, useRef } from 'react';
import './App.css';

interface Message {
  role: 'user' | 'bot';
  content: string;
  source?: string;
}

function App() {
  const [messages, setMessages] = useState<Message[]>([
    { role: 'bot', content: 'Hello! I am your Log Analysis Bot. Ask me about errors in your logs.' }
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(scrollToBottom, [messages]);

  const handleIngest = async () => {
    setIsLoading(true);
    try {
      await fetch('http://localhost:9090/api/logs/ingest', { method: 'POST' });
      setMessages(prev => [...prev, { role: 'bot', content: '✅ Log ingestion triggered successfully.' }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { role: 'bot', content: '❌ Failed to trigger ingestion.' }]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSend = async () => {
    if (!input.trim()) return;

    const userMsg = input;
    setMessages(prev => [...prev, { role: 'user', content: userMsg }]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await fetch(`http://localhost:9090/api/logs/query?q=${encodeURIComponent(userMsg)}`);
      const data = await response.json();

      const results = data.results as string[];
      let botResponse = '';

      if (results && results.length > 0) {
        botResponse = 'Here are the relevant log entries I found:\n\n' + results.map(r => `• ${r}`).join('\n\n');
      } else {
        botResponse = 'I could not find any relevant log entries.';
      }

      setMessages(prev => [...prev, { role: 'bot', content: botResponse }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { role: 'bot', content: 'Sorry, I encountered an error querying the logs.' }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>🔍 Log Bot</h1>
        <button className="ingest-btn" onClick={handleIngest} disabled={isLoading}>
          {isLoading ? 'Processing...' : 'Ingest Logs'}
        </button>
      </header>

      <main className="chat-area">
        <div className="messages-list">
          {messages.map((msg, idx) => (
            <div key={idx} className={`message ${msg.role}`}>
              <div className="message-bubble">
                {msg.content.split('\n').map((line, i) => (
                  <div key={i}>{line}</div>
                ))}
              </div>
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>
      </main>

      <footer className="input-area">
        <div className="input-wrapper">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            placeholder="Type your query..."
            disabled={isLoading}
          />
          <button className="send-btn" onClick={handleSend} disabled={isLoading}>➤</button>
        </div>
      </footer>
    </div>
  );
}

export default App;
