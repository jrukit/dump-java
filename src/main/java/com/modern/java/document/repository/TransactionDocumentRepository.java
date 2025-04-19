package com.modern.java.document.repository;

import com.modern.java.document.entity.UploadDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionDocumentRepository extends JpaRepository<UploadDocumentEntity, Long> {
    List<UploadDocumentEntity> findByCaseNo(String caseNo);
}
