import apiClient from './client'
import type {
  Invoice,
  CreateInvoiceRequest,
  UpdateInvoiceRequest,
  InvoiceStatus,
  Page,
} from '@/features/invoices/types/invoice.types'
import type { components } from '@/types/api'
import { mapInvoiceResponse } from '@/features/invoices/types/invoice.types'

type PageInvoiceResponse = components['schemas']['PageInvoiceResponse']
type InvoiceResponse = components['schemas']['InvoiceResponse']

// Helper to map paginated response
function mapPageResponse(response: PageInvoiceResponse): Page<Invoice> {
  return {
    content: (response.content || []).map(mapInvoiceResponse),
    totalElements: response.totalElements || 0,
    totalPages: response.totalPages || 0,
    size: response.size || 20,
    number: response.number || 0,
    first: response.first,
    last: response.last,
    empty: response.empty,
  }
}

/**
 * Get all invoices with pagination
 */
export async function getInvoices(
  page: number = 0,
  size: number = 20
): Promise<Page<Invoice>> {
  const response = await apiClient.get<PageInvoiceResponse>('/invoices', {
    params: { page, size },
  })
  return mapPageResponse(response.data)
}

/**
 * Get invoices filtered by status
 */
export async function getInvoicesByStatus(
  status: InvoiceStatus,
  page: number = 0,
  size: number = 20
): Promise<Page<Invoice>> {
  const response = await apiClient.get<PageInvoiceResponse>('/invoices', {
    params: { status, page, size },
  })
  return mapPageResponse(response.data)
}

/**
 * Get invoices filtered by customer
 */
export async function getInvoicesByCustomer(
  customerId: string,
  page: number = 0,
  size: number = 20
): Promise<Page<Invoice>> {
  const response = await apiClient.get<PageInvoiceResponse>('/invoices', {
    params: { customerId, page, size },
  })
  return mapPageResponse(response.data)
}

/**
 * Get invoices with combined filters (status + customer)
 */
export async function getInvoicesWithFilters(
  filters: {
    status?: InvoiceStatus
    customerId?: string
  },
  page: number = 0,
  size: number = 20
): Promise<Page<Invoice>> {
  const params: Record<string, unknown> = { page, size }
  if (filters.status) params.status = filters.status
  if (filters.customerId) params.customerId = filters.customerId

  const response = await apiClient.get<PageInvoiceResponse>('/invoices', {
    params,
  })
  return mapPageResponse(response.data)
}

/**
 * Get invoice by ID
 */
export async function getInvoiceById(id: string): Promise<Invoice> {
  const response = await apiClient.get<InvoiceResponse>(`/invoices/${id}`)
  return mapInvoiceResponse(response.data)
}

/**
 * Create a new invoice
 */
export async function createInvoice(
  data: CreateInvoiceRequest
): Promise<Invoice> {
  const response = await apiClient.post<InvoiceResponse>('/invoices', data)
  return mapInvoiceResponse(response.data)
}

/**
 * Update an existing invoice (only if DRAFT)
 */
export async function updateInvoice(
  id: string,
  data: UpdateInvoiceRequest
): Promise<Invoice> {
  const response = await apiClient.put<InvoiceResponse>(
    `/invoices/${id}`,
    data
  )
  return mapInvoiceResponse(response.data)
}

/**
 * Mark invoice as SENT
 */
export async function markInvoiceAsSent(id: string): Promise<Invoice> {
  const response = await apiClient.post<InvoiceResponse>(
    `/invoices/${id}/send`
  )
  return mapInvoiceResponse(response.data)
}


