package com.gmao.service.repository;

import com.gmao.entity.PieceDetachee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PieceDetacheeRepository extends JpaRepository<PieceDetachee, Integer> {
    boolean existsByReference(String reference);
}
