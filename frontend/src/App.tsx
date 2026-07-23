import { useEffect, useState } from 'react'

type HealthStatus = 'checking' | 'connected' | 'unreachable'

function App() {
  const [status, setStatus] = useState<HealthStatus>('checking')

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then((response) => {
        if (!response.ok) throw new Error('non-OK response')
        return response.json()
      })
      .then((data) => {
        setStatus(data.status === 'connected' ? 'connected' : 'unreachable')
      })
      .catch(() => {
        setStatus('unreachable')
      })
  }, [])

  const message =
    status === 'checking'
      ? 'Checking backend...'
      : status === 'connected'
        ? 'Backend: connected'
        : 'Backend: unreachable'

  return (
    <main>
      <h1>Habit Tracker</h1>
      <p>{message}</p>
    </main>
  )
}

export default App
