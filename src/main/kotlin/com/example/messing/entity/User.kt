package com.example.messing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant

@Entity
@Table(name = "users")
class User(

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    var id: String? = null,

    @Column(name = "username", nullable = false, unique = true, length = 50)
    var username: String = "",

    @Column(name = "display_name", nullable = true, length = 100)
    var displayName: String? = null,

    @Column(name = "email", nullable = false, unique = true, length = 100)
    var email: String = "",

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    var password: String = "",

    @Column(name = "avatar_url", length = 500)
    var avatarUrl: String? = null,

    @Column(name = "bio", length = 150)
    var bio: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "is_virtual", nullable = false)
    var isVirtual: Boolean = false,

    // --- Relationships ---

    // Memberships giúp xác định user đang thuộc server nào và với vai trò gì.
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    var serverMembers: MutableList<ServerMember> = mutableListOf(),

    // Messages là lịch sử chat do user gửi, không serialize trực tiếp qua API.
    @JsonIgnore
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    var messages: MutableList<Message> = mutableListOf()

) {
    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "User(id=$id, username='$username', displayName='$displayName', email='$email')"
}
