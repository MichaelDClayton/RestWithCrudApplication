package com.restwithcrud.services;

import org.springframework.data.jpa.repository.JpaRepository;
import com.restwithcrud.models.Product;
public interface ProductRepository extends JpaRepository<Product, Integer> {

}
