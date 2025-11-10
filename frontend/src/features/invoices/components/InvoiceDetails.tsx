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
import { Download, CreditCard, Mail, X } from 'lucide-react'
import apiClient from '@/lib/api/client'

interface InvoiceDetailsProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  invoice: Invoice | null
  onSendViaEmail?: (invoice: Invoice) => void
}

export function InvoiceDetails({
  open,
  onOpenChange,
  invoice,
  onSendViaEmail,
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

  const handleDownloadPdf = async () => {
    try {
      // Use apiClient with blob response type for PDF download
      const response = await apiClient.get(`/invoices/${invoice.id}/pdf`, {
        responseType: 'blob', // Important: tells axios to handle binary data
      })
      
      // Create blob from response data
      const blob = new Blob([response.data], { type: 'application/pdf' })
      
      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `invoice-${invoice.id.substring(0, 8)}.pdf`
      document.body.appendChild(link)
      link.click()
      
      // Cleanup
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (error: any) {
      console.error('Failed to download PDF:', error)
      console.error('Error details:', {
        message: error?.message,
        status: error?.response?.status,
        statusText: error?.response?.statusText,
        data: error?.response?.data,
        config: error?.config,
      })
      
      // Extract error message
      let errorMessage = 'Failed to download PDF. Please try again.'
      
      if (error?.response) {
        const status = error.response.status
        if (status === 404) {
          errorMessage = 'Invoice not found. Please check the invoice ID.'
        } else if (status === 401) {
          errorMessage = 'Authentication required. Please log in and try again.'
        } else if (status === 500) {
          errorMessage = 'Server error while generating PDF. Please check the backend logs and try again.'
        } else if (error.response.data?.message) {
          errorMessage = error.response.data.message
        } else {
          errorMessage = `Error ${status}: ${error.response.statusText || 'Unknown error'}. Please check the console for details.`
        }
      } else if (error?.message) {
        errorMessage = `${error.message}. Please check the console for details.`
      }
      
      // Show user-friendly error
      alert(errorMessage)
      
      // Log full error for debugging
      if (error?.response?.status) {
        console.error(`PDF download failed with status ${error.response.status}`)
      }
    }
  }

  const paidAmount = invoice.totalAmount - invoice.balance
  const payments = paymentsData?.content || []
  const hasBalance = invoice.balance > 0

  const handleRecordPayment = async (data: PaymentRequest) => {
    try {
      console.log('Recording payment with data:', data)
      const result = await recordPaymentMutation.mutateAsync(data)
      console.log('Payment recorded successfully:', result)
      // Invalidate invoice query to refresh balance
      queryClient.invalidateQueries({ queryKey: ['invoices', invoice.id] })
      queryClient.invalidateQueries({ queryKey: ['payments'] })
      setIsPaymentFormOpen(false)
    } catch (error: any) {
      console.error('Failed to record payment:', error)
      const errorData = error?.response?.data
      const validationErrors = errorData?.validationErrors
      const errorMessage = validationErrors 
        ? `Validation errors: ${JSON.stringify(validationErrors, null, 2)}`
        : errorData?.message || error?.message || 'Unknown error'
      
      console.error('Error details:', {
        message: error?.message,
        response: errorData,
        status: error?.response?.status,
        validationErrors: validationErrors,
      })
      // Keep dialog open on error so user can see the error
      alert(`Failed to record payment:\n${errorMessage}`)
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
                variant="outline"
                className="flex items-center gap-2"
              >
                <CreditCard className="h-4 w-4" />
                Record Payment
              </Button>
            )}
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={handleDownloadPdf}
              className="flex items-center gap-2"
            >
              <Download className="h-4 w-4" />
              Download PDF
            </Button>
            {invoice.status === InvoiceStatus.DRAFT && onSendViaEmail && (
              <Button
                onClick={() => {
                  onSendViaEmail(invoice)
                  onOpenChange(false)
                }}
                variant="outline"
                className="flex items-center gap-2"
                aria-label="Send invoice via email"
              >
                <Mail className="h-4 w-4" />
                Send via Email
              </Button>
            )}
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="flex items-center gap-2"
            >
              <X className="h-4 w-4" />
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

