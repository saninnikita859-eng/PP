package com.challengehub.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileUserDto(
    val id: String,
    val username: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    @SerialName("auth_provider") val authProvider: String = "password",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_background") val profileBackground: String? = null,
    val bio: String? = null,
    val xp: Int,
    @SerialName("is_premium") val isPremium: Boolean,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class TokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    val user: MobileUserDto,
)

@Serializable
data class PublicProfileDto(
    val user: MobileUserDto,
    val badges: List<BadgeDto> = emptyList(),
    val videos: List<VideoDto> = emptyList(),
    val challenges: List<ChallengeDto> = emptyList(),
    @SerialName("followers_count") val followersCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("is_following") val isFollowing: Boolean = false,
)

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class RegisterRequestDto(
    val username: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val password: String,
)

@Serializable
data class ChallengeDto(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    @SerialName("duration_days") val durationDays: Int,
    @SerialName("xp_reward") val xpReward: Int,
    @SerialName("challenge_type") val challengeType: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("creator_name") val creatorName: String? = null,
    @SerialName("participants_count") val participantsCount: Int = 0,
    @SerialName("participation_status") val participationStatus: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("progress_videos") val progressVideos: Int = 0,
)

@Serializable
data class VideoDto(
    val id: String,
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("challenge_type") val challengeType: String = "SPONSORED",
    @SerialName("author_user_id") val authorUserId: String,
    val url: String,
    val description: String? = null,
    @SerialName("ai_status") val aiStatus: String,
    val status: String,
    @SerialName("reports_count") val reportsCount: Int,
    @SerialName("is_ad") val isAd: Boolean,
    @SerialName("views_count") val viewsCount: Int,
    @SerialName("likes_count") val likesCount: Int,
    @SerialName("shares_count") val sharesCount: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("author_display_name") val authorDisplayName: String? = null,
    @SerialName("author_username") val authorUsername: String? = null,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("challenge_title") val challengeTitle: String? = null,
    @SerialName("challenge_xp_reward") val challengeXpReward: Int? = null,
)

@Serializable
data class JoinChallengeRequestDto(@SerialName("challenge_type") val challengeType: String)

@Serializable
data class CommentDto(
    val id: String,
    @SerialName("video_id") val videoId: String,
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val text: String,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CreateCommentRequestDto(val text: String)

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val xp: Int,
)

@Serializable
data class BadgeDto(
    val id: String,
    @SerialName("badge_code") val badgeCode: String,
    val title: String,
    val description: String,
    @SerialName("is_pinned") val isPinned: Boolean,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CreateChallengeRequestDto(
    val title: String,
    val description: String,
    val difficulty: String,
    @SerialName("duration_days") val durationDays: Int,
    @SerialName("xp_reward") val xpReward: Int,
)

@Serializable
data class ReportVideoRequestDto(val reason: String)

@Serializable
data class GoogleLoginRequestDto(@SerialName("id_token") val idToken: String)

@Serializable
data class AssistantRequestDto(val message: String)

@Serializable
data class AssistantResponseDto(
    val answer: String,
    @SerialName("suggested_challenge_id") val suggestedChallengeId: String? = null,
)

@Serializable
data class PremiumCheckoutDto(@SerialName("checkout_url") val checkoutUrl: String)

@Serializable
data class IntegrationStatusDto(
    val provider: String,
    val connected: Boolean,
    @SerialName("external_account_name") val externalAccountName: String? = null,
)

@Serializable
data class IntegrationConnectDto(@SerialName("connect_url") val connectUrl: String)

@Serializable
data class VideoExportDto(
    val provider: String,
    val exported: Boolean,
    @SerialName("external_url") val externalUrl: String? = null,
    val message: String,
)

@Serializable
data class FcmTokenRequestDto(val token: String)

@Serializable
data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class PinBadgesRequestDto(@SerialName("badge_ids") val badgeIds: List<String>)

@Serializable
data class UpdateProfileRequestDto(
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_background") val profileBackground: String? = null,
)
