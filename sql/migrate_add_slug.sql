-- Add slug column to products and fill with default slugs based on id
ALTER TABLE products
  ADD COLUMN slug VARCHAR(150) DEFAULT NULL,
  ADD UNIQUE INDEX idx_products_slug (slug);

-- Populate slug for existing rows where slug is null. Use a simple slug: lower(replace(name,' ', '-'))-id
UPDATE products
SET slug = CONCAT(LOWER(REPLACE(name, ' ', '-')),'-',id)
WHERE slug IS NULL;

-- Ensure no nulls remain
ALTER TABLE products
  MODIFY COLUMN slug VARCHAR(150) NOT NULL;
