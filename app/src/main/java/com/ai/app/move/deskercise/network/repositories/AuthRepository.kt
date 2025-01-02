package com.ai.app.move.deskercise.network.repositories

import android.util.Log
import com.ai.app.move.deskercise.network.responses.ForgotResponse
import com.ai.app.move.deskercise.network.responses.LoginResponse
import com.ai.app.move.deskercise.network.responses.LogoutResponse
import com.ai.app.move.deskercise.network.responses.RefreshTokenResponse
import com.ai.app.move.deskercise.network.responses.ResetResponse
import com.ai.app.move.deskercise.network.responses.Response
import com.ai.app.move.deskercise.network.responses.SignUpResponse
import com.ai.app.move.deskercise.network.responses.VerifyResponse
import com.ai.app.move.deskercise.network.services.AuthService

interface AuthRepository {
    suspend fun login(email: String, password: String): Response<LoginResponse>

    suspend fun forgot(email: String): Response<ForgotResponse>

    suspend fun logout(token: String): LogoutResponse?

    suspend fun reset(
        email: String,
        confirmation_code: String,
        new_password: String,
    ): Response<ResetResponse>

    suspend fun signup(email: String, password: String, name: String, signUpCode: String?): Response<SignUpResponse>

    suspend fun verify(email: String, confirmation_code: String): Response<VerifyResponse>

    suspend fun resent(email: String): Any?

    suspend fun refreshToken(refreshToken: String): RefreshTokenResponse
}

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {
    override suspend fun login(email: String, password: String): Response<LoginResponse> {
        val map = authService.login(
            mapOf(
                "email" to email,
                "password" to password,
            ),
        )

        /**
         * {
         *   "status": true,
         *   "message": "Ok",
         *   "data": {
         *     "access_token": "eyJraWQiOiJMNE1rQ1wvQUZWWkxFc0NLNGpzc1ErUDQrN2xWQVdvWFV5Z0tKbStXOUhSbz0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIyZWQ5ZWI0Zi0wZjAxLTRjNDQtYjE4ZC1kM2ZkNWZkOGMxODQiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuYXAtc291dGhlYXN0LTEuYW1hem9uYXdzLmNvbVwvYXAtc291dGhlYXN0LTFfQVpoTnZrNUtjIiwiY2xpZW50X2lkIjoiN2l2bmg5dnAzaWNuNjVlcGlyN291bnRtbHMiLCJvcmlnaW5fanRpIjoiODdiZTgyNGQtY2E2ZS00YTUxLTgyNWItMDdkOWQyMGQ2ZTk4IiwiZXZlbnRfaWQiOiI1OWY4N2JhZS0yNWEwLTRiNWUtYjE2Zi1jNzIzZDcwMTM0ODUiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6ImF3cy5jb2duaXRvLnNpZ25pbi51c2VyLmFkbWluIiwiYXV0aF90aW1lIjoxNzM1NjY2NjU1LCJleHAiOjE3MzU3NTMwNTUsImlhdCI6MTczNTY2NjY1NSwianRpIjoiZjdmZTM0MmMtMzE1Yy00ODcyLWJkNmQtNDhiMmU5MmZmNWU0IiwidXNlcm5hbWUiOiIyZWQ5ZWI0Zi0wZjAxLTRjNDQtYjE4ZC1kM2ZkNWZkOGMxODQifQ.e0oujyfkOtve0Wd68H-bcP4UjfCMpfcmduajnJYM98gyyBppQabRFlhFMZyLhZFZnGEqDzV6l-152m1vcTLRxSPyJb_GTDxPvEPAuGvcIqzZU4wHOFLvXtnzR5v5xNsKRK5FXPCml-0phfy6yT6UcQ6o3HWFPx9iUXLZ3mDX5w7Ihf4ydEyMxlEd14cAvCNbPqiPyAVcqcokNidUBZZqZP-iZP7OGzBxb8NC9SNSc2eB6ZsXzcirD3KYgH4pG8AAhZZ8sRhTdN1Mr-dDIe2Xw05dt9j8I-mijrWvsWaB0ZUCRXr1Z68ZWW_B3mJImSaM0iGEqoTNzwE1Ue0kTU9Ldw",
         *     "refresh_token": "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.tYFa64yzX-KaXGVmfuJJhPiAdrnHtYvc8sKRVamPFPZpiwiE2t0EcA4Fy8DEVr9-7v2lhazE9U6JLV-ek1ecNwQ-VDoi9Yk_WphtprRcWEYlch4Ffzf4jrpEw8wQk4effLaigBWPvEBMJW1ZxSpEqR4M-_jEOVovE-hwcVsD7kVQ5Eo7Bhw5d-zu4V32iAhwkGk_uMcG1XqF1dnq4nbXXnmBSacFmEYY19OT9weQb-W9X_UWntS0s88jnvDohBuprpN2Ztfw84DrdPUNIJvJqJj-1msj_hJx4OMNgP_M1jZwRIgSWLaNjFXXVTDzn3Eq9LSLXjhlgrRpQg1-XzKXeA.iuqpYWqLJ3PNmPdc.g4xhsaULM0OqF4uTAUO8qJdKeKFZ5g5i6PQazROEIdfxoLFHhe5cxpQIkCGaMeoyufzqMBoQ54cG7vT629QYJhgxeZV4diBjqVKaK9Muc2r5r5o-DEUIHnAvfIAZ5Gb0P6Lm8Mm9W77OzuxU6vvkSBBHKMecZvaS_hPHC1rM0yEoH8uh0qyQ0BIDzeG3ssmGoIGGABz-NRWJVjonay9ZZi2Ahv0OsV7080ASBVWHXC-sjVuvysORuzXvHSThf-boKnZJs2v_XQ2UV4mA4iBNP0BDdWFLuXyWI_nZwFLmeEuin7ZJK4ABwDxRUmaicCRb6XU-XH8puBuOLJAL66jb4TlJkGBYZfP2M5Edn8stL9AmIpPntA9UPacw5n5mSAKkeVmmjc66kefshmpqFrO887H74LEJzLJ3O-3MJK-BP3NZL0BnsdlC8bqHW4WCMpK32BtBl0MuxA5l3hgWABB7w6pIMDrM29vdewIjl4W6cuuuTxKXMJfq-VEBImnHUIi8zs46bFStmWdJoQpedBKTDOpxVfgo0w2zJM8IVshCj2Ttc80Mc4Dynmu8gvvNfDpT62HzH225TFLp9sKtqo0GUcMa5j7dJi9Comifssgr4xGWQ_sEFYA-GjXKkBinqG7b4MIK8LmWej2UHr9y3jeLpKG--Z87h4xlo_UwJt9cztQjTsAsH1FFbsuBbzjygEbw_tqAr7BGKiOPhlxc4oypSIoTJeuTz7iJISEtYi7RYh0Xg1fO416dq9sBh3jDiNTbPRP83uVnk6DkZplZLtPSsr6qa8j59cpK_1pJ_TqRrFg0YHhpG_bdRzacZaQYLqD7dWll9K2LAvuhfsZ_gQgUx_jPYTosahgMXfqYZ7T_HNRVUIETgaVy7URe6C7t9PZIDyKN4gYY7lOBcjSWhrlj9dGdzoEnwlCsTgWxYVKhZiNvbaIZT6oMhr6i0x8DMVBZvKJe8qDCBOY55LoVSLrXzBN8-vKKqyFdptjJ-iVFDdQRYpEAEHiWiaPqmBF7S7d_6I6VeraaHDWPHWDkxuasEvrYZJ4kixqQWRfUsp9o6DUP3ZqJXGpZNR8LMGuaTaoBXuwvbSz_GnB1tO3AzLd7YX8BhvQXM6mgjt-rDd43Y2KeWdwHlKCF7ysrMzs3Vh2dOZCmxQzei2sY7SUFLGkw49tlMNtjiEnJ91kY4ptp4kUjcL-tJEnD_Eqx01jWxmRctzKGrsZYxeBmSvFI0jznf2VZwa4_hiPo5hHEsf7oVICoHs1tN4e-JolXfO8SNR8m6J7iwS-JzSQTvvwc6fUGNmoCZISXfjl4M-zKk2V4aQ4cli7Br92VBn1X4jchC4DGzC_Qz6et2QbBMg.1ctswx_rsVc7Rwyz-lVbWQ"
         *   }
         * }
         */

        Log.d("TAG", "MAP: $map")
        val authentication: Map<Any, Any> = map["data"] as Map<Any, Any>
        val accessToken: String = authentication["access_token"] as String
        val refreshToken: String = authentication["refresh_token"] as String

        return Response(data = LoginResponse(accessToken, refreshToken))
    }

    override suspend fun forgot(email: String): Response<ForgotResponse> {
        val map = authService.forgot(
            mapOf(
                "email" to email,
            ),
        )
        val authentication: String = map["status"].toString()

        return Response(data = ForgotResponse(authentication))
    }

    override suspend fun logout(token: String): LogoutResponse? {
        return authService.logout(
            mapOf(
                "access_token" to token,
            ),
        ).data
    }

    override suspend fun signup(
        email: String,
        password: String,
        name: String,
        signUpCode: String?,
    ): Response<SignUpResponse> {
        val map = authService.signup(
            mapOf(
                "email" to email,
                "password" to password,
                "name" to name,
                "special_code" to signUpCode.orEmpty(),
            ),
        )

        val authentication: String = map["status"].toString()

        return Response(data = SignUpResponse(authentication))
    }

    override suspend fun verify(
        email: String,
        comfirmation_code: String,
    ): Response<VerifyResponse> {
        val map = authService.verify(
            mapOf(
                "email" to email,
                "confirmation_code" to comfirmation_code,
            ),
        )
        val authentication: String = map["status"].toString()
        return Response(data = VerifyResponse(authentication))
    }

    override suspend fun refreshToken(refreshToken: String): RefreshTokenResponse {
        return authService.refreshToken(
            mapOf(
                "refresh_token" to refreshToken,
            ),
        ).data ?: throw Exception("Unknown error")
    }

    override suspend fun reset(
        email: String,
        confirmation_code: String,
        new_password: String,
    ): Response<ResetResponse> {
        val map = authService.reset(
            mapOf(
                "email" to email,
                "confirmation_code" to confirmation_code,
                "new_password" to new_password,
            ),
        )
        val authentication: String = map["status"].toString()
        return Response(data = ResetResponse(authentication))
    }

    override suspend fun resent(email: String): Any? {
        return authService.resent(mapOf("email" to email)).data
    }
}
