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
import type { Invoice } from '@/features/invoices/types/invoice.types'
import { InvoiceStatus } from '@/features/invoices/types/invoice.types'
import { formatCurrency, formatDate } from '@/lib/utils/formatters'

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
  if (!invoice) return null

  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  const paidAmount = invoice.totalAmount - invoice.balance

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
        </div>

        <DialogFooter>
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
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

