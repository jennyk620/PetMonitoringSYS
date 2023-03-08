package com.example.hpilitev3.di

import android.app.Application
import androidx.room.Room
import com.example.hpilitev3.BuildConfig
import com.example.hpilitev3.data.data_source.SensorDataSource
import com.example.hpilitev3.data.db.SensorDatabase
import com.example.hpilitev3.data.remote.SensorApi
import com.example.hpilitev3.data.repository.SensorRepositoryImpl
import com.example.hpilitev3.domain.repository.SensorRepository
import com.example.hpilitev3.domain.use_case.SensorUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDataBase(app: Application): SensorDatabase {
        return Room.databaseBuilder(app, SensorDatabase::class.java, SensorDatabase.DATABASE_NAME).build()
    }

    @Singleton
    @Provides
    fun provideSensorRepository(db: SensorDatabase, dataSource: SensorDataSource): SensorRepository {
        return SensorRepositoryImpl(db, dataSource)
    }

    @Provides
    @Singleton
    fun provideSensorService(retrofit: Retrofit): SensorApi {
        return retrofit.create(SensorApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSensorDataSource(service: SensorApi): SensorDataSource {
        return SensorDataSource(service)
    }

    @Singleton
    @Provides
    fun provideSensorUseCase(repo: SensorRepository): SensorUseCase {
        return SensorUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://limtanis.duckdns.org:16041/")
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(999, TimeUnit.SECONDS)
            .readTimeout(999, TimeUnit.SECONDS)
            .addInterceptor(provideOkHttpLogging())
                .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

//    @Provides
//    @Singleton
//    fun connectionClose(): OkHttpClient {
//        return OkHttpClient.Builder().dispatcher()
//    }

}