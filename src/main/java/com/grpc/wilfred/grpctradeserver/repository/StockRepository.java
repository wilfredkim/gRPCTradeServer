package com.grpc.wilfred.grpctradeserver.repository;

import com.grpc.wilfred.grpctradeserver.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByStockSymbol(String stockSymbol);
}
