package com.example.fidenz.repository;

import com.example.fidenz.entity.SalesTransaction;
import com.example.fidenz.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesTransactionRepository extends JpaRepository<SalesTransaction, Long> {

    List<SalesTransaction> findByStore(Store store);

    List<SalesTransaction> findByStoreId(Long storeId);

    List<SalesTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<SalesTransaction> findByStoreIdAndTransactionDateBetween(Long storeId, LocalDateTime startDate, LocalDateTime endDate);

}
