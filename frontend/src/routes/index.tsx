import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Layout from '../components/layout/Layout'
import CustomersPage from '../features/customers/pages/CustomersPage'
import { InvoicesPage } from '../features/invoices/pages/InvoicesPage'
import { PaymentsPage } from '../features/payments/pages/PaymentsPage'
import LoginPage from '../pages/LoginPage'
import ProtectedRoute from '../components/common/ProtectedRoute'
import { Button } from '../components/ui/button'
// import { useAuth } from '../lib/hooks/useAuth'
// import LoadingSpinner from '../components/common/LoadingSpinner'
import { StatCard } from '../components/common/StatCard'
import { RecentInvoices } from '../components/common/RecentInvoices'
import { RecentPayments } from '../components/common/RecentPayments'
import { useCustomers } from '../features/customers/hooks/useCustomers'
import { useInvoices } from '../features/invoices/hooks/useInvoices'
import { usePayments } from '../features/payments/hooks/usePayments'
import { useMemo } from 'react'

// Simple SVG icons for stat cards
const UsersIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="h-4 w-4"
  >
    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </svg>
)

const FileTextIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="h-4 w-4"
  >
    <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
    <polyline points="14 2 14 8 20 8" />
    <line x1="16" y1="13" x2="8" y2="13" />
    <line x1="16" y1="17" x2="8" y2="17" />
    <polyline points="10 9 9 9 8 9" />
  </svg>
)

const DollarSignIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="h-4 w-4"
  >
    <line x1="12" y1="1" x2="12" y2="23" />
    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
  </svg>
)

const CreditCardIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="h-4 w-4"
  >
    <rect x="1" y="4" width="22" height="16" rx="2" ry="2" />
    <line x1="1" y1="10" x2="23" y2="10" />
  </svg>
)

// Home page component - shows login if not authenticated, welcome page if authenticated
const HomePage = () => {
  // AUTHENTICATION DISABLED - for local development
  // const user = null // Mock user for when auth is disabled

  // Fetch data for dashboard stats
  // Use small page size to get totals efficiently
  const {
    data: customersData,
    // isLoading: customersLoading,
    // error: customersError,
  } = useCustomers(0, 1)
  const {
    data: invoicesData,
    isLoading: invoicesLoading,
    error: invoicesError,
  } = useInvoices(0, 100) // Get more to calculate stats
  const {
    data: paymentsData,
    isLoading: paymentsLoading,
    error: paymentsError,
  } = usePayments(0, 10) // Get enough for recent payments display

  // Calculate statistics
  const stats = useMemo(() => {
    const customerCount = customersData?.totalElements || 0
    const invoiceCount = invoicesData?.totalElements || 0
    const paymentCount = paymentsData?.totalElements || 0

    // Calculate outstanding balance (sum of all unpaid invoice balances)
    const outstandingBalance =
      invoicesData?.content.reduce((sum, invoice) => {
        return sum + (invoice.balance > 0 ? invoice.balance : 0)
      }, 0) || 0

    return {
      customerCount,
      invoiceCount,
      paymentCount,
      outstandingBalance,
    }
  }, [customersData, invoicesData, paymentsData])

  // Get recent invoices and payments
  const recentInvoices = invoicesData?.content.slice(0, 5) || []
  const recentPayments = paymentsData?.content.slice(0, 5) || []

  // const isLoading = customersLoading || invoicesLoading || paymentsLoading

  // Show dashboard (authentication disabled)
  return (
    <div className="space-y-8">
      {/* Welcome Section */}
      <section>
        <h1 className="text-3xl font-bold">
          Welcome back!
        </h1>
        <p className="text-muted-foreground mt-2">
          Here's what's happening with your business
        </p>
      </section>

      {/* Stats Grid */}
      <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Customers"
          value={stats.customerCount}
          icon={<UsersIcon />}
          href="/customers"
          description="Active customers"
        />
        <StatCard
          title="Total Invoices"
          value={stats.invoiceCount}
          icon={<FileTextIcon />}
          href="/invoices"
          description="All invoices"
        />
        <StatCard
          title="Outstanding Balance"
          value={stats.outstandingBalance}
          format="currency"
          icon={<DollarSignIcon />}
          href="/invoices"
          description="Unpaid invoices"
        />
        <StatCard
          title="Total Payments"
          value={stats.paymentCount}
          icon={<CreditCardIcon />}
          href="/payments"
          description="All payments"
        />
      </section>

      {/* Quick Actions */}
      <section>
        <h2 className="text-xl font-semibold mb-4">Quick Actions</h2>
        <div className="flex flex-wrap gap-3">
          <Button asChild variant="outline" size="lg">
            <Link to="/invoices">Create Invoice</Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link to="/customers">Manage Customers</Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link to="/invoices">View All Invoices</Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link to="/payments">Record Payment</Link>
          </Button>
        </div>
      </section>

      {/* Recent Activity */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <RecentInvoices
          invoices={recentInvoices}
          isLoading={invoicesLoading}
          error={invoicesError as Error | null}
        />
        <RecentPayments
          payments={recentPayments}
          isLoading={paymentsLoading}
          error={paymentsError as Error | null}
        />
      </section>
    </div>
  )
}

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route
            path="customers"
            element={
              <ProtectedRoute>
                <CustomersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="invoices"
            element={
              <ProtectedRoute>
                <InvoicesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="payments"
            element={
              <ProtectedRoute>
                <PaymentsPage />
              </ProtectedRoute>
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

