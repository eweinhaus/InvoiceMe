import { Navigate } from 'react-router-dom'
import { useAuth } from '@/lib/hooks/useAuth'
import LoadingSpinner from './LoadingSpinner'

interface ProtectedRouteProps {
  children: React.ReactNode
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  // AUTHENTICATION DISABLED - for local development
  // To re-enable authentication, uncomment the code below and remove the return statement
  return <>{children}</>

  /* Uncomment to re-enable authentication:
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
  */
}

