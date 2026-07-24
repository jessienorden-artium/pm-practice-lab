import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'

function mockFetchResponses() {
  vi.mocked(fetch).mockImplementation((input: RequestInfo | URL) => {
    const url = typeof input === 'string' ? input : input.toString()

    if (url.includes('/api/health')) {
      return Promise.resolve({
        ok: true,
        json: async () => ({ status: 'connected' }),
      } as Response)
    }

    if (url.includes('/api/coach/messages')) {
      return Promise.resolve({
        ok: true,
        json: async () => ({
          reply: 'Added Read to your habits!',
          history: [
            { role: 'user', content: 'add a habit called Read' },
            { role: 'assistant', content: 'Added Read to your habits!' },
          ],
        }),
      } as Response)
    }

    if (url.includes('/api/habits')) {
      return Promise.resolve({
        ok: true,
        json: async () => [],
      } as Response)
    }

    return Promise.reject(new Error(`Unexpected fetch call: ${url}`))
  })
}

describe('App', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
    mockFetchResponses()
  })

  it('refetches habits after the coach takes an action', async () => {
    const user = userEvent.setup()
    render(<App />)

    const habitsFetchesBefore = vi
      .mocked(fetch)
      .mock.calls.filter(([input]) => input.toString().includes('/api/habits')).length

    await user.type(screen.getByRole('textbox', { name: /message/i }), 'add a habit called Read')
    await user.click(screen.getByRole('button', { name: /send/i }))

    await screen.findByText('Added Read to your habits!')

    const habitsFetchesAfter = vi
      .mocked(fetch)
      .mock.calls.filter(([input]) => input.toString().includes('/api/habits')).length

    expect(habitsFetchesAfter).toBeGreaterThan(habitsFetchesBefore)
  })
})
