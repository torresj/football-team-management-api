package com.torresj.footballteammanagementapi.entities;

import com.torresj.footballteammanagementapi.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter()
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String alias;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String password;

    @Column
    private int nCaptaincies;

    @Column
    private long nonce;

    @Column(nullable = false)
    private boolean injured;

    @Column(nullable = false)
    private boolean blocked;
}
