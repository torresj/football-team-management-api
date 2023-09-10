package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MatchEntity;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
}
