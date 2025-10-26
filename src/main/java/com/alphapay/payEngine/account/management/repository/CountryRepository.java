package com.alphapay.payEngine.account.management.repository;

import com.alphapay.payEngine.account.management.model.CountriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<CountriesEntity, Long> {
    @Override
    Optional<CountriesEntity> findById(Long aLong);
    Optional<CountriesEntity> findByNameEn(String nameEn);
}
