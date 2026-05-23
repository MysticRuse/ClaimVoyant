package com.android.mr.claimvoyantapp.data

import com.google.gson.annotations.SerializedName

data class GpsData(
    val lat: Double,
    val lng: Double,
    @SerializedName("accuracy_m") val accuracyM: Double = 10.0
)

data class IncidentInfo(
    val timestamp: String,
    val gps: GpsData,
    val description: String
)

data class Party(
    val role: String,
    val name: String,
    @SerializedName("license_number") val licenseNumber: String,
    val insurer: String,
    @SerializedName("policy_number") val policyNumber: String,
    val email: String = "",
    @SerializedName("insurance_expiry") val insuranceExpiry: String = "",
    @SerializedName("vehicle_year") val vehicleYear: String = "2021",
    @SerializedName("vehicle_make") val vehicleMake: String = "Honda",
    @SerializedName("vehicle_model") val vehicleModel: String = "Civic"
)

data class DamageInfo(
    val severity: String,
    @SerializedName("areas_affected") val areasAffected: List<String>,
    val description: String,
    @SerializedName("airbag_deployed") val airbagDeployed: Boolean = false,
    @SerializedName("vehicle_drivable") val vehicleDrivable: Boolean = true,
    val confidence: Double = 1.0
)

data class EvidenceImage(
    val type: String,
    val caption: String,
    val uri: String
)

data class Evidence(
    val images: List<EvidenceImage>
)

data class ConfidenceScores(
    @SerializedName("ocr_score") val ocrScore: Double,
    @SerializedName("narrative_score") val narrativeScore: Double,
    @SerializedName("used_cloud_fallback") val usedCloudFallback: Boolean = false
)

data class ClaimPackage(
    @SerializedName("report_id") val reportId: String,
    @SerializedName("schema_version") val schemaVersion: String = "1.0",
    @SerializedName("submitted_at") val submittedAt: String,
    val incident: IncidentInfo,
    val parties: List<Party>,
    val damage: DamageInfo,
    val evidence: Evidence,
    val confidence: ConfidenceScores
)

data class ClaimResponse(
    @SerializedName("claim_id") val claimId: String,
    val status: String,
    @SerializedName("pending_claim_number") val pendingClaimNumber: String?,
    val message: String?,
    @SerializedName("sse_url") val sseUrl: String?
)

data class SseEvent(
    val event: String,
    val data: SseEventData?
)

/** Result produced by [VideoAnalyzer] – frame paths + AI-generated assessment. */
data class VideoAnalysisResult(
    val damagedAreas: List<String>,
    val severity: String,               // "minor" | "moderate" | "severe"
    val damageDescription: String,
    val incidentNarrative: String,
    val frameUris: List<String>,        // absolute paths to cached JPEG frames
    val usedCloudFallback: Boolean
)

data class SseEventData(
    val text: String? = null,
    val risk_score: Int? = null,
    val risk_notes: List<String>? = null,
    val claim_number: String? = null,
    val approved: Boolean? = null,
    val insurer_email: String? = null,
    val summary: String? = null
)
