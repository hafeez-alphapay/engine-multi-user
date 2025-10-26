package com.alphapay.payEngine.storage.repository;

import com.alphapay.payEngine.storage.model.RequiredDocumentsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequiredDocumentsRepository extends JpaRepository<RequiredDocumentsCategory, Integer> {
    @Query("SELECT DISTINCT r.description FROM RequiredDocumentsCategory r WHERE r.status = 'Active'")
    List<String> findDistinctDescriptions();
    List<RequiredDocumentsCategory> findByStatusAndDescription(String status, String description);

    RequiredDocumentsCategory findById(Long id);
}
