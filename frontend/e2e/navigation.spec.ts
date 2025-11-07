import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('should navigate to customers page', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Look for navigation link in header (first one)
    const customersLink = page.getByRole('link', { name: 'Customers' }).first();
    await customersLink.click();
    await expect(page).toHaveURL(/.*customers.*/);
  });

  test('should navigate to invoices page', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Look for navigation link in header (first one)
    const invoicesLink = page.getByRole('link', { name: 'Invoices' }).first();
    await invoicesLink.click();
    await expect(page).toHaveURL(/.*invoices.*/);
  });

  test('should have navigation bar', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Check for navigation links (use first() to avoid strict mode violations)
    await expect(page.getByRole('link', { name: 'Home' }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: 'Customers' }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: 'Invoices' }).first()).toBeVisible();
  });
});

