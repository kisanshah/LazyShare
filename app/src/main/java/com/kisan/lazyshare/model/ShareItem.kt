package com.kisan.lazyshare.model

import android.net.Uri


data class ShareItem(
    val type: Int,
    val name: String,
    val id: Long,
    val size: Double,
    val contentUri: Uri,
    val width: Int,
    val height: Int,
)