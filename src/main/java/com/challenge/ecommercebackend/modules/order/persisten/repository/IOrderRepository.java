package com.challenge.ecommercebackend.modules.order.persisten.repository;

import com.challenge.ecommercebackend.modules.order.persisten.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
}
