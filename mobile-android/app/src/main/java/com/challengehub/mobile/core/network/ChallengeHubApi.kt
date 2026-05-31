package com.challengehub.mobile.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ChallengeHubApi {
    @POST("mobile/auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenDto

    @POST("mobile/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): TokenDto

    @POST("mobile/auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequestDto): TokenDto

    @GET("mobile/auth/me")
    suspend fun me(@Header("Authorization") authorization: String): MobileUserDto

    @GET("mobile/users/{id}/profile")
    suspend fun publicProfile(
        @Path("id") id: String,
        @Header("Authorization") authorization: String? = null,
    ): PublicProfileDto

    @POST("mobile/users/{id}/follow")
    suspend fun followUser(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): PublicProfileDto

    @DELETE("mobile/users/{id}/follow")
    suspend fun unfollowUser(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): PublicProfileDto

    @GET("mobile/feed")
    suspend fun feed(@Header("Authorization") authorization: String? = null): List<VideoDto>

    @GET("mobile/challenges")
    suspend fun challenges(): List<ChallengeDto>

    @POST("mobile/challenges")
    suspend fun createChallenge(
        @Header("Authorization") authorization: String,
        @Body body: CreateChallengeRequestDto,
    ): ChallengeDto

    @POST("mobile/challenges/{id}/join")
    suspend fun joinChallenge(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: JoinChallengeRequestDto,
    )

    @POST("mobile/challenges/{id}/leave")
    suspend fun leaveChallenge(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: JoinChallengeRequestDto,
    )

    @POST("mobile/videos/{id}/like")
    suspend fun likeVideo(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): VideoDto

    @DELETE("mobile/videos/{id}/like")
    suspend fun unlikeVideo(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): VideoDto

    @GET("mobile/videos/{id}/comments")
    suspend fun comments(
        @Path("id") id: String,
        @Header("Authorization") authorization: String? = null,
    ): List<CommentDto>

    @POST("mobile/videos/{id}/comments")
    suspend fun addComment(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: CreateCommentRequestDto,
    ): CommentDto

    @POST("mobile/comments/{id}/like")
    suspend fun likeComment(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): CommentDto

    @DELETE("mobile/comments/{id}/like")
    suspend fun unlikeComment(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): CommentDto

    @POST("mobile/videos/{id}/report")
    suspend fun reportVideo(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body body: ReportVideoRequestDto,
    )

    @POST("mobile/videos/{id}/share")
    suspend fun shareVideo(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    )

    @POST("mobile/videos/{id}/view")
    suspend fun viewVideo(@Path("id") id: String)

    @Multipart
    @POST("mobile/videos")
    suspend fun uploadVideo(
        @Header("Authorization") authorization: String,
        @Part("challenge_id") challengeId: RequestBody,
        @Part("challenge_type") challengeType: RequestBody,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
    ): VideoDto

    @GET("mobile/leaderboard")
    suspend fun leaderboard(@Query("period") period: String = "weekly"): List<LeaderboardEntryDto>

    @GET("mobile/profile/me/badges")
    suspend fun myBadges(@Header("Authorization") authorization: String): List<BadgeDto>

    @GET("mobile/profile/me/videos")
    suspend fun myVideos(@Header("Authorization") authorization: String): List<VideoDto>

    @GET("mobile/profile/me/challenges")
    suspend fun myChallenges(@Header("Authorization") authorization: String): List<ChallengeDto>

    @PATCH("mobile/profile/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body body: UpdateProfileRequestDto,
    ): MobileUserDto

    @Multipart
    @POST("mobile/profile/me/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
    ): MobileUserDto

    @POST("mobile/profile/me/pinned-badges")
    suspend fun pinBadges(
        @Header("Authorization") authorization: String,
        @Body body: PinBadgesRequestDto,
    ): List<BadgeDto>

    @POST("mobile/assistant")
    suspend fun assistant(
        @Header("Authorization") authorization: String,
        @Body body: AssistantRequestDto,
    ): AssistantResponseDto

    @POST("mobile/premium/checkout")
    suspend fun premiumCheckout(@Header("Authorization") authorization: String): PremiumCheckoutDto

    @GET("mobile/integrations/youtube/status")
    suspend fun youtubeStatus(@Header("Authorization") authorization: String): IntegrationStatusDto

    @POST("mobile/integrations/youtube/connect-url")
    suspend fun youtubeConnectUrl(@Header("Authorization") authorization: String): IntegrationConnectDto

    @DELETE("mobile/integrations/youtube")
    suspend fun disconnectYoutube(@Header("Authorization") authorization: String)

    @POST("mobile/videos/{id}/export/youtube")
    suspend fun exportYoutube(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): VideoExportDto

    @GET("mobile/integrations/tiktok/status")
    suspend fun tiktokStatus(@Header("Authorization") authorization: String): IntegrationStatusDto

    @POST("mobile/integrations/tiktok/connect-url")
    suspend fun tiktokConnectUrl(@Header("Authorization") authorization: String): IntegrationConnectDto

    @DELETE("mobile/integrations/tiktok")
    suspend fun disconnectTiktok(@Header("Authorization") authorization: String)

    @POST("mobile/videos/{id}/export/tiktok")
    suspend fun exportTiktok(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): VideoExportDto

    @POST("mobile/notifications/fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") authorization: String,
        @Body body: FcmTokenRequestDto,
    )

    @GET("mobile/notifications")
    suspend fun notifications(@Header("Authorization") authorization: String): List<NotificationDto>

    @POST("mobile/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    )
}
