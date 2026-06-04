-- Add image_urls column to product_reviews
ALTER TABLE `product_reviews`
  ADD COLUMN `image_urls` JSON NULL AFTER `content`;
