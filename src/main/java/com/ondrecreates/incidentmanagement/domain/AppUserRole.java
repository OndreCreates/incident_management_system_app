package com.ondrecreates.incidentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Local role assignment, keyed by the JWT's sub (email) -- identity_server_app's tokens
 * carry no role claim, and adding one there would mean changing a different portfolio
 * project. Anyone with no row here defaults to MEMBER (see AuthorizationService).
 */
@Entity
@Table(name = "app_user_role")
public class AppUserRole {

    @Id
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AppUserRole() {
        // JPA
    }

    public AppUserRole(String userEmail, Role role) {
        this.userEmail = userEmail;
        this.role = role;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
