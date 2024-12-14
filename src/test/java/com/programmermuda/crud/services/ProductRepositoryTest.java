package com.programmermuda.crud.services;

import com.programmermuda.crud.models.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testTableHasData() {
        List<Product> products = productRepository.findAll();
        Assertions.assertFalse(products.isEmpty());
    }
}
