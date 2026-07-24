import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Coach from './Coach'

function mockCoachResponse(reply: string, history: Array<{ role: string; content: string }>) {
  vi.mocked(fetch).mockResolvedValueOnce({
    ok: true,
    json: async () => ({ reply, history }),
  } as Response)
}

describe('Coach', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('shows the assistant reply after sending a message', async () => {
    const user = userEvent.setup()
    mockCoachResponse('Added Read to your habits!', [
      { role: 'user', content: 'add a habit called Read' },
      { role: 'assistant', content: 'Added Read to your habits!' },
    ])

    render(<Coach onAction={() => {}} />)

    await user.type(screen.getByRole('textbox', { name: /message/i }), 'add a habit called Read')
    await user.click(screen.getByRole('button', { name: /send/i }))

    expect(await screen.findByText('Added Read to your habits!')).toBeInTheDocument()
  })

  it('calls onAction after a successful reply, to refresh the habit list', async () => {
    const user = userEvent.setup()
    const onAction = vi.fn()
    mockCoachResponse('Added Read to your habits!', [
      { role: 'user', content: 'add a habit called Read' },
      { role: 'assistant', content: 'Added Read to your habits!' },
    ])

    render(<Coach onAction={onAction} />)

    await user.type(screen.getByRole('textbox', { name: /message/i }), 'add a habit called Read')
    await user.click(screen.getByRole('button', { name: /send/i }))

    await screen.findByText('Added Read to your habits!')
    expect(onAction).toHaveBeenCalledTimes(1)
  })

  it('sends the prior conversation history on a second message', async () => {
    const user = userEvent.setup()
    const firstHistory = [
      { role: 'user', content: 'add a habit called Read' },
      { role: 'assistant', content: 'Added Read to your habits!' },
    ]
    mockCoachResponse('Added Read to your habits!', firstHistory)
    mockCoachResponse('Done, marked Read as complete!', [
      ...firstHistory,
      { role: 'user', content: 'mark Read as done' },
      { role: 'assistant', content: 'Done, marked Read as complete!' },
    ])

    render(<Coach onAction={() => {}} />)

    await user.type(screen.getByRole('textbox', { name: /message/i }), 'add a habit called Read')
    await user.click(screen.getByRole('button', { name: /send/i }))
    await screen.findByText('Added Read to your habits!')

    await user.type(screen.getByRole('textbox', { name: /message/i }), 'mark Read as done')
    await user.click(screen.getByRole('button', { name: /send/i }))
    await screen.findByText('Done, marked Read as complete!')

    expect(fetch).toHaveBeenLastCalledWith(
      'http://localhost:8080/api/coach/messages',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ message: 'mark Read as done', history: firstHistory }),
      }),
    )
  })
})
