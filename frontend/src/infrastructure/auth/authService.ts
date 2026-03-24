import axiosClient from '../api/axiosClient';

const AUTH_TOKEN_KEY = 'auth_token';
const AUTH_USER_KEY = 'auth_user_id';

export interface AuthResponse {
  token: string;
  userId: string;
}

/**
 * Calls POST /auth/login (mock endpoint — no credentials needed).
 * Stores JWT token and userId in localStorage.
 */
export async function login(): Promise<AuthResponse> {
  const response = await axiosClient.post<AuthResponse>('/auth/login');
  const { token, userId } = response.data;

  localStorage.setItem(AUTH_TOKEN_KEY, token);
  localStorage.setItem(AUTH_USER_KEY, userId);

  return response.data;
}

/**
 * Clears stored authentication data.
 */
export function logout(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USER_KEY);
}

/**
 * Returns the stored JWT token, or null if not logged in.
 */
export function getToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

/**
 * Returns the stored userId, or null if not logged in.
 */
export function getUserId(): string | null {
  return localStorage.getItem(AUTH_USER_KEY);
}

/**
 * Returns true if a token exists in localStorage.
 */
export function isAuthenticated(): boolean {
  return getToken() !== null;
}
