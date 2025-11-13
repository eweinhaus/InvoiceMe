/**
 * Customer list component with table display
 */

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../../components/ui/table'
import { Button } from '../../../components/ui/button'
import LoadingSpinner from '../../../components/common/LoadingSpinner'
import ErrorMessage from '../../../components/common/ErrorMessage'
import Pagination from '../../../components/common/Pagination'
import type { Customer, Pagination as PaginationType } from '../types/customer.types'

interface CustomerListProps {
  customers: Customer[]
  isLoading: boolean
  error: Error | null
  pagination: PaginationType
  onEdit: (customer: Customer) => void
  onDelete: (customer: Customer) => void
  onPageChange: (page: number) => void
  onSizeChange?: (size: number) => void
}

export default function CustomerList({
  customers,
  isLoading,
  error,
  pagination,
  onEdit,
  onDelete,
  onPageChange,
}: CustomerListProps) {
  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (error) {
    return (
      <ErrorMessage message={error.message || 'Failed to load customers'} />
    )
  }

  if (customers.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <p>No customers found.</p>
        <p className="text-sm mt-2">Create your first customer to get started.</p>
      </div>
    )
  }

  // Convert 0-indexed page to 1-indexed for Pagination component
  const currentPage = pagination.page + 1

  return (
    <div className="space-y-4">
      <div className="rounded-md border overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Phone</TableHead>
              <TableHead>Address</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {customers.map((customer) => (
              <TableRow key={customer.id}>
                <TableCell className="font-medium">{customer.name}</TableCell>
                <TableCell>{customer.email}</TableCell>
                <TableCell>{customer.phone || 'N/A'}</TableCell>
                <TableCell className="max-w-xs truncate">
                  {customer.address || 'N/A'}
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onEdit(customer)}
                      aria-label={`Edit ${customer.name}`}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => onDelete(customer)}
                      aria-label={`Delete ${customer.name}`}
                    >
                      Delete
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {pagination.totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={pagination.totalPages}
          onPageChange={(page) => onPageChange(page - 1)} // Convert back to 0-indexed
        />
      )}
    </div>
  )
}



