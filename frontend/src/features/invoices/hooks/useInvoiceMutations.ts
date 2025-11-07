import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  createInvoice,
  updateInvoice,
  markInvoiceAsSent,
} from '@/lib/api/invoices'
import type {
  CreateInvoiceRequest,
  UpdateInvoiceRequest,
} from '@/features/invoices/types/invoice.types'

/**
 * Mutation hook to create a new invoice
 */
export function useCreateInvoice() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateInvoiceRequest) => createInvoice(data),
    onSuccess: () => {
      // Invalidate all invoice queries
      queryClient.invalidateQueries({ queryKey: ['invoices'] })
      // Show success notification (console for now, can be improved with toast)
      console.log('Invoice created successfully')
    },
    onError: (error) => {
      console.error('Failed to create invoice:', error)
    },
  })
}

/**
 * Mutation hook to update an existing invoice
 */
export function useUpdateInvoice() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string
      data: UpdateInvoiceRequest
    }) => updateInvoice(id, data),
    onSuccess: (_data, variables) => {
      // Invalidate all invoice queries
      queryClient.invalidateQueries({ queryKey: ['invoices'] })
      // Invalidate specific invoice query
      queryClient.invalidateQueries({ queryKey: ['invoices', variables.id] })
      console.log('Invoice updated successfully')
    },
    onError: (error) => {
      console.error('Failed to update invoice:', error)
    },
  })
}

/**
 * Mutation hook to mark invoice as SENT
 */
export function useMarkInvoiceAsSent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => markInvoiceAsSent(id),
    onSuccess: (_data, id) => {
      // Invalidate all invoice queries
      queryClient.invalidateQueries({ queryKey: ['invoices'] })
      // Invalidate specific invoice query
      queryClient.invalidateQueries({ queryKey: ['invoices', id] })
      console.log('Invoice marked as sent successfully')
    },
    onError: (error) => {
      console.error('Failed to mark invoice as sent:', error)
    },
  })
}

