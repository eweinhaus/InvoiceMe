import type { components } from '@/types/api'

// Type aliases from OpenAPI
type PaymentResponse = components['schemas']['PaymentResponse']
type PaymentRequest = components['schemas']['PaymentRequest']

// Payment interface
export interface Payment {
  id: string
  invoiceId: string
  invoiceNumber?: string // Optional, for display convenience
  customerName?: string // Customer name from invoice
  amount: number
  paymentDate: string
  createdAt: string
}

// Request interface
export interface CreatePaymentRequest {
  invoiceId: string
  amount: number
  paymentDate: string
}

// Re-export PaymentRequest for convenience
export type { PaymentRequest }

// Pagination interface (reuse from invoices or define here)
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number // Current page (0-indexed)
  first?: boolean
  last?: boolean
  empty?: boolean
}

// Helper function to map OpenAPI PaymentResponse to Payment
export function mapPaymentResponse(response: PaymentResponse): Payment {
  return {
    id: response.id || '',
    invoiceId: response.invoiceId || '',
    invoiceNumber: response.invoiceNumber || undefined, // Map invoiceNumber from API response
    customerName: response.customerName || undefined, // Map customerName from API response
    amount: response.amount || 0,
    paymentDate: response.paymentDate || '',
    createdAt: response.createdAt || '',
  }
}


