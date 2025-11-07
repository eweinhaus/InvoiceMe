import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { InvoiceStatusBadge } from './InvoiceStatusBadge'
import { PaymentForm } from '@/features/payments/components/PaymentForm'
import { usePaymentsByInvoice } from '@/features/payments/hooks/usePayments'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import type { Invoice } from '@/features/invoices/types/invoice.types'
import { InvoiceStatus } from '@/features/invoices/types/invoice.types'
import type { PaymentRequest } from '@/features/payments/types/payment.types'
import { formatCurrency, formatDate } from '@/lib/utils/formatters'
import { useRecordPayment } from '@/features/payments/hooks/usePaymentMutations'
import { useQueryClient } from '@tanstack/react-query'

interface InvoiceDetailsProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  invoice: Invoice | null
  onMarkAsSent?: (invoice: Invoice) => void
}

export function InvoiceDetails({
  open,
  onOpenChange,
  invoice,
  onMarkAsSent,
}: InvoiceDetailsProps) {
  const [isPaymentFormOpen, setIsPaymentFormOpen] = useState(false)
  const queryClient = useQueryClient()
  const recordPaymentMutation = useRecordPayment()

  // Fetch payments for this invoice
  const {
    data: paymentsData,
    isLoading: isLoadingPayments,
  } = usePaymentsByInvoice(invoice?.id || '', 0, 100)

  if (!invoice) return null

  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  const paidAmount = invoice.totalAmount - invoice.balance
  const payments = paymentsData?.content || []
  const hasBalance = invoice.balance > 0

  const handleRecordPayment = async (data: PaymentRequest) => {
    try {
      await recordPaymentMutation.mutateAsync(data)
      // Invalidate invoice query to refresh balance
      queryClient.invalidateQueries({ queryKey: ['invoices', invoice.id] })
      setIsPaymentFormOpen(false)
      console.log('Payment recorded successfully')
    } catch (error) {
      console.error('Failed to record payment:', error)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Invoice Details</DialogTitle>
        </DialogHeader>

        <div className="space-y-6">
          {/* Invoice Information */}
          <Card>
            <CardHeader>
              <CardTitle>Invoice Information</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Invoice #</p>
                <p className="font-medium">{formatInvoiceNumber(invoice.id)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Status</p>
                <InvoiceStatusBadge status={invoice.status} />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Customer</p>
                <p className="font-medium">
                  {invoice.customerName || invoice.customerId}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Created</p>
                <p className="font-medium">{formatDate(invoice.createdAt)}</p>
              </div>
              {invoice.updatedAt !== invoice.createdAt && (
                <div>
                  <p className="text-sm text-muted-foreground">Updated</p>
                  <p className="font-medium">{formatDate(invoice.updatedAt)}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Line Items */}
          <Card>
            <CardHeader>
              <CardTitle>Line Items</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Description</TableHead>
                    <TableHead className="text-right">Quantity</TableHead>
                    <TableHead className="text-right">Unit Price</TableHead>
                    <TableHead className="text-right">Subtotal</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {invoice.lineItems.map((item, index) => (
                    <TableRow key={index}>
                      <TableCell>{item.description}</TableCell>
                      <TableCell className="text-right">
                        {item.quantity}
                      </TableCell>
                      <TableCell className="text-right">
                        {formatCurrency(item.unitPrice)}
                      </TableCell>
                      <TableCell className="text-right">
                        {formatCurrency(
                          item.subtotal || item.quantity * item.unitPrice
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                  <TableRow className="font-bold">
                    <TableCell colSpan={3} className="text-right">
                      Total
                    </TableCell>
                    <TableCell className="text-right">
                      {formatCurrency(invoice.totalAmount)}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          {/* Balance Information */}
          <Card>
            <CardHeader>
              <CardTitle>Balance Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Total Amount</span>
                <span className="font-medium">
                  {formatCurrency(invoice.totalAmount)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Paid Amount</span>
                <span className="font-medium text-green-600">
                  {formatCurrency(paidAmount)}
                </span>
              </div>
              <div className="flex justify-between border-t pt-2">
                <span className="font-semibold">Balance</span>
                <span className="font-bold text-lg">
                  {formatCurrency(invoice.balance)}
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Payment History */}
          <Card>
            <CardHeader>
              <CardTitle>Payment History</CardTitle>
            </CardHeader>
            <CardContent>
              {isLoadingPayments ? (
                <div className="flex justify-center items-center py-8">
                  <LoadingSpinner size="sm" />
                </div>
              ) : payments.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-4">
                  No payments yet
                </p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Payment Date</TableHead>
                      <TableHead className="text-right">Amount</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {payments.map((payment) => (
                      <TableRow key={payment.id}>
                        <TableCell>{formatDate(payment.paymentDate)}</TableCell>
                        <TableCell className="text-right">
                          {formatCurrency(payment.amount)}
                        </TableCell>
                      </TableRow>
                    ))}
                    <TableRow className="font-bold">
                      <TableCell>Total Payments</TableCell>
                      <TableCell className="text-right">
                        {formatCurrency(
                          payments.reduce((sum, p) => sum + p.amount, 0)
                        )}
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </div>

        <DialogFooter className="flex justify-between">
          <div>
            {hasBalance && (
              <Button
                onClick={() => setIsPaymentFormOpen(true)}
                variant="default"
              >
                Record Payment
              </Button>
            )}
          </div>
          <div className="flex gap-2">
            {invoice.status === InvoiceStatus.DRAFT && onMarkAsSent && (
              <Button
                onClick={() => {
                  onMarkAsSent(invoice)
                  onOpenChange(false)
                }}
              >
                Mark as Sent
              </Button>
            )}
            <Button variant="outline" onClick={() => onOpenChange(false)}>
              Close
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>

      {/* Payment Form Dialog */}
      <Dialog open={isPaymentFormOpen} onOpenChange={setIsPaymentFormOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Record Payment</DialogTitle>
          </DialogHeader>
          <PaymentForm
            onSubmit={handleRecordPayment}
            onCancel={() => setIsPaymentFormOpen(false)}
            isLoading={recordPaymentMutation.isPending}
            preSelectedInvoiceId={invoice.id}
          />
        </DialogContent>
      </Dialog>
    </Dialog>
  )
}

