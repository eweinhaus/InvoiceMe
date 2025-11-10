import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { SearchableSelect } from '@/components/ui/searchable-select'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { PaymentList } from '../components/PaymentList'
import { PaymentForm } from '../components/PaymentForm'
import { usePaymentViewModel } from '../hooks/usePaymentViewModel'
import type { PaymentRequest } from '../types/payment.types'
import apiClient from '@/lib/api/client'
import type { components } from '@/types/api'

// Invoice type for filter dropdown
interface Invoice {
  id: string
  customerName?: string
}

// Fetch invoices for filter dropdown
async function fetchInvoices(): Promise<Invoice[]> {
  try {
    const response = await apiClient.get<components['schemas']['PageInvoiceResponse']>(
      '/invoices',
      { params: { page: 0, size: 100 } } // Fetch first 100 invoices
    )
    return (
      response.data.content?.map((inv) => {
        // Map InvoiceResponse to our Invoice interface
        const invoice = {
          id: inv.id || '',
          customerName: (inv as any).customerName, // customerName may not be in OpenAPI type but exists in our mapped type
        }
        return invoice
      }) || []
    )
  } catch (error) {
    console.error('Failed to fetch invoices:', error)
    return []
  }
}

export function PaymentsPage() {
  const viewModel = usePaymentViewModel()

  // Dialog state
  const [isFormOpen, setIsFormOpen] = useState(false)

  // Invoices state for filter dropdown
  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [isLoadingInvoices, setIsLoadingInvoices] = useState(true)

  // Fetch invoices on mount
  useEffect(() => {
    fetchInvoices().then((data) => {
      setInvoices(data)
      setIsLoadingInvoices(false)
    })
  }, [])

  const handleRecordPayment = () => {
    setIsFormOpen(true)
  }

  const handleFormSubmit = async (data: PaymentRequest) => {
    try {
      viewModel.recordPayment(data)
      setIsFormOpen(false)
      // Show success message (can be enhanced with toast)
      console.log('Payment recorded successfully')
    } catch (error) {
      console.error('Failed to record payment:', error)
    }
  }

  const handleInvoiceFilterChange = (value: string) => {
    if (value === 'ALL') {
      viewModel.setInvoiceFilter(null)
    } else {
      viewModel.setInvoiceFilter(value)
    }
  }

  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  return (
    <div className="container mx-auto py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h1 className="text-3xl font-bold">Payments</h1>
        <Button onClick={handleRecordPayment} aria-label="Record new payment" variant="outline">
          Record Payment
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <label htmlFor="invoice-filter" className="text-sm font-medium mb-2 block">
            Invoice
          </label>
          <SearchableSelect
            options={[
              { value: 'ALL', label: 'All Invoices' },
              ...invoices.map((invoice) => ({
                value: invoice.id,
                label: `${formatInvoiceNumber(invoice.id)}${invoice.customerName ? ` - ${invoice.customerName}` : ''}`,
              })),
            ]}
            value={viewModel.filters.invoiceId || 'ALL'}
            onValueChange={handleInvoiceFilterChange}
            disabled={isLoadingInvoices}
            placeholder="All Invoices"
            searchPlaceholder="Search invoices by number or customer..."
            emptyMessage="No invoices found"
            getSearchableText={(option) => {
              if (option.value === 'ALL') return option.label
              const invoice = invoices.find((inv) => inv.id === option.value)
              return invoice
                ? `${formatInvoiceNumber(invoice.id)} ${invoice.customerName || ''}`
                : option.label
            }}
          />
        </div>
      </div>

      {/* Payment List */}
      <PaymentList
        payments={viewModel.payments}
        isLoading={viewModel.isLoading}
        error={viewModel.error}
        pagination={viewModel.pagination}
        onPageChange={viewModel.setPage}
      />

      {/* Record Payment Form Dialog */}
      <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Record Payment</DialogTitle>
          </DialogHeader>
          <PaymentForm
            onSubmit={handleFormSubmit}
            onCancel={() => {
              setIsFormOpen(false)
            }}
            isLoading={false} // Can be enhanced with mutation loading state
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}

