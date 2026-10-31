let csrf: Promise<{ token: string; headerName: string }> | undefined
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message)
  }
}
export function resetCsrf() {
  csrf = undefined
}
export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && typeof init.body === 'string' && !headers.has('Content-Type'))
    headers.set('Content-Type', 'application/json')
  if (init.method && !['GET', 'HEAD'].includes(init.method)) {
    csrf ??= fetch('/auth/csrf', { credentials: 'same-origin' }).then(async (r) => {
      if (!r.ok) throw new Error('无法连接服务，请稍后重试')
      return r.json()
    })
    try {
      const token = await csrf
      headers.set(token.headerName, token.token)
    } catch (e) {
      resetCsrf()
      throw e
    }
  }
  let response: Response
  try {
    response = await fetch(path, { ...init, headers, credentials: 'same-origin' })
  } catch {
    throw new Error('无法连接服务器，请检查后端是否启动')
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    if (response.status === 401 && path !== '/auth/login' && path !== '/auth/me')
      window.dispatchEvent(new Event('session-expired'))
    if (response.status === 403) resetCsrf()
    throw new ApiError(body.message || body.detail || '操作失败，请重试', response.status)
  }
  return response.status === 204 ? (undefined as T) : response.json()
}
