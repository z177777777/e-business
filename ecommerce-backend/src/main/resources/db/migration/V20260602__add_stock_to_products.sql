-- Add stock and restock_requests columns to products
ALTER TABLE `products`
  ADD COLUMN `stock` INT DEFAULT 10 AFTER `sold_count`,
  ADD COLUMN `restock_requests` INT DEFAULT 0 AFTER `stock`;

-- Set default stock for existing products
UPDATE `products` SET `stock` = 10 WHERE `stock` IS NULL;
UPDATE `products` SET `restock_requests` = 0 WHERE `restock_requests` IS NULL;
