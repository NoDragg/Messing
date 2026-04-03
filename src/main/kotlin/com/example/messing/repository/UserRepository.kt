package com.example.messing.repository

import com.example.messing.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {

    fun findByEmail(email: String): User?

    fun findByUsername(username: String): User?

    fun findByLoginName(loginName: String): User?

    fun findByEmailOrLoginName(email: String, loginName: String): User?

    fun existsByEmail(email: String): Boolean

    fun existsByUsername(username: String): Boolean

    fun existsByLoginName(loginName: String): Boolean
}
