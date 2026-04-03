package com.example.messing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant

@Entity
@Table(name = "servers")
class Server(

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    var id: String? = null,

    @Column(name = "name", nullable = false, length = 100)
    var name: String = "",

    @Column(name = "icon_url", length = 500)
    var iconUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null,

    @JsonIgnore
    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var channels: MutableList<Channel> = mutableListOf(),

    @JsonIgnore
    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var members: MutableList<ServerMember> = mutableListOf()

) {
    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Server) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Server(id=$id, name='$name')"
}
