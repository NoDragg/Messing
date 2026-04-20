package com.example.messing.repository

import com.example.messing.entity.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServerRepository : JpaRepository<Server, String> {

    // Dùng để dựng danh sách server mà user sở hữu cho sidebar/home state.
    fun findAllByOwnerId(ownerId: String): List<Server>
}
