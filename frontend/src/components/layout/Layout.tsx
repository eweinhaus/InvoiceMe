import { Outlet, Link, useLocation } from 'react-router-dom'
import { Button } from '@/components/ui/button'

export default function Layout() {
  const location = useLocation()

  const navLinks = [
    { path: '/', label: 'Home' },
    { path: '/customers', label: 'Customers' },
    { path: '/invoices', label: 'Invoices' },
    { path: '/payments', label: 'Payments' },
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold">InvoiceMe</h1>
            <nav className="flex gap-2">
              {navLinks.map((link) => {
                const isActive = location.pathname === link.path
                return (
                  <Button
                    key={link.path}
                    asChild
                    variant={isActive ? 'default' : 'ghost'}
                  >
                    <Link to={link.path}>{link.label}</Link>
                  </Button>
                )
              })}
            </nav>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}

