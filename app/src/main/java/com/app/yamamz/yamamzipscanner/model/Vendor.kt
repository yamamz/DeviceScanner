package com.app.yamamz.yamamzipscanner.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macvendor")
data class Vendor(@PrimaryKey val mac:String, val vendor:String)