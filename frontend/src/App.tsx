import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { queryClient } from './lib/react-query/queryClient'
import AppRoutes from './routes'

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppRoutes />
      {import.meta.env.DEV && <ReactQueryDevtools initialIsOpen={false} />}
    </QueryClientProvider>
  )
}

export default App

