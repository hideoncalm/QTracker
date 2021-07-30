package com.quyenln.runtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.quyenln.runtracker.data.db.RunDatabase
import com.quyenln.runtracker.utils.Constants.DB_NAME
import com.quyenln.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.quyenln.runtracker.utils.Constants.KEY_NAME
import com.quyenln.runtracker.utils.Constants.KEY_WEIGHT
import com.quyenln.runtracker.utils.Constants.SHARED_PREFERENCE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, RunDatabase::class.java, DB_NAME)
            .build()


    @Singleton
    @Provides
    fun provideRunDao(db: RunDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 50f)

    @Singleton
    @Provides
    fun provideIsFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

}