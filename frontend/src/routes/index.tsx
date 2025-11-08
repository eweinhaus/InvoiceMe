import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Layout from '../components/layout/Layout'
import CustomersPage from '../features/customers/pages/CustomersPage'
import { InvoicesPage } from '../features/invoices/pages/InvoicesPage'
import { PaymentsPage } from '../features/payments/pages/PaymentsPage'
import LoginPage from '../pages/LoginPage'
import ProtectedRoute from '../components/common/ProtectedRoute'
import { Button } from '../components/ui/button'

// Placeholder components
const HomePage = () => (
  <div className="text-center py-12 space-y-6">
    <h2 className="text-3xl font-bold">Welcome to InvoiceMe</h2>
    <p className="text-muted-foreground">
      Manage your customers, invoices, and payments all in one place.
    </p>
    <div className="flex gap-4 justify-center mt-8">
      <Button asChild>
        <Link to="/customers">Manage Customers</Link>
      </Button>
      <Button asChild>
        <Link to="/invoices">Manage Invoices</Link>
      </Button>
      <Button asChild variant="outline">
        <Link to="/payments">View Payments</Link>
      </Button>
    </div>
  </div>
)

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

