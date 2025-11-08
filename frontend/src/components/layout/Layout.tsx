import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/lib/hooks/useAuth'
import { queryClient } from '@/lib/react-query/queryClient'

export default function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAuth()

  const navLinks = [
    { path: '/', label: 'Home' },
    { path: '/customers', label: 'Customers' },
    { path: '/invoices', label: 'Invoices' },
    { path: '/payments', label: 'Payments' },
  ]

  const handleLogout = async () => {
    try {
      // Call backend logout endpoint
      const response = await fetch('http://localhost:8080/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
        redirect: 'manual', // Don't follow redirects
      })
      
      // Check if response is ok (200) or redirect (302)
      if (!response.ok && response.status !== 302) {
        console.error('Logout failed:', response.status)
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // Always clear React Query cache and redirect, even if logout request fails
      queryClient.clear()
      queryClient.removeQueries({ queryKey: ['auth', 'user'] })
      // Redirect to login immediately
      navigate('/login', { replace: true })
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold">InvoiceMe</h1>
            <div className="flex items-center gap-4">
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
              {isAuthenticated && user && (
                <div className="flex items-center gap-3 pl-4 border-l border-gray-200">
                  {user.picture && (
                    <img
                      src={user.picture}
                      alt={user.name || user.email || 'User'}
                      className="w-8 h-8 rounded-full"
                    />
                  )}
                  <span className="text-sm text-gray-700">
                    {user.name || user.email}
                  </span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleLogout}
                    className="text-xs"
                  >
                    Logout
                  </Button>
                </div>
              )}
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

