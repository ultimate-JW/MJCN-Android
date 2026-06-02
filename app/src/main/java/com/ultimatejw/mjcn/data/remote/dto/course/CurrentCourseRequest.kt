package com.ultimatejw.mjcn.data.remote.dto.course

import com.google.gson.annotations.SerializedName

data class CurrentCourseRequest(
    @SerializedName("offering_id") val offeringId: Int
)
