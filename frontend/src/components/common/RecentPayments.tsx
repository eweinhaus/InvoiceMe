import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Link } from 'react-router-dom'
import LoadingSpinner from './LoadingSpinner'
import ErrorMessage from './ErrorMessage'
import { formatCurrency, formatDate } from '@/lib/utils/formatters'
import type { Payment } from '@/features/payments/types/payment.types'

interface RecentPaymentsProps {
  payments: Payment[]
  isLoading: boolean
  error: Error | null
}

export function RecentPayments({
  payments,
  isLoading,
  error,
}: RecentPaymentsProps) {
  const formatPaymentNumber = (id: string) => {
    return `PAY-${id.substring(0, 8).toUpperCase()}`
  }

  const formatInvoiceNumber = (id: string) => {
    return `INV-${id.substring(0, 8).toUpperCase()}`
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Payments</CardTitle>
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
          <CardTitle>Recent Payments</CardTitle>
        </CardHeader>
        <CardContent>
          <ErrorMessage message={`Failed to load payments: ${error.message}`} />
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
        <CardTitle>Recent Payments</CardTitle>
        <Button asChild variant="outline" size="sm">
          <Link to="/payments">View All</Link>
        </Button>
      </CardHeader>
      <CardContent>
        {payments.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <p className="text-sm">No payments yet</p>
            <Button asChild variant="link" size="sm" className="mt-2">
              <Link to="/payments">Record a payment</Link>
            </Button>
          </div>
        ) : (
          <div className="space-y-3">
            {payments.slice(0, 5).map((payment) => (
              <Link
                key={payment.id}
                to={`/payments`}
                className="block p-3 rounded-lg border hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium">
                        {formatPaymentNumber(payment.id)}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        for {formatInvoiceNumber(payment.invoiceId)}
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {formatDate(payment.paymentDate)}
                    </p>
                  </div>
                  <div className="text-right ml-4">
                    <p className="text-sm font-semibold text-green-600">
                      {formatCurrency(payment.amount)}
                    </p>
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

