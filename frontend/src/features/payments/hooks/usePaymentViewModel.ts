import { useState, useEffect, useMemo } from 'react'
import { usePayments, usePaymentsByInvoice } from './usePayments'
import { useRecordPayment } from './usePaymentMutations'
import type { PaymentRequest } from '@/features/payments/types/payment.types'

export function usePaymentViewModel() {
  // Filter state
  const [invoiceFilter, setInvoiceFilter] = useState<string | null>(null)

  // Pagination state
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)

  // Reset page to 0 when filter changes
  useEffect(() => {
    setPage(0)
  }, [invoiceFilter])

  // Conditionally call appropriate query hook based on filters
  const allPaymentsQuery = usePayments(page, size)
  const invoicePaymentsQuery = usePaymentsByInvoice(invoiceFilter!, page, size)

  // Select the appropriate query result
  const selectedQuery = invoiceFilter ? invoicePaymentsQuery : allPaymentsQuery

  // Mutation hook
  const recordPaymentMutation = useRecordPayment()

  // Transform data
  const payments = useMemo(
    () => selectedQuery.data?.content || [],
    [selectedQuery.data]
  )

  const pagination = useMemo(
    () => ({
      page: selectedQuery.data?.number || 0,
      size: selectedQuery.data?.size || 20,
      totalElements: selectedQuery.data?.totalElements || 0,
      totalPages: selectedQuery.data?.totalPages || 0,
    }),
    [selectedQuery.data]
  )

  // Actions
  const handleRecordPayment = (data: PaymentRequest) => {
    recordPaymentMutation.mutate(data)
  }

  // Validation helper
  const validatePaymentAmount = (
    amount: number,
    invoiceBalance: number
  ): boolean => {
    return amount > 0 && amount <= invoiceBalance
  }

  return {
    // Data
    payments,
    pagination,
    filters: {
      invoiceId: invoiceFilter,
    },
    isLoading: selectedQuery.isLoading,
    error: selectedQuery.error,
    // Actions
    recordPayment: handleRecordPayment,
    // Setters
    setInvoiceFilter,
    setPage,
    setSize,
    // Utilities
    refetch: selectedQuery.refetch,
    validatePaymentAmount,
  }
}



