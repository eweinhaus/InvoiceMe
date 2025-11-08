import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import apiClient from '../api/client'
import { queryClient } from '../react-query/queryClient'
import type { components } from '../../types/api'

type UserResponse = components['schemas']['UserResponse']

export function useAuth() {
  const navigate = useNavigate()

  const { data: user, isLoading, error, refetch } = useQuery({
    queryKey: ['auth', 'user'],
    queryFn: async (): Promise<UserResponse | null> => {
      try {
        const response = await apiClient.get<UserResponse>('/auth/user')
        return response.data
      } catch (error: any) {
        // If 401, return null (user not authenticated)
        if (error.response?.status === 401) {
          return null
        }
        throw error
      }
    },
    retry: false,
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchOnWindowFocus: false,
    enabled: window.location.pathname !== '/login', // Don't fetch on login page
  })

  // Handle 401 errors - redirect to login
  useEffect(() => {
    if (error && (error as any).response?.status === 401) {
      // Clear React Query cache
      queryClient.clear()
      navigate('/login', { replace: true })
    }
  }, [error, navigate])

  return {
    user: user || null,
    isAuthenticated: !!user,
    isLoading,
    refetch: async () => {
      const result = await refetch()
      return result.data
    },
  }
}

