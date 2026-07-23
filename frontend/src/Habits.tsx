import { useEffect, useState } from 'react'

type Habit = {
  id: number
  name: string
}

const API_BASE = 'http://localhost:8080/api/habits'

function Habits() {
  const [habits, setHabits] = useState<Habit[]>([])
  const [newHabitName, setNewHabitName] = useState('')

  useEffect(() => {
    fetch(API_BASE)
      .then((response) => response.json())
      .then((data: Habit[]) => setHabits(data))
  }, [])

  function handleAdd() {
    if (newHabitName.trim() === '') return

    fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: newHabitName }),
    })
      .then((response) => response.json())
      .then((created: Habit) => {
        setHabits((current) => [...current, created])
        setNewHabitName('')
      })
  }

  function handleDelete(id: number) {
    fetch(`${API_BASE}/${id}`, { method: 'DELETE' }).then(() => {
      setHabits((current) => current.filter((habit) => habit.id !== id))
    })
  }

  return (
    <>
      <div>
        <label htmlFor="habit-name">Habit name</label>
        <input
          id="habit-name"
          value={newHabitName}
          onChange={(event) => setNewHabitName(event.target.value)}
        />
        <button type="button" onClick={handleAdd}>
          Add
        </button>
      </div>

      {habits.length === 0 ? (
        <p>No habits yet — add one above.</p>
      ) : (
        <ul>
          {habits.map((habit) => (
            <li key={habit.id}>
              {habit.name}
              <button
                type="button"
                aria-label={`Delete ${habit.name}`}
                onClick={() => handleDelete(habit.id)}
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

export default Habits
