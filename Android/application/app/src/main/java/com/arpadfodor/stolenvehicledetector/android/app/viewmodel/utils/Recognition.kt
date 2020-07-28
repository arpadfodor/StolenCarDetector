package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils

import android.graphics.Bitmap

data class Recognition(
    val artificialId: Int,
    val licenseId: String,
    val image: Bitmap,
    val date: String,
    val latitude: String,
    val longitude: String,
    var message: String = ""
    )