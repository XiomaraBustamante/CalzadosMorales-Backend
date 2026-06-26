package com.calzadosmorales.repository;

import com.calzadosmorales.entity.ProductoTalla;
import com.calzadosmorales.entity.ProductoTallaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoTallaRepository extends JpaRepository<ProductoTalla, ProductoTallaKey> {
    
}