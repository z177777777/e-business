-- Add received_at column for tracking when user confirms receipt
-- Compatible with MySQL/MariaDB and most SQL DBs using TIMESTAMP/DATETIME
ALTER TABLE `orders`
  ADD COLUMN `received_at` DATETIME NULL COMMENT '用户确认收货时间';
