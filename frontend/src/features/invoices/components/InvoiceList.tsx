import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorMessage from '@/components/common/ErrorMessage'
import Pagination from '@/components/common/Pagination'
import { InvoiceStatusBadge } from './InvoiceStatusBadge'
import type { Invoice } from '@/features/invoices/types/invoice.types'
import { InvoiceStatus } from '@/features/invoices/types/invoice.types'
import { formatCurrency, formatDate } from '@/lib/utils/formatters'

interface InvoiceListProps {
  invoices: Invoice[]
  isLoading: boolean
  error: Error | null
  pagination: {
    page: number
    size: number
    totalElements: number
    totalPages: number
  }
  onViewDetails: (invoice: Invoice) => void
  onEdit: (invoice: Invoice) => void
  onSendViaEmail: (invoice: Invoice) => void
  onPageChange: (page: number) => void
  onSizeChange?: (size: number) => void
}

export function InvoiceList({
  invoices,
  isLoading,
  error,
  pagination,
  onViewDetails,
  onEdit,
  onSendViaEmail,
  onPageChange,
}: InvoiceListProps) {
  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (error) {
    return (
      <ErrorMessage message={`Failed to load invoices: ${error.message}`} />
    )
  }

  if (invoices.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p className="text-lg font-medium">No invoices found</p>
        <p className="text-sm">Create your first invoice to get started</p>
      </div>
    )
  }

  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  return (
    <div className="space-y-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Invoice #</TableHead>
              <TableHead>Customer</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Total</TableHead>
              <TableHead className="text-right">Balance</TableHead>
              <TableHead>Created</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {invoices.map((invoice) => (
              <TableRow key={invoice.id}>
                <TableCell className="font-medium">
                  {formatInvoiceNumber(invoice.id)}
                </TableCell>
                <TableCell>
                  {invoice.customerName || invoice.customerId.substring(0, 8)}
                </TableCell>
                <TableCell>
                  <InvoiceStatusBadge status={invoice.status} />
                </TableCell>
                <TableCell className="text-right">
                  {formatCurrency(invoice.totalAmount)}
                </TableCell>
                <TableCell className="text-right">
                  {formatCurrency(invoice.balance)}
                </TableCell>
                <TableCell>{formatDate(invoice.createdAt)}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onViewDetails(invoice)}
                      aria-label={`View details for invoice ${formatInvoiceNumber(invoice.id)}`}
                    >
                      View
                    </Button>
                    {invoice.status === InvoiceStatus.DRAFT && (
                      <>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => onEdit(invoice)}
                          aria-label={`Edit invoice ${formatInvoiceNumber(invoice.id)}`}
                        >
                          Edit
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => onSendViaEmail(invoice)}
                          aria-label={`Send invoice ${formatInvoiceNumber(invoice.id)} via email`}
                        >
                          Send via Email
                        </Button>
                      </>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {pagination.totalPages > 1 && (
        <Pagination
          currentPage={pagination.page + 1} // Pagination component uses 1-based indexing
          totalPages={pagination.totalPages}
          onPageChange={(page) => onPageChange(page - 1)} // Convert back to 0-based
        />
      )}
    </div>
  )
}

