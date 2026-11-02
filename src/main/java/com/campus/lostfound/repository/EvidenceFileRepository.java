package com.campus.lostfound.repository;

import com.campus.lostfound.entity.EvidenceFile;
import com.campus.lostfound.entity.enums.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceFileRepository extends JpaRepository<EvidenceFile, Long> {

    Optional<EvidenceFile> findByStorageKey(String storageKey);

    List<EvidenceFile> findByItemReportId(Long itemReportId);

    List<EvidenceFile> findByClaimId(Long claimId);

    Optional<EvidenceFile> findFirstByItemReportIdAndEvidenceType(Long itemReportId, EvidenceType evidenceType);

    Optional<EvidenceFile> findFirstByClaimIdAndEvidenceType(Long claimId, EvidenceType evidenceType);

    void deleteByStorageKey(String storageKey);
}

