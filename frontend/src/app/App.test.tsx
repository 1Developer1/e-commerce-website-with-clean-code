/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from './App';

vi.mock('../infrastructure/api/axiosClient', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} })),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
}));

describe('App', () => {
  it('renders the application correctly (redirects to Login when unauthorized)', () => {
    render(<App />);
    // Because the default route is /catalog and it's protected, it redirects to /login
    // We expect the login form or branding to be somewhere in the DOM
    expect(screen.getByText(/Giriş Yap/i)).toBeInTheDocument();
  });
});
