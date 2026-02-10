package com.dmv.texas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "state_packs")
data class StatePackEntity(
    @PrimaryKey val stateCode: String,
    val version: Int,
    val installedAt: Long,
    val questionCount: Int
)
