package com.alphapay.payEngine.alphaServices.repository;

import com.alphapay.payEngine.alphaServices.model.BinData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BinDataRepository extends JpaRepository<BinData, String> {
    // Custom finder method example
    BinData findByBin(String bin);

    @Query("SELECT b.isoCode2 FROM BinData b WHERE b.bin = :bin")
    String findIsoCode2ByBin(@Param("bin") String bin);

}

