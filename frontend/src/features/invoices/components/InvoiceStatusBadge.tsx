import { Badge } from '@/components/ui/badge'
import { InvoiceStatus } from '@/features/invoices/types/invoice.types'
import { cn } from '@/lib/utils'

interface InvoiceStatusBadgeProps {
  status: InvoiceStatus
  className?: string
}

export function InvoiceStatusBadge({
  status,
  className,
}: InvoiceStatusBadgeProps) {
  const getVariant = () => {
    switch (status) {
      case InvoiceStatus.DRAFT:
        return 'secondary' // Gray
      case InvoiceStatus.SENT:
        return 'default' // Blue
      case InvoiceStatus.PAID:
        return 'outline' // Will add green styling
      default:
        return 'secondary'
    }
  }

  const getStatusColor = () => {
    if (status === InvoiceStatus.PAID) {
      return 'border-green-500 text-green-700 bg-green-50'
    }
    return ''
  }

  return (
    <Badge
      variant={getVariant()}
      className={cn(getStatusColor(), className)}
      aria-label={`Invoice status: ${status}`}
    >
      {status}
    </Badge>
  )
}

