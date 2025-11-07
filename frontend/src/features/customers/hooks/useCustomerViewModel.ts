/**
 * Customer ViewModel hook (MVVM pattern)
 * Encapsulates business logic and state management
 */

import { useState } from 'react'
import { useCustomers } from './useCustomers'
import {
  useCreateCustomer,
  useUpdateCustomer,
  useDeleteCustomer,
} from './useCustomerMutations'
import type { Customer, CustomerRequest, Pagination } from '../types/customer.types'

export function useCustomerViewModel(initialPage: number = 0, initialSize: number = 20) {
  const [page, setPage] = useState(initialPage)
  const [size, setSize] = useState(initialSize)

  // Data fetching
  const { data, isLoading, error, refetch } = useCustomers(page, size)

  // Mutations
  const createMutation = useCreateCustomer()
  const updateMutation = useUpdateCustomer()
  const deleteMutation = useDeleteCustomer()

  // Transform data for UI
  const customers: Customer[] = data?.content?.filter((c): c is Customer => c !== undefined && c.id !== undefined) || []
  
  const pagination: Pagination = {
    page: data?.number ?? page, // API returns 0-indexed page
    size: data?.size ?? size,
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
  }

  // Actions
  const createCustomer = async (data: CustomerRequest) => {
    await createMutation.mutateAsync(data)
  }

  const updateCustomer = async (id: string, data: CustomerRequest) => {
    await updateMutation.mutateAsync({ id, data })
  }

  const deleteCustomer = async (id: string) => {
    await deleteMutation.mutateAsync(id)
  }

  return {
    // Data
    customers,
    pagination,
    isLoading: isLoading || createMutation.isPending || updateMutation.isPending || deleteMutation.isPending,
    error: error || createMutation.error || updateMutation.error || deleteMutation.error,
    
    // Actions
    createCustomer,
    updateCustomer,
    deleteCustomer,
    refetch,
    
    // Pagination
    setPage,
    setSize,
  }
}

