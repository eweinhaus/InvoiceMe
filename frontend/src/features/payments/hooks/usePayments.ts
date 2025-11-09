import { useQuery } from '@tanstack/react-query'
import {
  getPayments,
  getPaymentsByInvoice,
  getPaymentById,
} from '@/lib/api/payments'
import type { Payment, Page } from '@/features/payments/types/payment.types'

/**
 * Query hook to fetch all payments
 */
export function usePayments(page: number = 0, size: number = 20) {
  return useQuery<Page<Payment>>({
    queryKey: ['payments', 'all', page, size],
    queryFn: () => getPayments(page, size),
    staleTime: 30000, // 30 seconds
    gcTime: 300000, // 5 minutes (formerly cacheTime)
  })
}

/**
 * Query hook to fetch payments by invoice
 */
export function usePaymentsByInvoice(
  invoiceId: string,
  page: number = 0,
  size: number = 20
) {
  return useQuery<Page<Payment>>({
    queryKey: ['payments', 'invoice', invoiceId, page, size],
    queryFn: () => getPaymentsByInvoice(invoiceId, page, size),
    staleTime: 30000,
    gcTime: 300000,
    enabled: !!invoiceId, // Only fetch if invoiceId exists
  })
}

/**
 * Query hook to fetch a single payment by ID
 */
export function usePayment(id: string) {
  return useQuery<Payment>({
    queryKey: ['payments', id],
    queryFn: () => getPaymentById(id),
    staleTime: 30000,
    gcTime: 300000,
    enabled: !!id, // Only fetch if id exists
  })
}


