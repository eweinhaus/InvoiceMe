# InvoiceMe Frontend

React + TypeScript + Vite frontend for InvoiceMe.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create `.env` file (copy from `.env.example`):
```bash
cp .env.example .env
```

3. Start development server:
```bash
npm run dev
```

## Type Generation

To generate TypeScript types from the backend OpenAPI spec:

1. Ensure the backend is running on `http://localhost:8080`
2. Run:
```bash
npm run generate:types
```

This will generate types in `src/types/api.ts`.

## Project Structure

```
src/
├── components/     # React components
│   ├── common/    # Shared components (LoadingSpinner, ErrorMessage, etc.)
│   ├── layout/    # Layout components
│   └── ui/        # shadcn/ui components
├── lib/           # Utilities and configurations
│   ├── api/       # API client (Axios)
│   ├── hooks/     # Custom hooks
│   ├── react-query/ # React Query configuration
│   └── utils/     # Utility functions
├── routes/        # Routing configuration
└── types/         # TypeScript types (generated from OpenAPI)
```

