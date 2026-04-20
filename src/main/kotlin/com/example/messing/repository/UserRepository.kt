package com.example.messing.repository

import com.example.messing.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {

    // Identity lookup dùng xuyên suốt auth, profile và websocket principal mapping.
    fun findByEmail(email: String): User?

    fun findByUsername(username: String): User?

    fun findByLoginName(loginName: String): User?

    fun findByEmailOrLoginName(email: String, loginName: String): User?

    // Duplicate guards cho register flow.
    fun existsByEmail(email: String): Boolean

    fun existsByUsername(username: String): Boolean

    fun existsByLoginName(loginName: String): Boolean
}
