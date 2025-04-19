package com.modern.java.document.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionDocumentRepository extends JpaRepository<UploadDocumentEntity, Long> {
    List<UploadDocumentEntity> findByCaseNo(String caseNo);
}
