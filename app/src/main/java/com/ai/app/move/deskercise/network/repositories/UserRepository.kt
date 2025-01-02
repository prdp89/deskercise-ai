package com.ai.app.move.deskercise.network.repositories

import com.ai.app.move.deskercise.data.Score
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.services.UserService

interface UserRepository {
    suspend fun getProfile(): User

    suspend fun update(user: User): User?

    suspend fun getScoreHistories(): List<Score>

    suspend fun requestDeleteAccount(): String

    suspend fun updateFcmToken(fcmToken: String, userId: Int): Any?

    suspend fun deleteFcmToken(fcmToken: String,  userId: Int): Any?
}

class UserRepositoryImpl(private val userService: UserService) : UserRepository {
    override suspend fun getProfile(): User {
        /**
         * {
         *   "data": {
         *     "avatar": "https://deskercise-move-backend-develop.s3.ap-southeast-1.amazonaws.com/avatar/1713873197.511109.png",
         *     "before_rank": 0,
         *     "cognito_uid": "2ed9eb4f-0f01-4c44-b18d-d3fd5fd8c184",
         *     "company": {
         *       "id": 5,
         *       "name": "CN"
         *     },
         *     "count_session_completed": 0,
         *     "country": "India",
         *     "current_rank": 0,
         *     "designation": "A",
         *     "DOB": "1989-05-27",
         *     "email": "pardeep.sharma.dev2@gmail.com",
         *     "full_name": "Pardeep",
         *     "gender": 1,
         *     "id": 393,
         *     "name": "Pardeep",
         *     "phone_number": "63604396754",
         *     "point": 4,
         *     "rank": 76,
         *     "score": 0,
         *     "special_code": "xxx",
         *     "streak": 0,
         *     "team": {
         *       "count_user": 3,
         *       "id": 47,
         *       "name": "HQ/Management",
         *       "rank": 10,
         *       "total_score": 241.0
         *     },
         *     "timezone": "Asia/Kolkata",
         *     "total_score": 4
         *   },
         *   "message": "Ok",
         *   "status": true
         * }
         */
        val response = userService.profile()
        return response.data ?: throw Exception("Unknown Error")
    }

    override suspend fun update(user: User): User? {
        val request = mapOf(
            "name" to user.name,
            "full_name" to user.fullName,
            "gender" to user.gender,
            "DOB" to user.dob,
            "designation" to user.designation,
            "phone_number" to user.phoneNumber,
            "avatar" to user.avatar,
            "country" to user.country,
            "timezone" to user.timezone,
            "team_id" to user.team?.id,
            "company_id" to user.company?.id,
        )
        return userService.update(request).data
    }

    override suspend fun getScoreHistories(): List<Score> {
        val res  = userService.scoreHistories()

        /**
         * {
         *   "data": [
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 4,
         *       "created_at": "Jan 1, 2025 3:21:37 AM",
         *       "id": 0,
         *       "name": "5 min session",
         *       "score": 0,
         *       "session_id": 10307,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 4,
         *       "created_at": "Dec 31, 2024 9:13:29 PM",
         *       "id": 0,
         *       "name": "5 min session",
         *       "score": 0,
         *       "session_id": 10306,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 7,
         *       "created_at": "Apr 29, 2024 6:21:48 PM",
         *       "id": 0,
         *       "name": "10 min session",
         *       "score": 0,
         *       "session_id": 9861,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 7,
         *       "created_at": "Apr 29, 2024 6:21:06 PM",
         *       "id": 0,
         *       "name": "10 min session",
         *       "score": 0,
         *       "session_id": 9860,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:19:25 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9859,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:18:45 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9858,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:17:42 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9857,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:14:18 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9856,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:12:21 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9855,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:06:24 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9853,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 4:02:52 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9852,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:58:20 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9851,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:52:07 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9841,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:51:09 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9840,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:47:10 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9837,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:46:25 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9836,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:42:29 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9834,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:22:57 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9828,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:09:16 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9827,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     },
         *     {
         *       "count_completed_exercise": 0,
         *       "count_exercise": 10,
         *       "created_at": "Apr 29, 2024 3:04:58 PM",
         *       "id": 0,
         *       "name": "15 min session",
         *       "score": 0,
         *       "session_id": 9826,
         *       "type": "PROGRAM",
         *       "user_id": 0
         *     }
         *   ],
         *   "message": "Ok",
         *   "status": true
         * }
         */
        return res.data ?: throw Exception("Unknown Error")
    }

    override suspend fun requestDeleteAccount(): String {
        return userService.requestDeleteAccount().data ?: throw Exception("Unknown Error")
    }

    override suspend fun updateFcmToken(fcmToken: String, userId: Int): Any? {
        return userService.updateFcmToken(
            mapOf(
                "token" to fcmToken,
                "user_id" to userId,
            )
        ).data
    }

    override suspend fun deleteFcmToken(fcmToken: String,  userId: Int): Any? {
        return userService.deleteFcmToken(
            mapOf(
                "token" to fcmToken,
                "user_id" to userId,
            )
        ).data
    }
}
