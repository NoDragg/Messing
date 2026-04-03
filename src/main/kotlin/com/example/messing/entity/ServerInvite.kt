package com.example.messing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant

@Entity
@Table(
    name = "server_invites",
    uniqueConstraints = [UniqueConstraint(columnNames = ["code"])]
)
class ServerInvite(

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    var id: String? = null,

    @Column(name = "code", nullable = false, length = 32, unique = true)
    var code: String = "",

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    var expiresAt: Instant? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    var server: Server? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User? = null
) {
    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }
}
