package com.example.messing.dto.call

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CallSignalRequest(
    @field:NotBlank(message = "toUserId is required")
    val toUserId: String,

    @field:NotBlank(message = "roomId is required")
    val roomId: String,

    @field:NotNull(message = "type is required")
    val type: CallSignalType,

    val sdp: String? = null,
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val metadata: Map<String, Any?>? = null
)
