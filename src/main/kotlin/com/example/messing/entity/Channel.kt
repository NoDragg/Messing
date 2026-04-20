package com.example.messing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant

@Entity
@Table(
    name = "channels",
    uniqueConstraints = [UniqueConstraint(columnNames = ["server_id", "name"])]
)
class Channel(

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    var id: String? = null,

    @Column(name = "name", nullable = false, length = 100)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    var type: ChannelType = ChannelType.TEXT,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    // --- Relationships ---

    // Channel luôn thuộc một server cụ thể để giữ tenant boundary rõ ràng.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    var server: Server? = null,

    // Messages cascade theo channel để dọn sạch lịch sử khi channel bị xóa.
    @JsonIgnore
    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var messages: MutableList<Message> = mutableListOf()

) {
    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Channel) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Channel(id=$id, name='$name', type=$type)"
}
