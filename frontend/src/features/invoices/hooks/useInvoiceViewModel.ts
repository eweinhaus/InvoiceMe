import { useState, useEffect, useMemo } from 'react'
import {
  useInvoices,
  useInvoicesByStatus,
  useInvoicesByCustomer,
  useInvoicesWithFilters,
} from './useInvoices'
import {
  useCreateInvoice,
  useUpdateInvoice,
  useMarkInvoiceAsSent,
} from './useInvoiceMutations'
import type {
  InvoiceStatus,
  CreateInvoiceRequest,
  UpdateInvoiceRequest,
} from '@/features/invoices/types/invoice.types'

export function useInvoiceViewModel() {
  // Filter state
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | 'ALL'>('ALL')
  const [customerFilter, setCustomerFilter] = useState<string | null>(null)

  // Pagination state
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)

  // Reset page to 0 when filters change
  useEffect(() => {
    setPage(0)
  }, [statusFilter, customerFilter])

  // Conditionally call appropriate query hook based on filters
  const hasStatusFilter = statusFilter !== 'ALL'
  const hasCustomerFilter = customerFilter !== null && customerFilter !== 'ALL'
  const hasBothFilters = hasStatusFilter && hasCustomerFilter

  const allInvoicesQuery = useInvoices(page, size)
  const statusQuery = useInvoicesByStatus(
    statusFilter, // Now accepts 'ALL' but query is disabled when 'ALL'
    page,
    size
  )
  const customerQuery = useInvoicesByCustomer(
    customerFilter || '', 
    page, 
    size
  )
  const combinedQuery = useInvoicesWithFilters(
    {
      status: statusFilter !== 'ALL' ? statusFilter : undefined,
      customerId: customerFilter && customerFilter !== 'ALL' ? customerFilter : undefined,
    },
    page,
    size
  )

  // Select the appropriate query result
  const selectedQuery = hasBothFilters
    ? combinedQuery
    : hasStatusFilter
      ? statusQuery
      : hasCustomerFilter
        ? customerQuery
        : allInvoicesQuery

  // Mutation hooks
  const createInvoiceMutation = useCreateInvoice()
  const updateInvoiceMutation = useUpdateInvoice()
  const markAsSentMutation = useMarkInvoiceAsSent()

  // Transform data
  const invoices = useMemo(
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
  const handleCreateInvoice = (data: CreateInvoiceRequest) => {
    createInvoiceMutation.mutate(data)
  }

  const handleUpdateInvoice = (id: string, data: UpdateInvoiceRequest) => {
    updateInvoiceMutation.mutate({ id, data })
  }

  const handleMarkAsSent = (id: string) => {
    markAsSentMutation.mutate(id)
  }

  return {
    // Data
    invoices,
    pagination,
    filters: {
      status: statusFilter,
      customerId: customerFilter,
    },
    isLoading: selectedQuery.isLoading,
    error: selectedQuery.error,
    // Actions
    createInvoice: handleCreateInvoice,
    updateInvoice: handleUpdateInvoice,
    markAsSent: handleMarkAsSent,
    // Setters
    setStatusFilter,
    setCustomerFilter,
    setPage,
    setSize,
    // Utilities
    refetch: selectedQuery.refetch,
  }
}

