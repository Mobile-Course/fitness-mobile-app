package com.fitness.app.data.api

import com.fitness.app.data.model.AchievementDto
import com.fitness.app.data.model.AchievementXpDto
import com.fitness.app.data.model.UserAchievementDto
import retrofit2.Response
import retrofit2.http.GET

interface AchievementsApiService {
    @GET("api/achievements")
    suspend fun getAllAchievements(): Response<List<AchievementDto>>

    @GET("api/achievements/me")
    suspend fun getMyAchievements(): Response<List<UserAchievementDto>>

    @GET("api/achievements/xp")
    suspend fun getMyXp(): Response<AchievementXpDto>
}
