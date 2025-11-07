/**
 * Customer types - using auto-generated types from OpenAPI spec
 */

import type { components } from '../../../types/api'

// Type aliases for easier use
export type Customer = components['schemas']['CustomerResponse']
export type CustomerRequest = components['schemas']['CustomerRequest']
export type PageCustomerResponse = components['schemas']['PageCustomerResponse']

// Helper type for pagination
export interface Pagination {
  page: number
  size: number
  totalElements: number
  totalPages: number
}

