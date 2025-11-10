// InvoiceSelector component - no local state needed
import { SearchableSelect } from '@/components/ui/searchable-select'
import { Label } from '@/components/ui/label'
import { useInvoices } from '@/features/invoices/hooks/useInvoices'
import { InvoiceStatus } from '@/features/invoices/types/invoice.types'
import { formatCurrency } from '@/lib/utils/formatters'
import LoadingSpinner from '@/components/common/LoadingSpinner'

interface InvoiceSelectorProps {
  value: string | null
  onChange: (invoiceId: string | null) => void
  error?: string
  disabled?: boolean
}

export function InvoiceSelector({
  value,
  onChange,
  error,
  disabled = false,
}: InvoiceSelectorProps) {
  // Fetch all invoices (we'll filter client-side)
  const { data, isLoading } = useInvoices(0, 100) // Fetch first 100 invoices

  // Filter invoices: Only show SENT or PAID invoices with balance > 0
  const eligibleInvoices = (data?.content || []).filter(
    (invoice) =>
      (invoice.status === InvoiceStatus.SENT ||
        invoice.status === InvoiceStatus.PAID) &&
      invoice.balance > 0
  )

  // Find selected invoice
  const selectedInvoice = eligibleInvoices.find(
    (inv) => inv.id === value
  ) || null

  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  if (isLoading) {
    return (
      <div className="space-y-2">
        <Label htmlFor="invoice-selector">Invoice</Label>
        <div className="flex justify-center items-center py-4 border rounded-md">
          <LoadingSpinner size="sm" />
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <Label htmlFor="invoice-selector">Invoice</Label>
      <SearchableSelect
        options={
          eligibleInvoices.length === 0
            ? [{ value: 'no-invoices', label: 'No eligible invoices found', disabled: true }]
            : eligibleInvoices.map((invoice) => ({
                value: invoice.id,
                label: `${formatInvoiceNumber(invoice.id)} - ${invoice.customerName || 'Unknown'} (${formatCurrency(invoice.balance)} balance)`,
              }))
        }
        value={value || ''}
        onValueChange={(val) => onChange(val || null)}
        disabled={disabled}
        placeholder="Select Invoice"
        searchPlaceholder="Search invoices by number or customer..."
        emptyMessage="No invoices found"
        triggerClassName={error ? 'border-red-500' : ''}
        getSearchableText={(option) => {
          const invoice = eligibleInvoices.find((inv) => inv.id === option.value)
          if (!invoice) return option.label
          return `${formatInvoiceNumber(invoice.id)} ${invoice.customerName || 'Unknown'} ${formatCurrency(invoice.balance)}`
        }}
      />
      {error && (
        <p className="text-sm text-red-500" role="alert">
          {error}
        </p>
      )}
      {selectedInvoice && (
        <div className="mt-2 p-3 bg-muted rounded-md">
          <p className="text-sm font-medium">
            Invoice: {formatInvoiceNumber(selectedInvoice.id)}
          </p>
          <p className="text-sm text-muted-foreground">
            Customer: {selectedInvoice.customerName || 'Unknown'}
          </p>
          <p className="text-sm text-muted-foreground">
            Current Balance: {formatCurrency(selectedInvoice.balance)}
          </p>
        </div>
      )}
    </div>
  )
}

