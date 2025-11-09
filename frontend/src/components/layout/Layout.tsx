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
        <div className="mx-auto px-6 py-4">
          <div className="flex items-center justify-between gap-6 overflow-visible">
            <h1 className="text-2xl font-bold flex-shrink-0">InvoiceMe</h1>
            <div className="flex items-center gap-4 flex-shrink-0 overflow-visible">
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
              {/* Authentication disabled for local development */}
            </div>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}

