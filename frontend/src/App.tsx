import { useEffect, useState } from 'react'
import Habits from './Habits'
import Coach from './Coach'

type HealthStatus = 'checking' | 'connected' | 'unreachable'

function App() {
  const [status, setStatus] = useState<HealthStatus>('checking')
  const [refreshKey, setRefreshKey] = useState(0)

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
      <Habits key={refreshKey} />
      <Coach onAction={() => setRefreshKey((current) => current + 1)} />
    </main>
  )
}

export default App
