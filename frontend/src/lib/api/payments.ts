import apiClient from './client'
import type {
  Payment,
  PaymentRequest,
  Page,
} from '@/features/payments/types/payment.types'
import type { components } from '@/types/api'
import { mapPaymentResponse } from '@/features/payments/types/payment.types'

type PagePaymentResponse = components['schemas']['PagePaymentResponse']
type PaymentResponse = components['schemas']['PaymentResponse']

// Helper to map paginated response
function mapPageResponse(response: PagePaymentResponse): Page<Payment> {
  return {
    content: (response.content || []).map(mapPaymentResponse),
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
 * Get all payments with pagination
 */
export async function getPayments(
  page: number = 0,
  size: number = 20
): Promise<Page<Payment>> {
  const response = await apiClient.get<PagePaymentResponse>('/payments', {
    params: { page, size },
  })
  return mapPageResponse(response.data)
}

/**
 * Get payments filtered by invoice
 */
export async function getPaymentsByInvoice(
  invoiceId: string,
  page: number = 0,
  size: number = 20
): Promise<Page<Payment>> {
  const response = await apiClient.get<PagePaymentResponse>('/payments', {
    params: { invoiceId, page, size },
  })
  return mapPageResponse(response.data)
}

/**
 * Get payment by ID
 */
export async function getPaymentById(id: string): Promise<Payment> {
  const response = await apiClient.get<PaymentResponse>(`/payments/${id}`)
  return mapPaymentResponse(response.data)
}

/**
 * Record a payment
 */
export async function recordPayment(
  data: PaymentRequest
): Promise<Payment> {
  console.log('Sending payment request to API:', data)
  try {
    const response = await apiClient.post<PaymentResponse>('/payments', data)
    console.log('Payment API response:', response.data)
    return mapPaymentResponse(response.data)
  } catch (error: any) {
    const errorDetails = {
      message: error?.message,
      response: error?.response?.data,
      status: error?.response?.status,
      requestData: data,
      validationErrors: error?.response?.data?.validationErrors,
    }
    console.error('Payment API error:', JSON.stringify(errorDetails, null, 2))
    throw error
  }
}


