package com.android.mr.claimvoyantapp.data

import retrofit2.http.Body
import retrofit2.http.POST

interface ClaimApiService {
    @POST("api/claims")
    suspend fun createClaim(@Body pkg: ClaimPackage): ClaimResponse
}
