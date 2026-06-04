package com.ebusiness.config;

import com.ebusiness.entity.Product;
import com.ebusiness.repository.ProductRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductStockInitializer implements CommandLineRunner {
  private final ProductRepository productRepository;

  public ProductStockInitializer(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    List<Product> products = productRepository.findAll();
    int updated = 0;
    for (Product p : products) {
      boolean changed = false;
      if (p.getStock() == null) {
        p.setStock(10);
        changed = true;
      }
      if (p.getRestockRequests() == null) {
        p.setRestockRequests(0);
        changed = true;
      }
      if (changed) {
        productRepository.save(p);
        updated++;
      }
    }
    if (updated > 0) {
      System.out.println("Initialized stock for " + updated + " products");
    }
  }
}
