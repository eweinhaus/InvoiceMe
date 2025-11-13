/**
 * React Query mutation hooks for customer operations
 */

import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  createCustomer,
  updateCustomer,
  deleteCustomer,
} from '../../../lib/api/customers'
import type { Customer, CustomerRequest } from '../types/customer.types'

/**
 * Hook for creating a customer
 */
export function useCreateCustomer() {
  const queryClient = useQueryClient()

  return useMutation<Customer, Error, CustomerRequest>({
    mutationFn: createCustomer,
    onSuccess: () => {
      // Invalidate all customer queries to refetch
      queryClient.invalidateQueries({ queryKey: ['customers'] })
      console.log('Customer created successfully')
    },
    onError: (error) => {
      console.error('Failed to create customer:', error)
    },
  })
}

/**
 * Hook for updating a customer
 */
export function useUpdateCustomer() {
  const queryClient = useQueryClient()

  return useMutation<
    Customer,
    Error,
    { id: string; data: CustomerRequest }
  >({
    mutationFn: ({ id, data }) => updateCustomer(id, data),
    onSuccess: () => {
      // Invalidate all customer queries to refetch
      queryClient.invalidateQueries({ queryKey: ['customers'] })
      console.log('Customer updated successfully')
    },
    onError: (error) => {
      console.error('Failed to update customer:', error)
    },
  })
}

/**
 * Hook for deleting a customer
 */
export function useDeleteCustomer() {
  const queryClient = useQueryClient()

  return useMutation<void, Error, string>({
    mutationFn: deleteCustomer,
    onSuccess: () => {
      // Invalidate all customer queries to refetch
      queryClient.invalidateQueries({ queryKey: ['customers'] })
      console.log('Customer deleted successfully')
    },
    onError: (error) => {
      console.error('Failed to delete customer:', error)
    },
  })
}



