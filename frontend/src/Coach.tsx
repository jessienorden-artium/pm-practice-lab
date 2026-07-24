import { useState } from 'react'

type ChatTurn = {
  role: 'user' | 'assistant'
  content: string
}

type CoachProps = {
  onAction: () => void
}

const COACH_API = 'http://localhost:8080/api/coach/messages'

function Coach({ onAction }: CoachProps) {
  const [history, setHistory] = useState<ChatTurn[]>([])
  const [messageText, setMessageText] = useState('')

  function handleSend() {
    fetch(COACH_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: messageText, history }),
    })
      .then((response) => response.json())
      .then((data: { reply: string; history: ChatTurn[] }) => {
        setHistory(data.history)
        setMessageText('')
        onAction()
      })
  }

  return (
    <div>
      <ul>
        {history.map((turn, index) => (
          <li key={index}>{turn.content}</li>
        ))}
      </ul>
      <label htmlFor="coach-message">Message</label>
      <input
        id="coach-message"
        value={messageText}
        onChange={(event) => setMessageText(event.target.value)}
      />
      <button type="button" onClick={handleSend}>
        Send
      </button>
    </div>
  )
}

export default Coach
