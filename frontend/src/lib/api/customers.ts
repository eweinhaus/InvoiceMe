/**
 * Customer API client methods
 */

import apiClient from './client'
import type { Customer, CustomerRequest, PageCustomerResponse } from '../../features/customers/types/customer.types'

/**
 * Get paginated list of customers
 */
export async function getCustomers(
  page: number = 0,
  size: number = 20
): Promise<PageCustomerResponse> {
  const response = await apiClient.get<PageCustomerResponse>('/customers', {
    params: { page, size },
  })
  return response.data
}

/**
 * Get customer by ID
 */
export async function getCustomerById(id: string): Promise<Customer> {
  const response = await apiClient.get<Customer>(`/customers/${id}`)
  return response.data
}

/**
 * Create a new customer
 */
export async function createCustomer(data: CustomerRequest): Promise<Customer> {
  const response = await apiClient.post<Customer>('/customers', data)
  return response.data
}

/**
 * Update an existing customer
 */
export async function updateCustomer(
  id: string,
  data: CustomerRequest
): Promise<Customer> {
  const response = await apiClient.put<Customer>(`/customers/${id}`, data)
  return response.data
}

/**
 * Delete a customer
 */
export async function deleteCustomer(id: string): Promise<void> {
  await apiClient.delete(`/customers/${id}`)
}

