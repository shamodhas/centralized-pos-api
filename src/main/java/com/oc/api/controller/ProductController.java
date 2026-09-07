package com.oc.api.controller;

import com.oc.api.model.tenant.Product;
import com.oc.api.repository.tenant.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            return ResponseEntity.ok(productRepository.save(product));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }
}