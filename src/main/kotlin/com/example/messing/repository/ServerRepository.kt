package com.example.messing.repository

import com.example.messing.entity.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServerRepository : JpaRepository<Server, String> {

    fun findAllByOwnerId(ownerId: String): List<Server>
}
