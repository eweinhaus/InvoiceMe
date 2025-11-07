import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useMemo } from 'react'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { InvoiceSelector } from './InvoiceSelector'
import { useInvoice } from '@/features/invoices/hooks/useInvoices'
import type { PaymentRequest } from '@/features/payments/types/payment.types'
import { formatCurrency } from '@/lib/utils/formatters'

// Base Zod validation schema
const paymentSchema = z.object({
  invoiceId: z.string().uuid('Invalid invoice ID').min(1, 'Invoice is required'),
  amount: z
    .number({ message: 'Amount must be a number' })
    .min(0.01, 'Amount must be greater than 0'),
  paymentDate: z.string().min(1, 'Payment date is required'),
})

type PaymentFormData = z.infer<typeof paymentSchema>

interface PaymentFormProps {
  onSubmit: (data: PaymentRequest) => void | Promise<void>
  onCancel: () => void
  isLoading?: boolean
  preSelectedInvoiceId?: string | null // Pre-select invoice if provided
}

export function PaymentForm({
  onSubmit,
  onCancel,
  isLoading = false,
  preSelectedInvoiceId = null,
}: PaymentFormProps) {
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<string | null>(
    preSelectedInvoiceId
  )

  // Fetch invoice details when selected
  const { data: invoice } = useInvoice(selectedInvoiceId || '')

  // Create enhanced schema with balance validation
  const enhancedSchema = useMemo(() => {
    return paymentSchema.refine(
      (data) => {
        if (!invoice) return true // Skip validation if no invoice selected
        return data.amount <= invoice.balance
      },
      {
        message: `Amount cannot exceed invoice balance (${formatCurrency(invoice?.balance || 0)})`,
        path: ['amount'],
      }
    )
  }, [invoice])

  // Set default payment date to today
  const today = new Date().toISOString().split('T')[0]

  const form = useForm<PaymentFormData>({
    resolver: zodResolver(enhancedSchema),
    defaultValues: {
      invoiceId: '',
      amount: 0,
      paymentDate: today,
    },
  })

  // Watch amount field for real-time balance calculation
  const watchedAmount = useWatch({
    control: form.control,
    name: 'amount',
    defaultValue: 0,
  })

  // Calculate remaining balance
  const remainingBalance =
    invoice && watchedAmount
      ? invoice.balance - (watchedAmount || 0)
      : invoice?.balance || 0

  const handleInvoiceChange = (invoiceId: string | null) => {
    setSelectedInvoiceId(invoiceId)
    form.setValue('invoiceId', invoiceId || '')
    form.trigger('invoiceId')
  }

  const handleSubmit = (data: PaymentFormData) => {
    onSubmit({
      invoiceId: data.invoiceId,
      amount: data.amount,
      paymentDate: data.paymentDate,
    })
  }

  return (
    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
      {/* Invoice Selector */}
      <InvoiceSelector
        value={selectedInvoiceId}
        onChange={handleInvoiceChange}
        error={form.formState.errors.invoiceId?.message}
        disabled={isLoading}
      />

      {/* Selected Invoice Details */}
      {invoice && (
        <div className="p-4 bg-muted rounded-md space-y-2">
          <h4 className="font-medium">Invoice Details</h4>
          <div className="text-sm space-y-1">
            <p>
              <span className="font-medium">Invoice #:</span>{' '}
              {`INV-${invoice.id.substring(0, 8).toUpperCase()}`}
            </p>
            <p>
              <span className="font-medium">Customer:</span>{' '}
              {invoice.customerName || 'Unknown'}
            </p>
            <p>
              <span className="font-medium">Current Balance:</span>{' '}
              {formatCurrency(invoice.balance)}
            </p>
            {watchedAmount > 0 && (
              <p>
                <span className="font-medium">Remaining Balance:</span>{' '}
                <span
                  className={
                    remainingBalance < 0
                      ? 'text-red-500 font-semibold'
                      : remainingBalance === 0
                        ? 'text-green-500 font-semibold'
                        : ''
                  }
                >
                  {formatCurrency(remainingBalance)}
                </span>
              </p>
            )}
          </div>
        </div>
      )}

      {/* Amount Field */}
      <div className="space-y-2">
        <Label htmlFor="amount">
          Amount <span className="text-red-500">*</span>
        </Label>
        <Input
          id="amount"
          type="number"
          step="0.01"
          min="0.01"
          max={invoice?.balance || undefined}
          {...form.register('amount', {
            valueAsNumber: true,
            onChange: () => {
              form.trigger('amount')
            },
          })}
          className={form.formState.errors.amount ? 'border-red-500' : ''}
          aria-label="Payment amount"
          aria-invalid={!!form.formState.errors.amount}
          aria-describedby={
            form.formState.errors.amount ? 'amount-error' : undefined
          }
        />
        {form.formState.errors.amount && (
          <p
            id="amount-error"
            className="text-sm text-red-500"
            role="alert"
          >
            {form.formState.errors.amount.message}
          </p>
        )}
      </div>

      {/* Payment Date Field */}
      <div className="space-y-2">
        <Label htmlFor="paymentDate">
          Payment Date <span className="text-red-500">*</span>
        </Label>
        <Input
          id="paymentDate"
          type="date"
          {...form.register('paymentDate')}
          className={form.formState.errors.paymentDate ? 'border-red-500' : ''}
          aria-label="Payment date"
          aria-invalid={!!form.formState.errors.paymentDate}
          aria-describedby={
            form.formState.errors.paymentDate ? 'paymentDate-error' : undefined
          }
        />
        {form.formState.errors.paymentDate && (
          <p
            id="paymentDate-error"
            className="text-sm text-red-500"
            role="alert"
          >
            {form.formState.errors.paymentDate.message}
          </p>
        )}
      </div>

      {/* Form Actions */}
      <div className="flex justify-end gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isLoading}
        >
          Cancel
        </Button>
        <Button type="submit" disabled={isLoading}>
          {isLoading ? 'Recording...' : 'Record Payment'}
        </Button>
      </div>
    </form>
  )
}

