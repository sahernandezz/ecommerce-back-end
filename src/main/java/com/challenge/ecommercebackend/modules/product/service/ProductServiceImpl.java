package com.challenge.ecommercebackend.modules.product.service;

import com.challenge.ecommercebackend.modules.product.persisten.entity.Product;
import com.challenge.ecommercebackend.modules.product.persisten.entity.ProductStatus;
import com.challenge.ecommercebackend.modules.product.persisten.repository.IProductRepository;
import com.challenge.ecommercebackend.modules.product.web.dto.request.InputProductRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(IProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).getContent();
    }

    @Override
    public List<Product> getAllProductsActive() {
        return productRepository.findAllByStatus(ProductStatus.ACTIVE);
    }

    @Override
    public List<Product> getAllProductsActiveByCategoryId(Long categoryId) {
        return productRepository.findAllByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE);
    }

    @Override
    public Product addProduct(InputProductRequest inputProductRequest) {
        Product product = modelMapper.map(inputProductRequest, Product.class);
        product.setCreatedAt(new Date());
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> updateProduct(Product product) {
        Optional<Product> productOptional = productRepository.findById(product.getId());
        if (productOptional.isPresent()) {
            product.setUpdatedAt(new Date());
            return Optional.of(productRepository.save(product));
        }
        return Optional.empty();
    }

    @Override
    public List<Product> getAllProductsActiveByName(String name) {
        return productRepository.findAllByNameContainingIgnoreCaseAndStatus(name, ProductStatus.ACTIVE);
    }
}
