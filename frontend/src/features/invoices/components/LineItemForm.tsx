import { useState, useEffect, useMemo } from 'react'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import type { LineItemRequest } from '@/features/invoices/types/invoice.types'
import { formatCurrency } from '@/lib/utils/formatters'

interface LineItemFormProps {
  lineItem: LineItemRequest
  index: number
  onChange: (index: number, lineItem: LineItemRequest) => void
  onRemove: (index: number) => void
  errors?: Record<string, string>
  canRemove?: boolean
}

export function LineItemForm({
  lineItem,
  index,
  onChange,
  onRemove,
  errors,
  canRemove = true,
}: LineItemFormProps) {
  const [description, setDescription] = useState(lineItem.description || '')
  const [quantity, setQuantity] = useState(lineItem.quantity || 1)
  const [unitPrice, setUnitPrice] = useState(lineItem.unitPrice || 0)

  // Calculate subtotal
  const subtotal = useMemo(() => {
    return Number((quantity * unitPrice).toFixed(2))
  }, [quantity, unitPrice])

  // Update parent when fields change
  useEffect(() => {
    onChange(index, {
      description,
      quantity,
      unitPrice,
    })
  }, [description, quantity, unitPrice, index, onChange])

  const handleDescriptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setDescription(e.target.value)
  }

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseFloat(e.target.value) || 0
    setQuantity(value > 0 ? value : 1)
  }

  const handleUnitPriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseFloat(e.target.value) || 0
    setUnitPrice(value >= 0 ? value : 0)
  }

  return (
    <div className="grid grid-cols-12 gap-4 items-end p-4 border rounded-lg">
      <div className="col-span-12 md:col-span-5">
        <Label htmlFor={`description-${index}`}>
          Description <span className="text-destructive">*</span>
        </Label>
        <Input
          id={`description-${index}`}
          type="text"
          value={description}
          onChange={handleDescriptionChange}
          placeholder="Item description"
          aria-label={`Line item ${index + 1} description`}
          aria-required="true"
          aria-invalid={!!errors?.description}
          className={errors?.description ? 'border-destructive' : ''}
        />
        {errors?.description && (
          <p className="text-sm text-destructive mt-1" role="alert">
            {errors.description}
          </p>
        )}
      </div>

      <div className="col-span-6 md:col-span-2">
        <Label htmlFor={`quantity-${index}`}>
          Quantity <span className="text-destructive">*</span>
        </Label>
        <Input
          id={`quantity-${index}`}
          type="number"
          min="1"
          step="1"
          value={quantity}
          onChange={handleQuantityChange}
          aria-label={`Line item ${index + 1} quantity`}
          aria-required="true"
          aria-invalid={!!errors?.quantity}
          className={errors?.quantity ? 'border-destructive' : ''}
        />
        {errors?.quantity && (
          <p className="text-sm text-destructive mt-1" role="alert">
            {errors.quantity}
          </p>
        )}
      </div>

      <div className="col-span-6 md:col-span-2">
        <Label htmlFor={`unitPrice-${index}`}>
          Unit Price <span className="text-destructive">*</span>
        </Label>
        <Input
          id={`unitPrice-${index}`}
          type="number"
          min="0"
          step="0.01"
          value={unitPrice}
          onChange={handleUnitPriceChange}
          aria-label={`Line item ${index + 1} unit price`}
          aria-required="true"
          aria-invalid={!!errors?.unitPrice}
          className={errors?.unitPrice ? 'border-destructive' : ''}
        />
        {errors?.unitPrice && (
          <p className="text-sm text-destructive mt-1" role="alert">
            {errors.unitPrice}
          </p>
        )}
      </div>

      <div className="col-span-12 md:col-span-2">
        <Label>Subtotal</Label>
        <div className="h-10 px-3 py-2 border rounded-md bg-muted flex items-center">
          {formatCurrency(subtotal)}
        </div>
      </div>

      <div className="col-span-12 md:col-span-1">
        <Button
          type="button"
          variant="outline"
          onClick={() => onRemove(index)}
          disabled={!canRemove}
          aria-label={`Remove line item ${index + 1}`}
        >
          Remove
        </Button>
      </div>
    </div>
  )
}

