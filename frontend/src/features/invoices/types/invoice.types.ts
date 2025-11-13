import type { components } from '@/types/api'

// Invoice Status Enum
export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  PAID = 'PAID',
}

// Type aliases from OpenAPI
type InvoiceResponse = components['schemas']['InvoiceResponse']
type LineItemRequest = components['schemas']['LineItemRequest']

// Line Item interface
export interface LineItem {
  description: string
  quantity: number
  unitPrice: number
  subtotal?: number // Calculated, optional in request
}

// Invoice interface
export interface Invoice {
  id: string
  customerId: string
  customerName?: string // Optional, for display convenience
  status: InvoiceStatus
  lineItems: LineItem[]
  totalAmount: number
  balance: number
  createdAt: string
  updatedAt: string
}

// Request interfaces
export interface CreateInvoiceRequest {
  customerId: string
  lineItems: LineItemRequest[]
}

export interface UpdateInvoiceRequest {
  lineItems: LineItemRequest[]
}

// Re-export LineItemRequest for convenience
export type { LineItemRequest }

// Pagination interface
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

// Helper function to map OpenAPI InvoiceResponse to Invoice
export function mapInvoiceResponse(response: InvoiceResponse): Invoice {
  return {
    id: response.id || '',
    customerId: response.customerId || '',
    customerName: response.customerName || undefined, // Map customerName from API response
    status: (response.status as InvoiceStatus) || InvoiceStatus.DRAFT,
    lineItems: (response.lineItems || []).map(item => ({
      description: item.description || '',
      quantity: item.quantity || 0,
      unitPrice: item.unitPrice || 0,
      subtotal: item.subtotal,
    })),
    totalAmount: response.totalAmount || 0,
    balance: response.balance || 0,
    createdAt: response.createdAt || '',
    updatedAt: response.updatedAt || '',
  }
}

