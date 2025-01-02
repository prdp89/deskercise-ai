package com.ai.app.move.deskercise.network.repositories

import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.PersonalBoardResponse
import com.ai.app.move.deskercise.network.responses.TeamBoardResponse
import com.ai.app.move.deskercise.network.services.LeaderBoardService

interface LeaderBoardRepository {
    suspend fun getPersonalBoard(filter: String): List<PersonalBoardResponse>

    suspend fun getTeamBoard(filter: String): List<TeamBoardResponse>

    suspend fun getProfileBoard(filter: String, type: String): User
}

class LeaderBoardRepositoryImpl(private val leaderBoardService: LeaderBoardService) : LeaderBoardRepository {
    override suspend fun getPersonalBoard(filter: String): List<PersonalBoardResponse> {
        val res = leaderBoardService.getPersonalBoard(filter)
        /**
         * {
         *   "data": [
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 385,
         *       "name": "tester",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 377,
         *       "name": "wz",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 231,
         *       "name": "x",
         *       "score": 0
         *     },
         *     {
         *       "avatar": "https://deskercise-move-backend-develop.s3.ap-southeast-1.amazonaws.com/avatar/1689704735.904931.png",
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 238,
         *       "name": "wallie",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 240,
         *       "name": "Raulie",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 390,
         *       "name": "tttt",
         *       "score": 0
         *     },
         *     {
         *       "avatar": "https://deskercise-move-backend-develop.s3.ap-southeast-1.amazonaws.com/avatar/1690209989.316582.png",
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 249,
         *       "name": "aaron",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 365,
         *       "name": "harrt",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 384,
         *       "name": "Aaron Test",
         *       "score": 0
         *     },
         *     {
         *       "avatar": "https://deskercise-move-backend-develop.s3.ap-southeast-1.amazonaws.com/avatar/1695891651.112357.png",
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 220,
         *       "name": "Test 007",
         *       "score": 0
         *     }
         *   ],
         *   "message": "Ok",
         *   "status": true
         * }
         */
        return (res.data ?: throw Exception("Unknown Error"))
    }

    override suspend fun getTeamBoard(filter: String): List<TeamBoardResponse> {
        val res = leaderBoardService.getTeamBoard(filter)
        /**
         * {
         *   "data": [
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 49,
         *       "name": "Sales",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 34,
         *       "name": "Founders\u0027 Office",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 36,
         *       "name": "Technology",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 33,
         *       "name": "Business",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 48,
         *       "name": "Marketing",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 46,
         *       "name": "Ecommerce Operations",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 47,
         *       "name": "HQ/Management",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 35,
         *       "name": "Operations",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 5,
         *       "name": "Country Marketing",
         *       "score": 0
         *     },
         *     {
         *       "before_rank": 1,
         *       "current_rank": 1,
         *       "id": 22,
         *       "name": "Legal",
         *       "score": 0
         *     }
         *   ],
         *   "message": "Ok",
         *   "status": true
         * }
         */
        return res.data ?: throw Exception("Unknown Error")
    }

    override suspend fun getProfileBoard(filter: String, type: String): User {
        val res = leaderBoardService.getProfileBoard(filter, type)
        /**
         * {
         *   "data": {
         *     "before_rank": 1,
         *     "count_session_completed": 0,
         *     "current_rank": 1,
         *     "gender": 0,
         *     "id": 0,
         *     "point": 0,
         *     "rank": 0,
         *     "score": 0,
         *     "streak": 0,
         *     "total_score": 0
         *   },
         *   "message": "Ok",
         *   "status": true
         * }
         */
        return res.data ?: throw Exception("Unknown Error")
    }
}
