import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useMemo, useEffect } from 'react'
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

  // Set default payment date to today (in local timezone, not UTC)
  // Use local date to avoid timezone issues that could make it tomorrow
  const today = new Date().toLocaleDateString('en-CA') // Returns YYYY-MM-DD in local timezone

  const form = useForm<PaymentFormData>({
    resolver: zodResolver(enhancedSchema),
    mode: 'onChange', // Validate on change for better UX
    defaultValues: {
      invoiceId: preSelectedInvoiceId || '',
      amount: 0, // Will be validated - user must enter a value
      paymentDate: today,
    },
  })

  // Sync preSelectedInvoiceId to form field when it changes
  useEffect(() => {
    if (preSelectedInvoiceId) {
      form.setValue('invoiceId', preSelectedInvoiceId)
      form.trigger('invoiceId')
      setSelectedInvoiceId(preSelectedInvoiceId)
    }
  }, [preSelectedInvoiceId, form])

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
    console.log('Form submitted with data:', JSON.stringify(data, null, 2))
    // Convert date string (YYYY-MM-DD) to LocalDateTime format (YYYY-MM-DDTHH:mm:ss)
    // Backend expects LocalDateTime format - Spring Boot accepts ISO-8601 without timezone
    // Use local date to ensure it's not in the future due to timezone issues
    const selectedDate = data.paymentDate || today
    // Ensure the date is not in the future by using today if selected date is after today
    const todayDate = new Date().toLocaleDateString('en-CA')
    const finalDate = selectedDate > todayDate ? todayDate : selectedDate
    const paymentDate = `${finalDate}T00:00:00`
    
    const paymentRequest = {
      invoiceId: data.invoiceId,
      amount: data.amount,
      paymentDate: paymentDate,
    }
    
    console.log('Payment request being sent:', JSON.stringify(paymentRequest, null, 2))
    onSubmit(paymentRequest)
  }

  // Handle form submission errors
  const handleFormError = (errors: any) => {
    console.error('Form validation errors:', errors)
    // Scroll to first error
    const firstErrorField = Object.keys(errors)[0]
    if (firstErrorField) {
      const element = document.querySelector(`[name="${firstErrorField}"]`)
      element?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }

  return (
    <form onSubmit={form.handleSubmit(handleSubmit, handleFormError)} className="space-y-6">
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
          placeholder="0.00"
          {...form.register('amount', {
            valueAsNumber: true,
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
          max={today} // Prevent selecting future dates
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
        <Button type="submit" variant="default" disabled={isLoading}>
          {isLoading ? 'Recording...' : 'Record Payment'}
        </Button>
      </div>
    </form>
  )
}

