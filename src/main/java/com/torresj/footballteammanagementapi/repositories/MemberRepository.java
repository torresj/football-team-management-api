package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByNameAndSurname(String name, String surname);
}
