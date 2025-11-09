import { useState, useEffect } from 'react'
import { getCurrentUser, isAuthenticated as checkAuth, SimpleUser } from '../auth/simpleAuth'

export function useAuth() {
  const [user, setUser] = useState<SimpleUser | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Check authentication status on mount and when storage changes
    const checkAuthStatus = () => {
      const currentUser = getCurrentUser()
      setUser(currentUser)
      setIsLoading(false)
    }

    checkAuthStatus()

    // Listen for storage changes (e.g., logout from another tab)
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'invoiceme_auth' || e.key === null) {
        checkAuthStatus()
      }
    }

    window.addEventListener('storage', handleStorageChange)
    return () => window.removeEventListener('storage', handleStorageChange)
  }, [])

  return {
    user,
    isAuthenticated: checkAuth(),
    isLoading,
    refetch: async () => {
      const currentUser = getCurrentUser()
      setUser(currentUser)
      return currentUser
    },
  }
}

