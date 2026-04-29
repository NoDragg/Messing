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

    @Column(name = "bot_user_id", length = 36)
    var botUserId: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    // --- Relationships ---

    // Owner là nguồn quyền lực chính cho các thao tác quản trị server.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null,

    // Xóa server sẽ cascade xuống toàn bộ channels để tránh dữ liệu mồ côi.
    @JsonIgnore
    @OneToMany(mappedBy = "server", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var channels: MutableList<Channel> = mutableListOf(),

    // Danh sách members phục vụ kiểm tra membership/role khi authorize.
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
