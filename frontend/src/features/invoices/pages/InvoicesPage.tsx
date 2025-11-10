import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { SearchableSelect } from '@/components/ui/searchable-select'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { InvoiceList } from '../components/InvoiceList'
import { InvoiceForm } from '../components/InvoiceForm'
import { InvoiceDetails } from '../components/InvoiceDetails'
import { useInvoiceViewModel } from '../hooks/useInvoiceViewModel'
import type {
  Invoice,
  CreateInvoiceRequest,
  UpdateInvoiceRequest,
} from '../types/invoice.types'
import { InvoiceStatus } from '../types/invoice.types'
import apiClient from '@/lib/api/client'
import type { components } from '@/types/api'

// Customer type
interface Customer {
  id: string
  name: string
  email: string
}

// Fetch customers (simple implementation, can be improved with proper customer hooks)
async function fetchCustomers(): Promise<Customer[]> {
  try {
    const response = await apiClient.get<components['schemas']['PageCustomerResponse']>(
      '/customers',
      { params: { page: 0, size: 100 } } // Fetch first 100 customers
    )
    return (
      response.data.content?.map((c) => ({
        id: c.id || '',
        name: c.name || '',
        email: c.email || '',
      })) || []
    )
  } catch (error) {
    console.error('Failed to fetch customers:', error)
    // Return empty array or mock data if API fails
    return []
  }
}

export function InvoicesPage() {
  const viewModel = useInvoiceViewModel()

  // Dialog state
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create')
  const [editingInvoice, setEditingInvoice] = useState<Invoice | null>(null)
  const [isDetailsOpen, setIsDetailsOpen] = useState(false)
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null)

  // Customers state
  const [customers, setCustomers] = useState<Customer[]>([])
  const [isLoadingCustomers, setIsLoadingCustomers] = useState(true)

  // Fetch customers on mount
  useEffect(() => {
    fetchCustomers().then((data) => {
      setCustomers(data)
      setIsLoadingCustomers(false)
    })
  }, [])

  const handleCreateInvoice = () => {
    setFormMode('create')
    setEditingInvoice(null)
    setIsFormOpen(true)
  }

  const handleEditInvoice = (invoice: Invoice) => {
    setFormMode('edit')
    setEditingInvoice(invoice)
    setIsFormOpen(true)
  }

  const handleViewDetails = (invoice: Invoice) => {
    setSelectedInvoice(invoice)
    setIsDetailsOpen(true)
  }

  const handleFormSubmit = async (
    data: CreateInvoiceRequest | UpdateInvoiceRequest
  ) => {
    try {
      if (formMode === 'create') {
        viewModel.createInvoice(data as CreateInvoiceRequest)
      } else if (editingInvoice) {
        viewModel.updateInvoice(editingInvoice.id, data as UpdateInvoiceRequest)
      }
      setIsFormOpen(false)
      setEditingInvoice(null)
    } catch (error) {
      console.error('Failed to submit invoice:', error)
    }
  }

  const handleSendViaEmail = (invoice: Invoice) => {
    viewModel.sendViaEmail(invoice.id)
  }

  const handleStatusFilterChange = (value: string) => {
    if (value === 'ALL') {
      viewModel.setStatusFilter('ALL')
    } else {
      viewModel.setStatusFilter(value as InvoiceStatus)
    }
  }

  const handleCustomerFilterChange = (value: string) => {
    if (value === 'ALL') {
      viewModel.setCustomerFilter(null)
    } else {
      viewModel.setCustomerFilter(value)
    }
  }

  return (
    <div className="container mx-auto py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h1 className="text-3xl font-bold">Invoices</h1>
        <Button onClick={handleCreateInvoice} aria-label="Create new invoice" variant="outline">
          Create Invoice
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <label htmlFor="status-filter" className="text-sm font-medium mb-2 block">
            Status
          </label>
          <Select
            value={viewModel.filters.status}
            onValueChange={handleStatusFilterChange}
          >
            <SelectTrigger id="status-filter" aria-label="Filter by status">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All</SelectItem>
              <SelectItem value={InvoiceStatus.DRAFT}>Draft</SelectItem>
              <SelectItem value={InvoiceStatus.SENT}>Sent</SelectItem>
              <SelectItem value={InvoiceStatus.PAID}>Paid</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="flex-1">
          <label htmlFor="customer-filter" className="text-sm font-medium mb-2 block">
            Customer
          </label>
          <SearchableSelect
            options={[
              { value: 'ALL', label: 'All Customers' },
              ...customers.map((customer) => ({
                value: customer.id,
                label: customer.name,
              })),
            ]}
            value={viewModel.filters.customerId || 'ALL'}
            onValueChange={handleCustomerFilterChange}
            disabled={isLoadingCustomers}
            placeholder="All Customers"
            searchPlaceholder="Search customers by name..."
            emptyMessage="No customers found"
            getSearchableText={(option) => {
              if (option.value === 'ALL') return option.label
              const customer = customers.find((c) => c.id === option.value)
              return customer ? `${customer.name} ${customer.email}` : option.label
            }}
          />
        </div>
      </div>

      {/* Invoice List */}
      <InvoiceList
        invoices={viewModel.invoices}
        isLoading={viewModel.isLoading}
        error={viewModel.error}
        pagination={viewModel.pagination}
        onViewDetails={handleViewDetails}
        onEdit={handleEditInvoice}
        onSendViaEmail={handleSendViaEmail}
        onPageChange={viewModel.setPage}
      />

      {/* Create/Edit Form Dialog */}
      <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {formMode === 'create' ? 'Create Invoice' : 'Edit Invoice'}
            </DialogTitle>
          </DialogHeader>
          <InvoiceForm
            mode={formMode}
            initialData={editingInvoice || undefined}
            customers={customers}
            onSubmit={handleFormSubmit}
            onCancel={() => {
              setIsFormOpen(false)
              setEditingInvoice(null)
            }}
            isLoading={false} // Can be enhanced with mutation loading state
          />
        </DialogContent>
      </Dialog>

      {/* Invoice Details Dialog */}
      <InvoiceDetails
        open={isDetailsOpen}
        onOpenChange={setIsDetailsOpen}
        invoice={selectedInvoice}
        onSendViaEmail={handleSendViaEmail}
      />
    </div>
  )
}

