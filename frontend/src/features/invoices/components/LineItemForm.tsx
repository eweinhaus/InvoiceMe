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
  // Store as strings to allow empty values during editing
  const [quantity, setQuantity] = useState(String(lineItem.quantity || 1))
  const [unitPrice, setUnitPrice] = useState(String(lineItem.unitPrice || 0))

  // Sync state when lineItem prop changes
  useEffect(() => {
    setDescription(lineItem.description || '')
    setQuantity(String(lineItem.quantity || 1))
    setUnitPrice(String(lineItem.unitPrice || 0))
  }, [lineItem.description, lineItem.quantity, lineItem.unitPrice])

  // Calculate subtotal from numeric values
  const subtotal = useMemo(() => {
    const qty = parseFloat(quantity) || 0
    const price = parseFloat(unitPrice) || 0
    return Number((qty * price).toFixed(2))
  }, [quantity, unitPrice])

  // Update parent when fields change (convert to numbers)
  useEffect(() => {
    const qty = parseFloat(quantity) || 1
    const price = parseFloat(unitPrice) || 0
    onChange(index, {
      description,
      quantity: qty,
      unitPrice: price,
    })
  }, [description, quantity, unitPrice, index, onChange])

  const handleDescriptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setDescription(e.target.value)
  }

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value
    // Allow empty string during typing (for deletion)
    if (value === '') {
      setQuantity(value)
      return
    }
    // Allow decimal point for partial entry (e.g., "1.")
    if (value.endsWith('.') && /^\d+\.?$/.test(value)) {
      setQuantity(value)
      return
    }
    // Only update if it's a valid positive number
    const numValue = parseFloat(value)
    if (!isNaN(numValue) && numValue > 0) {
      setQuantity(value)
    } else if (numValue === 0) {
      // Allow 0 temporarily so user can type "10" (starts with 0)
      if (value === '0' || /^0\d+$/.test(value)) {
        setQuantity(value)
      }
    }
  }

  const handleQuantityBlur = () => {
    // Validate and set default on blur
    const numValue = parseFloat(quantity)
    if (isNaN(numValue) || numValue <= 0) {
      setQuantity('1')
    } else {
      // Normalize to integer (remove decimal if whole number)
      const intValue = Math.floor(numValue)
      setQuantity(String(intValue))
    }
  }

  const handleUnitPriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value
    // Allow empty string during typing (for deletion)
    if (value === '') {
      setUnitPrice(value)
      return
    }
    // Allow decimal point for partial entry (e.g., "1.")
    if (value.endsWith('.') && /^\d+\.?$/.test(value)) {
      setUnitPrice(value)
      return
    }
    // Allow negative sign only at the start (though we'll validate it's >= 0 on blur)
    if (value === '-') {
      setUnitPrice(value)
      return
    }
    // Only update if it's a valid number
    const numValue = parseFloat(value)
    if (!isNaN(numValue)) {
      setUnitPrice(value)
    }
  }

  const handleUnitPriceBlur = () => {
    // Validate and set default on blur
    const numValue = parseFloat(unitPrice)
    if (isNaN(numValue) || numValue < 0) {
      setUnitPrice('0')
    } else {
      // Keep decimal precision
      setUnitPrice(String(numValue))
    }
  }

  return (
    <div className="grid grid-cols-12 gap-4 items-start p-4 border rounded-lg">
      <div className="col-span-12 md:col-span-4 flex flex-col">
        <Label htmlFor={`description-${index}`} className="mb-1.5">
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

      <div className="col-span-6 md:col-span-2 flex flex-col">
        <Label htmlFor={`quantity-${index}`} className="mb-1.5">
          Quantity <span className="text-destructive">*</span>
        </Label>
        <Input
          id={`quantity-${index}`}
          type="number"
          min="1"
          step="1"
          value={quantity}
          onChange={handleQuantityChange}
          onBlur={handleQuantityBlur}
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

      <div className="col-span-6 md:col-span-2 flex flex-col">
        <Label htmlFor={`unitPrice-${index}`} className="mb-1.5">
          Unit Price <span className="text-destructive">*</span>
        </Label>
        <Input
          id={`unitPrice-${index}`}
          type="number"
          min="0"
          step="0.01"
          value={unitPrice}
          onChange={handleUnitPriceChange}
          onBlur={handleUnitPriceBlur}
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

      <div className="col-span-12 md:col-span-2 flex flex-col">
        <Label className="mb-1.5">Subtotal</Label>
        <div className="h-9 px-3 border rounded-md bg-gray-50 flex items-center">
          {formatCurrency(subtotal)}
        </div>
      </div>

      <div className="col-span-12 md:col-span-2 flex flex-col">
        <Label className="mb-1.5 invisible">Remove</Label>
        <Button
          type="button"
          variant="outline"
          onClick={() => onRemove(index)}
          disabled={!canRemove}
          aria-label={`Remove line item ${index + 1}`}
          className="h-9 w-full"
        >
          Remove
        </Button>
      </div>
    </div>
  )
}

