package com.ai.app.move.deskercise.di

import com.ai.app.move.deskercise.BuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ai.app.move.deskercise.app.AppStorage
import com.ai.app.move.deskercise.app.DeviceManager
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.fcm.FcmHelper
import com.ai.app.move.deskercise.network.NetworkException
import com.ai.app.move.deskercise.network.RefreshTokenException
import com.ai.app.move.deskercise.network.ServerException
import com.ai.app.move.deskercise.network.repositories.AuthRepository
import com.ai.app.move.deskercise.network.repositories.AuthRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.CompanyRepository
import com.ai.app.move.deskercise.network.repositories.CompanyRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.ExerciseRepository
import com.ai.app.move.deskercise.network.repositories.ExerciseRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.FileRepository
import com.ai.app.move.deskercise.network.repositories.FileRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.LeaderBoardRepository
import com.ai.app.move.deskercise.network.repositories.LeaderBoardRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.RewardRepository
import com.ai.app.move.deskercise.network.repositories.RewardRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.TeamRepository
import com.ai.app.move.deskercise.network.repositories.TeamRepositoryImpl
import com.ai.app.move.deskercise.network.repositories.UserRepository
import com.ai.app.move.deskercise.network.repositories.UserRepositoryImpl
import com.ai.app.move.deskercise.network.services.AuthService
import com.ai.app.move.deskercise.network.services.CompanyService
import com.ai.app.move.deskercise.network.services.ExerciseService
import com.ai.app.move.deskercise.network.services.FileService
import com.ai.app.move.deskercise.network.services.LeaderBoardService
import com.ai.app.move.deskercise.network.services.RewardService
import com.ai.app.move.deskercise.network.services.TeamService
import com.ai.app.move.deskercise.network.services.UserService
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.StartProgramViewModel
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.active.MyActiveRewardViewModel
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.past.MyPastRewardViewModel
import com.ai.app.move.deskercise.ui.home.HomeViewModel
import com.ai.app.move.deskercise.ui.leaderboard.LeaderBoardViewModel
import com.ai.app.move.deskercise.ui.login.ForgetViewModel
import com.ai.app.move.deskercise.ui.login.LoginViewModel
import com.ai.app.move.deskercise.ui.login.ResetViewModel
import com.ai.app.move.deskercise.ui.login.RegisterViewModel
import com.ai.app.move.deskercise.ui.login.VerifyViewModel
import com.ai.app.move.deskercise.ui.myPoints.MyPointsViewModel
import com.ai.app.move.deskercise.ui.profile.EditProfileViewModel
import com.ai.app.move.deskercise.ui.profile.MyProfileViewModel
import com.ai.app.move.deskercise.ui.rewards.RewardListViewModel
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailViewModel
import com.ai.app.move.deskercise.ui.splash.SplashViewModel
import com.ai.app.move.deskercise.usecases.DeleteFcmTokenToServerUseCase
import com.ai.app.move.deskercise.usecases.ForgetUseCase
import com.ai.app.move.deskercise.usecases.LoginUseCase
import com.ai.app.move.deskercise.usecases.LogoutUseCase
import com.ai.app.move.deskercise.usecases.RenewAccessTokenUseCase
import com.ai.app.move.deskercise.usecases.ResentOptCodeUseCase
import com.ai.app.move.deskercise.usecases.ResetUseCase
import com.ai.app.move.deskercise.usecases.SignUpUseCase
import com.ai.app.move.deskercise.usecases.UpdateFcmTokenToServerUseCase
import com.ai.app.move.deskercise.usecases.VerifyUseCase
import com.ai.app.move.deskercise.usecases.exercise.FetchScoreConditionUseCase
import com.ai.app.move.deskercise.usecases.exercise.SendExerciseResultUseCase
import com.ai.app.move.deskercise.usecases.exercise.StartExerciseProgramUseCase
import com.ai.app.move.deskercise.usecases.leaderboard.GetPersonalBoardUseCase
import com.ai.app.move.deskercise.usecases.leaderboard.GetProfileBoardUseCase
import com.ai.app.move.deskercise.usecases.leaderboard.GetTeamBoardUseCase
import com.ai.app.move.deskercise.usecases.listItems.FetchCompaniesUseCase
import com.ai.app.move.deskercise.usecases.listItems.FetchTeamUseCase
import com.ai.app.move.deskercise.usecases.reward.FetchAvailableRewardListUseCase
import com.ai.app.move.deskercise.usecases.reward.FetchRedeemRewardListUseCase
import com.ai.app.move.deskercise.usecases.reward.FetchRewardListUseCase
import com.ai.app.move.deskercise.usecases.reward.GetRewardDetailUseCase
import com.ai.app.move.deskercise.usecases.reward.RedeemRewardUseCase
import com.ai.app.move.deskercise.usecases.reward.RedemptionsRewardUseCase
import com.ai.app.move.deskercise.usecases.team.FetchTeamUsersUseCase
import com.ai.app.move.deskercise.usecases.user.FetchUserScoreHistoriesUseCase
import com.ai.app.move.deskercise.usecases.user.GetUserProfileUseCase
import com.ai.app.move.deskercise.usecases.user.RequestDeleteAccountUseCase
import com.ai.app.move.deskercise.usecases.user.UpdateUserAvatarUseCase
import com.ai.app.move.deskercise.usecases.user.UpdateUserProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@ExperimentalCoroutinesApi
val appModule = module {
    single { UserManager(get(), get()) }
    single { AppStorage(get()) }
    single { DeviceManager(get()) }
    single { FcmHelper() }
}
val viewModelModule = module {
    viewModel { SplashViewModel(get(), get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { VerifyViewModel(get(), get()) }
    viewModel { ResetViewModel(get()) }
    viewModel { ForgetViewModel(get()) }
    viewModel { StartProgramViewModel(get(), get(), get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MyProfileViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { LeaderBoardViewModel(get(), get(), get()) }
    viewModel { RewardListViewModel(get(), get(), get()) }
    viewModel { MyPointsViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { MyActiveRewardViewModel(get()) }
    viewModel { MyPastRewardViewModel(get()) }
    viewModel { RewardDetailViewModel(get(), get()) }
}
val useCaseModule = module {
    factory { LoginUseCase(get(), get(), get()) }
    factory { RenewAccessTokenUseCase(get(), get()) }
    factory { LogoutUseCase(get(), get()) }
    factory { ForgetUseCase(get()) }
    factory { ResetUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { VerifyUseCase(get()) }
    factory { ResentOptCodeUseCase(get()) }
    factory { SendExerciseResultUseCase(get(), get(), get(), get()) }
    factory { StartExerciseProgramUseCase(get(), get(), get(), get()) }
    factory { GetUserProfileUseCase(get(), get()) }
    factory { GetPersonalBoardUseCase(get()) }
    factory { GetTeamBoardUseCase(get()) }
    factory { UpdateUserProfileUseCase(get(), get(), get()) }
    factory { FetchScoreConditionUseCase(get(), get()) }
    factory { FetchUserScoreHistoriesUseCase(get()) }
    factory { FetchCompaniesUseCase(get()) }
    factory { FetchTeamUseCase(get()) }
    factory { FetchTeamUsersUseCase(get(), get()) }
    factory { UpdateUserAvatarUseCase(get(), get(), get()) }
    factory { FetchRewardListUseCase(get()) }
    factory { FetchAvailableRewardListUseCase(get()) }
    factory { FetchRedeemRewardListUseCase(get()) }
    factory { RedeemRewardUseCase(get(), get(), get()) }
    factory { RedemptionsRewardUseCase(get(), get(), get()) }
    factory { RequestDeleteAccountUseCase(get(), get()) }
    factory { UpdateFcmTokenToServerUseCase(get(), get(), get()) }
    factory { DeleteFcmTokenToServerUseCase(get(), get(), get()) }
    factory { GetRewardDetailUseCase(get()) }
    factory { GetProfileBoardUseCase(get()) }
}

val repositoryModule = module {

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<ExerciseRepository> { ExerciseRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<LeaderBoardRepository> { LeaderBoardRepositoryImpl(get()) }
    single<CompanyRepository> { CompanyRepositoryImpl(get()) }
    single<TeamRepository> { TeamRepositoryImpl(get()) }
    single<FileRepository> { FileRepositoryImpl(get()) }
    single<RewardRepository> { RewardRepositoryImpl(get()) }
}

val networkModule = module {
    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setLenient()
            .create()
    }

    fun provideHttpClient(userManager: UserManager, gson: Gson): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
            //if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        httpClient.interceptors().add(interceptor)
        httpClient.interceptors().add(
            Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer ${userManager.getAccessToken()}")
                    .build()
                val response = chain.proceed(request)
                val bodyString = response.peekBody(2048).string()
                Timber.d(">> accessToken=${userManager.getAccessToken()}")
                when (response.code) {
                    // Successful response
                    in 200..299 -> return@Interceptor response

                    400, 403 -> {
                        val obj = JSONObject(bodyString)
                        throw ServerException(obj["message"] as String)
                    }

                    401 -> {
                        val isRefreshRequest = request.url.toString().endsWith("auth/refresh-token")
                        if (isRefreshRequest) {
                            throw RefreshTokenException("Refresh token process failed!")
                        }
                        val refreshToken = userManager.getRefreshToken()
                        val refreshRequest = request.newBuilder()
                            //.url("${BuildConfig.API_URL}v1/auth/refresh-token")
                            .post(
                                mapOf(
                                    "refresh_token" to refreshToken,
                                ).toString().toRequestBody(),
                            ).build()

                        val refreshResponse = chain.proceed(refreshRequest)
                        val body = response.peekBody(2048).string()
                        if (refreshResponse.code != 200) {
                            throw RefreshTokenException("Refresh token process failed!")
                        }
                        val obj = JSONObject(body)
                        val newAccessToken = obj["access_token"] as String
                        userManager.storeAccessToken(newAccessToken)
                        return@Interceptor chain.proceed(
                            request.newBuilder()
                                .header(
                                    "Authorization",
                                    "Bearer ${userManager.getAccessToken()}",
                                ).build(),
                        )
                    }

                    // All other responses (e.g. client or server errors) will trigger an exception.
                    else -> throw NetworkException(
                        "${response.code}: An unexpected error occurred.",
                    )
                }
            },
        )
        return httpClient.build()
    }

    fun provideRetrofit(factory: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create(factory))
            .client(client)
            .build()
    }

    single { provideGson() }
    single { provideHttpClient(get(), get()) }
    single { provideRetrofit(get(), get()) }

    fun provideAuthApi(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    fun provideExerciseApi(retrofit: Retrofit): ExerciseService {
        return retrofit.create(ExerciseService::class.java)
    }

    fun provideUserApi(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    fun provideLeaderBoardApi(retrofit: Retrofit): LeaderBoardService {
        return retrofit.create(LeaderBoardService::class.java)
    }

    fun provideCompanyApi(retrofit: Retrofit): CompanyService {
        return retrofit.create(CompanyService::class.java)
    }

    fun provideTeamApi(retrofit: Retrofit): TeamService {
        return retrofit.create(TeamService::class.java)
    }

    fun provideFileApi(retrofit: Retrofit): FileService {
        return retrofit.create(FileService::class.java)
    }

    fun provideRewardApi(retrofit: Retrofit): RewardService {
        return retrofit.create(RewardService::class.java)
    }

    single { provideAuthApi(get()) }
    single { provideExerciseApi(get()) }
    single { provideUserApi(get()) }
    single { provideLeaderBoardApi(get()) }
    single { provideCompanyApi(get()) }
    single { provideTeamApi(get()) }
    single { provideFileApi(get()) }
    single { provideRewardApi(get()) }
}
