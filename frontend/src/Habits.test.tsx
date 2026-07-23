import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Habits from './Habits'

describe('Habits', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('shows an empty-state message when there are no habits', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    } as Response)

    render(<Habits />)

    expect(await screen.findByText('No habits yet — add one above.')).toBeInTheDocument()
  })

  it('shows previously created habits', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => [{ id: 1, name: 'Drink water' }],
    } as Response)

    render(<Habits />)

    expect(await screen.findByText('Drink water')).toBeInTheDocument()
  })

  it('adds a habit and shows it in the list without a page reload', async () => {
    const user = userEvent.setup()
    vi.mocked(fetch)
      .mockResolvedValueOnce({ ok: true, json: async () => [] } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ id: 2, name: 'Meditate' }),
      } as Response)

    render(<Habits />)
    await screen.findByText('No habits yet — add one above.')

    await user.type(screen.getByRole('textbox', { name: /habit name/i }), 'Meditate')
    await user.click(screen.getByRole('button', { name: /add/i }))

    expect(await screen.findByText('Meditate')).toBeInTheDocument()
    expect(fetch).toHaveBeenLastCalledWith(
      'http://localhost:8080/api/habits',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ name: 'Meditate' }),
      }),
    )
  })

  it('does not create a habit when the name is blank', async () => {
    const user = userEvent.setup()
    vi.mocked(fetch).mockResolvedValueOnce({ ok: true, json: async () => [] } as Response)

    render(<Habits />)
    await screen.findByText('No habits yet — add one above.')

    await user.type(screen.getByRole('textbox', { name: /habit name/i }), '   ')
    await user.click(screen.getByRole('button', { name: /add/i }))

    // Only the initial GET on mount — no POST for a blank name.
    expect(fetch).toHaveBeenCalledTimes(1)
    expect(await screen.findByText('No habits yet — add one above.')).toBeInTheDocument()
  })

  it('removes a habit from the list when Delete is clicked', async () => {
    const user = userEvent.setup()
    vi.mocked(fetch)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ id: 3, name: 'Read' }],
      } as Response)
      .mockResolvedValueOnce({ ok: true, status: 204 } as Response)

    render(<Habits />)
    await screen.findByText('Read')

    await user.click(screen.getByRole('button', { name: /delete read/i }))

    expect(fetch).toHaveBeenLastCalledWith(
      'http://localhost:8080/api/habits/3',
      expect.objectContaining({ method: 'DELETE' }),
    )
    expect(await screen.findByText('No habits yet — add one above.')).toBeInTheDocument()
  })
})
