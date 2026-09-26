package com.prreview.repository;

import com.prreview.model.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
    // findAll(), findById(), save() etc. come free from JpaRepository.
    // We'll add custom queries (e.g. findTop10ByOrderByCreatedAtDesc) in Phase 2.
}
