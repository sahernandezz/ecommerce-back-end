package com.challenge.ecommercebackend.modules.product.persisten.repository;

import com.challenge.ecommercebackend.modules.product.persisten.entity.Product;
import com.challenge.ecommercebackend.modules.product.persisten.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByStatus(ProductStatus status);

    List<Product> findAllByCategoryIdAndStatus(Long categoryId, ProductStatus status);

    List<Product> findAllByNameContainingIgnoreCaseAndStatus(String name, ProductStatus status);
}
