package com.example.backend.repository;

import com.example.backend.model.Stock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

	boolean existsBySymbolIgnoreCase(String symbol);

	Optional<Stock> findBySymbolIgnoreCase(String symbol);

	List<Stock> findAllByOrderBySymbolAsc();
}
