package com.example.messing.dto.call

data class CallSignalMessage(
    val fromUserId: String,
    val fromUsername: String,
    val toUserId: String,
    val roomId: String,
    val type: CallSignalType,
    val sdp: String? = null,
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val metadata: Map<String, Any?>? = null,
    val timestamp: Long = System.currentTimeMillis()
)
