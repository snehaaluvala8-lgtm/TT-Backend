package com.example.backend.repository;

import com.example.backend.model.TradeTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

	List<TradeTransaction> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
