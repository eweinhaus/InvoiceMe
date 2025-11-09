import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recordPayment } from '@/lib/api/payments'
import type { PaymentRequest } from '@/features/payments/types/payment.types'

/**
 * Mutation hook to record a payment
 */
export function useRecordPayment() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: PaymentRequest) => recordPayment(data),
    onSuccess: (data) => {
      // Invalidate all payment queries
      queryClient.invalidateQueries({ queryKey: ['payments'] })
      // Invalidate all invoice queries (to update invoice balance)
      queryClient.invalidateQueries({ queryKey: ['invoices'] })
      // Invalidate specific invoice query
      queryClient.invalidateQueries({ queryKey: ['invoices', data.invoiceId] })
      // Show success notification (console for now, can be improved with toast)
      console.log('Payment recorded successfully')
    },
    onError: (error) => {
      console.error('Failed to record payment:', error)
    },
  })
}


