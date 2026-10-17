package com.example.backend.repository;

import com.example.backend.model.Holding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

	List<Holding> findAllByUser_Id(Long userId);

	Optional<Holding> findByUser_IdAndStock_Id(Long userId, Long stockId);
}
