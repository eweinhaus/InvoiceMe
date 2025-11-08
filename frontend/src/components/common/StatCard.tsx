import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import { Link } from 'react-router-dom'

interface StatCardProps {
  title: string
  value: string | number
  description?: string
  icon?: React.ReactNode
  href?: string
  className?: string
  format?: 'number' | 'currency'
}

export function StatCard({
  title,
  value,
  description,
  icon,
  href,
  className,
  format = 'number',
}: StatCardProps) {
  const formattedValue =
    format === 'currency' && typeof value === 'number'
      ? new Intl.NumberFormat('en-US', {
          style: 'currency',
          currency: 'USD',
        }).format(value)
      : typeof value === 'number'
        ? value.toLocaleString()
        : value

  const content = (
    <Card className={cn('hover:shadow-md transition-shadow', className)}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        {icon && <div className="h-4 w-4 text-muted-foreground">{icon}</div>}
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{formattedValue}</div>
        {description && (
          <p className="text-xs text-muted-foreground mt-1">{description}</p>
        )}
      </CardContent>
    </Card>
  )

  if (href) {
    return (
      <Link to={href} className="block">
        {content}
      </Link>
    )
  }

  return content
}

