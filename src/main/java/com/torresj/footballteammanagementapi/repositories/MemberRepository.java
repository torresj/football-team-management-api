package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MemberEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MemberRepository extends CrudRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByNameAndSurname(String name, String surname);
}
