-- Create invoices table
CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'SENT', 'PAID')),
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for faster lookups
CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);

-- Create invoice_line_items table
CREATE TABLE invoice_line_items (
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    line_order INTEGER NOT NULL,
    PRIMARY KEY (invoice_id, line_order)
);

-- Create index on invoice_id for faster lookups
CREATE INDEX idx_invoice_line_items_invoice_id ON invoice_line_items(invoice_id);

