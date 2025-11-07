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
import type { Payment } from '@/features/payments/types/payment.types'
import { formatCurrency, formatDate } from '@/lib/utils/formatters'

interface PaymentListProps {
  payments: Payment[]
  isLoading: boolean
  error: Error | null
  pagination: {
    page: number
    size: number
    totalElements: number
    totalPages: number
  }
  onViewDetails?: (payment: Payment) => void
  onPageChange: (page: number) => void
  onSizeChange?: (size: number) => void
}

export function PaymentList({
  payments,
  isLoading,
  error,
  pagination,
  onViewDetails,
  onPageChange,
}: PaymentListProps) {
  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (error) {
    return (
      <ErrorMessage message={`Failed to load payments: ${error.message}`} />
    )
  }

  if (payments.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p className="text-lg font-medium">No payments found</p>
        <p className="text-sm">Record your first payment to get started</p>
      </div>
    )
  }

  const formatPaymentNumber = (id: string) => {
    return `PAY-${id.substring(0, 8).toUpperCase()}`
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
              <TableHead>Payment #</TableHead>
              <TableHead>Invoice #</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead>Payment Date</TableHead>
              <TableHead>Created</TableHead>
              {onViewDetails && <TableHead className="text-right">Actions</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {payments.map((payment) => (
              <TableRow key={payment.id}>
                <TableCell className="font-medium">
                  {formatPaymentNumber(payment.id)}
                </TableCell>
                <TableCell>
                  {payment.invoiceNumber || formatInvoiceNumber(payment.invoiceId)}
                </TableCell>
                <TableCell className="text-right">
                  {formatCurrency(payment.amount)}
                </TableCell>
                <TableCell>{formatDate(payment.paymentDate)}</TableCell>
                <TableCell>{formatDate(payment.createdAt)}</TableCell>
                {onViewDetails && (
                  <TableCell className="text-right">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onViewDetails(payment)}
                      aria-label={`View details for payment ${formatPaymentNumber(payment.id)}`}
                    >
                      View
                    </Button>
                  </TableCell>
                )}
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

