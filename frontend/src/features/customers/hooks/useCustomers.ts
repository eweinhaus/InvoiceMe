/**
 * React Query hook for fetching customers
 */

import { useQuery } from '@tanstack/react-query'
import { getCustomers } from '../../../lib/api/customers'
import type { PageCustomerResponse } from '../types/customer.types'

export function useCustomers(page: number = 0, size: number = 20) {
  return useQuery<PageCustomerResponse>({
    queryKey: ['customers', page, size],
    queryFn: () => getCustomers(page, size),
    staleTime: 1000 * 30, // 30 seconds
  })
}


