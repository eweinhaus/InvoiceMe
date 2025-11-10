import { useQuery } from '@tanstack/react-query'
import {
  getInvoices,
  getInvoicesByStatus,
  getInvoicesByCustomer,
  getInvoicesWithFilters,
  getInvoiceById,
} from '@/lib/api/invoices'
import type {
  Invoice,
  InvoiceStatus,
  Page,
} from '@/features/invoices/types/invoice.types'

/**
 * Query hook to fetch all invoices
 */
export function useInvoices(page: number = 0, size: number = 20) {
  return useQuery<Page<Invoice>>({
    queryKey: ['invoices', 'all', page, size],
    queryFn: () => getInvoices(page, size),
    staleTime: 30000, // 30 seconds
    gcTime: 300000, // 5 minutes (formerly cacheTime)
  })
}

/**
 * Query hook to fetch invoices by status
 */
export function useInvoicesByStatus(
  status: InvoiceStatus | 'ALL',
  page: number = 0,
  size: number = 20
) {
  return useQuery<Page<Invoice>>({
    queryKey: ['invoices', 'status', status, page, size],
    queryFn: () => getInvoicesByStatus(status as InvoiceStatus, page, size),
    staleTime: 30000,
    gcTime: 300000,
    enabled: status !== 'ALL', // Disable query when status is 'ALL'
  })
}

/**
 * Query hook to fetch invoices by customer
 */
export function useInvoicesByCustomer(
  customerId: string,
  page: number = 0,
  size: number = 20
) {
  return useQuery<Page<Invoice>>({
    queryKey: ['invoices', 'customer', customerId, page, size],
    queryFn: () => getInvoicesByCustomer(customerId, page, size),
    staleTime: 30000,
    gcTime: 300000,
    enabled: !!customerId, // Only fetch if customerId exists
  })
}

/**
 * Query hook to fetch invoices with combined filters
 */
export function useInvoicesWithFilters(
  filters: {
    status?: InvoiceStatus
    customerId?: string
  },
  page: number = 0,
  size: number = 20
) {
  return useQuery<Page<Invoice>>({
    queryKey: ['invoices', 'filters', filters, page, size],
    queryFn: () => getInvoicesWithFilters(filters, page, size),
    staleTime: 30000,
    gcTime: 300000,
    enabled: !!(filters.status || filters.customerId), // Only fetch if at least one filter is set
  })
}

/**
 * Query hook to fetch a single invoice by ID
 */
export function useInvoice(id: string) {
  return useQuery<Invoice>({
    queryKey: ['invoices', id],
    queryFn: () => getInvoiceById(id),
    staleTime: 30000,
    gcTime: 300000,
    enabled: !!id, // Only fetch if id exists
  })
}


