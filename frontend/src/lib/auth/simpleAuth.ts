// Simple authentication utility using localStorage
// Hardcoded credentials: username='username', password='password'

const AUTH_STORAGE_KEY = 'invoiceme_auth'
const HARDCODED_USERNAME = 'username'
const HARDCODED_PASSWORD = 'password'

export interface SimpleUser {
  username: string
  name: string
}

export function login(username: string, password: string): boolean {
  if (username === HARDCODED_USERNAME && password === HARDCODED_PASSWORD) {
    const user: SimpleUser = {
      username: HARDCODED_USERNAME,
      name: 'User',
    }
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user))
    return true
  }
  return false
}

export function logout(): void {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function getCurrentUser(): SimpleUser | null {
  const authData = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!authData) {
    return null
  }
  try {
    return JSON.parse(authData) as SimpleUser
  } catch {
    return null
  }
}

export function isAuthenticated(): boolean {
  return getCurrentUser() !== null
}


