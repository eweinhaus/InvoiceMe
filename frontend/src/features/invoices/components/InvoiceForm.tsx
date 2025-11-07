import { useForm, useFieldArray, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { LineItemForm } from './LineItemForm'
import type {
  Invoice,
  CreateInvoiceRequest,
  UpdateInvoiceRequest,
} from '@/features/invoices/types/invoice.types'
import { formatCurrency } from '@/lib/utils/formatters'

// Customer type (simplified, should match Customer from customers feature)
interface Customer {
  id: string
  name: string
  email: string
}

// Zod validation schema
const lineItemSchema = z.object({
  description: z.string().min(1, 'Description is required'),
  quantity: z.number().min(1, 'Quantity must be at least 1'),
  unitPrice: z.number().min(0, 'Unit price must be >= 0'),
})

const invoiceSchema = z.object({
  customerId: z.string().uuid('Invalid customer ID'),
  lineItems: z
    .array(lineItemSchema)
    .min(1, 'At least one line item is required'),
})

type InvoiceFormData = z.infer<typeof invoiceSchema>

interface InvoiceFormProps {
  mode: 'create' | 'edit'
  initialData?: Invoice
  customers: Customer[]
  onSubmit: (
    data: CreateInvoiceRequest | UpdateInvoiceRequest
  ) => void | Promise<void>
  onCancel: () => void
  isLoading?: boolean
}

export function InvoiceForm({
  mode,
  initialData,
  customers,
  onSubmit,
  onCancel,
  isLoading = false,
}: InvoiceFormProps) {
  const form = useForm<InvoiceFormData>({
    resolver: zodResolver(invoiceSchema),
    defaultValues: {
      customerId: initialData?.customerId || '',
      lineItems:
        initialData?.lineItems.map(item => ({
          description: item.description,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
        })) || [{ description: '', quantity: 1, unitPrice: 0 }],
    },
  })

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'lineItems',
  })

  // Watch line items for total calculation
  const watchedLineItems = useWatch({
    control: form.control,
    name: 'lineItems',
  })

  // Calculate total
  const total = watchedLineItems.reduce((sum, item) => {
    const subtotal = (item.quantity || 0) * (item.unitPrice || 0)
    return sum + subtotal
  }, 0)

  const handleSubmit = (data: InvoiceFormData) => {
    if (mode === 'create') {
      onSubmit(data as CreateInvoiceRequest)
    } else {
      onSubmit({ lineItems: data.lineItems } as UpdateInvoiceRequest)
    }
  }

  const addLineItem = () => {
    append({ description: '', quantity: 1, unitPrice: 0 })
  }

  const removeLineItem = (index: number) => {
    if (fields.length > 1) {
      remove(index)
    }
  }

  const lineItemsErrors = form.formState.errors.lineItems

  return (
    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
      {/* Customer Selector */}
      <div>
        <Label htmlFor="customerId">
          Customer <span className="text-destructive">*</span>
        </Label>
        <Select
          value={form.watch('customerId') || ''}
          onValueChange={(value) => {
            form.setValue('customerId', value, { shouldValidate: true })
          }}
          disabled={mode === 'edit'} // Disable in edit mode
        >
          <SelectTrigger id="customerId" aria-label="Select customer">
            <SelectValue placeholder="Select a customer" />
          </SelectTrigger>
          <SelectContent>
            {customers.length === 0 ? (
              <SelectItem value="" disabled>
                No customers available
              </SelectItem>
            ) : (
              customers.map((customer) => (
                <SelectItem key={customer.id} value={customer.id}>
                  {customer.name} ({customer.email})
                </SelectItem>
              ))
            )}
          </SelectContent>
        </Select>
        {form.formState.errors.customerId && (
          <p className="text-sm text-destructive mt-1" role="alert">
            {form.formState.errors.customerId.message}
          </p>
        )}
      </div>

      {/* Line Items Section */}
      <div>
        <div className="flex justify-between items-center mb-4">
          <Label>Line Items</Label>
          <Button
            type="button"
            variant="outline"
            onClick={addLineItem}
            aria-label="Add line item"
          >
            Add Line Item
          </Button>
        </div>

        <div className="space-y-4">
          {fields.map((field, index) => {
            const lineItem = watchedLineItems[index]
            const lineItemError = lineItemsErrors?.[index]
            return (
              <LineItemForm
                key={field.id}
                lineItem={
                  lineItem
                    ? {
                        description: lineItem.description || '',
                        quantity: lineItem.quantity || 1,
                        unitPrice: lineItem.unitPrice || 0,
                      }
                    : { description: '', quantity: 1, unitPrice: 0 }
                }
                index={index}
                onChange={(idx, item) => {
                  form.setValue(`lineItems.${idx}`, {
                    description: item.description || '',
                    quantity: item.quantity || 1,
                    unitPrice: item.unitPrice || 0,
                  })
                }}
                onRemove={removeLineItem}
                errors={
                  lineItemError
                    ? {
                        description: lineItemError.description?.message || '',
                        quantity: lineItemError.quantity?.message || '',
                        unitPrice: lineItemError.unitPrice?.message || '',
                      }
                    : undefined
                }
                canRemove={fields.length > 1}
              />
            )
          })}
        </div>

        {form.formState.errors.lineItems?.root && (
          <p className="text-sm text-destructive mt-2" role="alert">
            {form.formState.errors.lineItems.root.message}
          </p>
        )}
        {form.formState.errors.lineItems && !Array.isArray(lineItemsErrors) && (
          <p className="text-sm text-destructive mt-2" role="alert">
            {form.formState.errors.lineItems.message}
          </p>
        )}
      </div>

      {/* Total */}
      <div className="flex justify-end">
        <div className="text-right">
          <Label className="text-lg font-semibold">Total</Label>
          <div className="text-2xl font-bold">
            {formatCurrency(Number(total.toFixed(2)))}
          </div>
        </div>
      </div>

      {/* Form Actions */}
      <div className="flex justify-end gap-4">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isLoading}
        >
          Cancel
        </Button>
        <Button type="submit" disabled={isLoading}>
          {isLoading ? 'Saving...' : mode === 'create' ? 'Create Invoice' : 'Update Invoice'}
        </Button>
      </div>
    </form>
  )
}

