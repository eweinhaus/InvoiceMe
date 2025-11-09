/**
 * Customers page component - main page for customer management
 */

import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '../../../components/ui/dialog'
import { Button } from '../../../components/ui/button'
import { useCustomerViewModel } from '../hooks/useCustomerViewModel'
import CustomerList from '../components/CustomerList'
import CustomerForm from '../components/CustomerForm'
import CustomerDeleteDialog from '../components/CustomerDeleteDialog'
import type { Customer, CustomerRequest } from '../types/customer.types'

export default function CustomersPage() {
  const viewModel = useCustomerViewModel()

  // Form dialog state
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create')
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null)

  // Delete dialog state
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [customerToDelete, setCustomerToDelete] = useState<Customer | null>(null)

  const handleCreateClick = () => {
    setFormMode('create')
    setEditingCustomer(null)
    setIsFormOpen(true)
  }

  const handleEditClick = (customer: Customer) => {
    setFormMode('edit')
    setEditingCustomer(customer)
    setIsFormOpen(true)
  }

  const handleDeleteClick = (customer: Customer) => {
    setCustomerToDelete(customer)
    setIsDeleteDialogOpen(true)
  }

  const handleFormSubmit = async (data: CustomerRequest) => {
    try {
      if (formMode === 'create') {
        await viewModel.createCustomer(data)
      } else if (editingCustomer?.id) {
        await viewModel.updateCustomer(editingCustomer.id, data)
      }
      setIsFormOpen(false)
      setEditingCustomer(null)
    } catch (error) {
      console.error('Failed to save customer:', error)
      // Error is already handled by the ViewModel
    }
  }

  const handleDeleteConfirm = async () => {
    if (customerToDelete?.id) {
      try {
        await viewModel.deleteCustomer(customerToDelete.id)
        setIsDeleteDialogOpen(false)
        setCustomerToDelete(null)
      } catch (error) {
        console.error('Failed to delete customer:', error)
        // Error is already handled by the ViewModel
      }
    }
  }

  return (
    <div className="container mx-auto py-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Customers</h1>
        <Button onClick={handleCreateClick}>Create Customer</Button>
      </div>

      <CustomerList
        customers={viewModel.customers}
        isLoading={viewModel.isLoading}
        error={viewModel.error}
        pagination={viewModel.pagination}
        onEdit={handleEditClick}
        onDelete={handleDeleteClick}
        onPageChange={viewModel.setPage}
      />

      {/* Create/Edit Form Dialog */}
      <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {formMode === 'create' ? 'Create Customer' : 'Edit Customer'}
            </DialogTitle>
          </DialogHeader>
          <CustomerForm
            mode={formMode}
            initialData={editingCustomer || undefined}
            onSubmit={handleFormSubmit}
            onCancel={() => {
              setIsFormOpen(false)
              setEditingCustomer(null)
            }}
            isLoading={viewModel.isLoading}
          />
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <CustomerDeleteDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        customer={customerToDelete}
        onConfirm={handleDeleteConfirm}
        isLoading={viewModel.isLoading}
      />
    </div>
  )
}


