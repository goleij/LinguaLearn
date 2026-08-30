import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, api } from './api';

/**
 * The fetch wrapper is the one place where the session and CSRF rules of the
 * Spring Boot backend are encoded, so the header it sends and the error it
 * raises are worth pinning down.
 */
describe('api', () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    // The mock is shared by the whole file, so its call log has to be cleared
    // between tests or lastCall() would read the previous test's request
    fetchMock.mockReset();
    vi.stubGlobal('fetch', fetchMock);
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  const respondWith = (status: number, body: string | null) =>
    fetchMock.mockResolvedValue({
      ok: status >= 200 && status < 300,
      status,
      text: async () => body ?? '',
    });

  const lastCall = () => {
    const { calls } = fetchMock.mock;
    const [url, init] = calls[calls.length - 1] as [string, RequestInit];
    return { url, init, headers: new Headers(init.headers) };
  };

  it('prefixes the path with /api and sends the session cookie', async () => {
    respondWith(200, JSON.stringify({ username: 'anna' }));

    await api.get('/auth/me');

    expect(lastCall().url).toBe('/api/auth/me');
    expect(lastCall().init.credentials).toBe('same-origin');
  });

  it('echoes the XSRF-TOKEN cookie back in the X-XSRF-TOKEN header', async () => {
    document.cookie = 'XSRF-TOKEN=token-from-cookie; path=/';
    respondWith(200, '{}');

    await api.post('/auth/logout');

    expect(lastCall().headers.get('X-XSRF-TOKEN')).toBe('token-from-cookie');
  });

  it('only sets a JSON content type when there is a body', async () => {
    respondWith(200, '{}');
    await api.post('/auth/logout');
    expect(lastCall().headers.get('Content-Type')).toBeNull();

    fetchMock.mockClear();
    respondWith(200, '{}');
    await api.post('/auth/login', { username: 'anna', password: 'secret123' });
    expect(lastCall().headers.get('Content-Type')).toBe('application/json');
    expect(lastCall().init.body).toBe('{"username":"anna","password":"secret123"}');
  });

  it('turns a failed response into an ApiError carrying the server message', async () => {
    respondWith(401, JSON.stringify({ message: 'Not authenticated' }));

    const failure = await api.get('/auth/me').catch((error: unknown) => error);

    expect(failure).toBeInstanceOf(ApiError);
    expect((failure as ApiError).status).toBe(401);
    expect((failure as ApiError).message).toBe('Not authenticated');
  });

  it('falls back to a generic message when the error body has none', async () => {
    respondWith(500, '');

    const failure = await api.get('/courses').catch((error: unknown) => error);

    expect((failure as ApiError).message).toBe('Request failed (500)');
  });

  it('returns nothing for a 204, which is what logout and startAttempt send', async () => {
    respondWith(204, null);

    await expect(api.post('/lessons/1/attempt')).resolves.toBeUndefined();
  });
});
