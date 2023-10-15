package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MatchEntity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
    Optional<MatchEntity> findByMatchDayGreaterThanEqual(LocalDate date);
    Optional<MatchEntity> findByMatchDay(LocalDate matchDay);
    List<MatchEntity> findByClosedAndMatchDayBefore(boolean closed, LocalDate matchDay);
}
