import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from '../components/layout/Layout'

// Placeholder components
const HomePage = () => <div className="text-center py-12">Welcome to InvoiceMe</div>
const LoginPage = () => <div className="text-center py-12">Login (PRD 08)</div>
const CustomersPage = () => <div className="text-center py-12">Customers (PRD 03)</div>
const InvoicesPage = () => <div className="text-center py-12">Invoices (PRD 05)</div>
const PaymentsPage = () => <div className="text-center py-12">Payments (PRD 07)</div>

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="invoices" element={<InvoicesPage />} />
          <Route path="payments" element={<PaymentsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

