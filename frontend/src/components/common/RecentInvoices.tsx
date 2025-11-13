import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Link } from 'react-router-dom'
import LoadingSpinner from './LoadingSpinner'
import ErrorMessage from './ErrorMessage'
import { formatCurrency, formatDate } from '@/lib/utils/formatters'
import type { Invoice } from '@/features/invoices/types/invoice.types'
import { InvoiceStatusBadge } from '@/features/invoices/components/InvoiceStatusBadge'

interface RecentInvoicesProps {
  invoices: Invoice[]
  isLoading: boolean
  error: Error | null
}

export function RecentInvoices({
  invoices,
  isLoading,
  error,
}: RecentInvoicesProps) {
  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Invoices</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex justify-center items-center py-8">
            <LoadingSpinner size="md" />
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Invoices</CardTitle>
        </CardHeader>
        <CardContent>
          <ErrorMessage message={`Failed to load invoices: ${error.message}`} />
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
        <CardTitle>Recent Invoices</CardTitle>
        <Button asChild variant="outline" size="sm">
          <Link to="/invoices">View All</Link>
        </Button>
      </CardHeader>
      <CardContent>
        {invoices.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <p className="text-sm">No invoices yet</p>
            <Button asChild variant="link" size="sm" className="mt-2">
              <Link to="/invoices">Create your first invoice</Link>
            </Button>
          </div>
        ) : (
          <div className="space-y-3">
            {invoices.slice(0, 5).map((invoice) => (
              <Link
                key={invoice.id}
                to={`/invoices`}
                className="block p-3 rounded-lg border hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium">
                        {formatInvoiceNumber(invoice.id)}
                      </span>
                      <InvoiceStatusBadge status={invoice.status} />
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {formatDate(invoice.createdAt)}
                    </p>
                  </div>
                  <div className="text-right ml-4">
                    <p className="text-sm font-semibold">
                      {formatCurrency(invoice.totalAmount)}
                    </p>
                    {invoice.balance > 0 && (
                      <p className="text-xs text-muted-foreground">
                        {formatCurrency(invoice.balance)} due
                      </p>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}



