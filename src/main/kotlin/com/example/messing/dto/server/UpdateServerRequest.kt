package com.example.messing.dto.server

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateServerRequest(
    @field:NotBlank(message = "Tên server không được để trống")
    @field:Size(min = 2, max = 100, message = "Tên server phải từ 2 đến 100 ký tự")
    val name: String
)
