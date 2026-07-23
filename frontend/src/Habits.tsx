import { useEffect, useState } from 'react'

type Habit = {
  id: number
  name: string
  doneToday: boolean
}

const API_BASE = 'http://localhost:8080/api/habits'

function Habits() {
  const [habits, setHabits] = useState<Habit[]>([])
  const [newHabitName, setNewHabitName] = useState('')

  useEffect(() => {
    fetch(API_BASE)
      .then((response) => response.json())
      .then((data: Array<{ id: number; name: string }>) =>
        Promise.all(
          data.map((habit) =>
            fetch(`${API_BASE}/${habit.id}/checkins/today`)
              .then((response) => response.json())
              .then((checkin: { done: boolean }) => ({ ...habit, doneToday: checkin.done })),
          ),
        ),
      )
      .then((habitsWithCheckins: Habit[]) => setHabits(habitsWithCheckins))
  }, [])

  function handleAdd() {
    if (newHabitName.trim() === '') return

    fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: newHabitName }),
    })
      .then((response) => response.json())
      .then((created: { id: number; name: string }) => {
        setHabits((current) => [...current, { ...created, doneToday: false }])
        setNewHabitName('')
      })
  }

  function handleToggle(id: number, done: boolean) {
    fetch(`${API_BASE}/${id}/checkins/today`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ done }),
    })
      .then((response) => response.json())
      .then((checkin: { done: boolean }) => {
        setHabits((current) =>
          current.map((habit) =>
            habit.id === id ? { ...habit, doneToday: checkin.done } : habit,
          ),
        )
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
              <label>
                <input
                  type="checkbox"
                  checked={habit.doneToday}
                  onChange={(event) => handleToggle(habit.id, event.target.checked)}
                />
                {habit.name}
              </label>
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
