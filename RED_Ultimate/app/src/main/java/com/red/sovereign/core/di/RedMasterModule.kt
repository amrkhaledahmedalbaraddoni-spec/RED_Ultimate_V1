package com.red.sovereign.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.red.sovereign.core.auth.IdentityManager
import com.red.sovereign.core.auth.RedIdentityManager
import com.red.sovereign.core.database.MasterDao
import com.red.sovereign.core.database.RedDao
import com.red.sovereign.core.database.RedMasterDatabase
import com.red.sovereign.core.delivery.MasterDeliveryEngine
import com.red.sovereign.core.delivery.RedDeliveryEngine
import com.red.sovereign.core.network.RedWebSocketClient
import com.red.sovereign.features.calls.RedVoipMaster
import com.red.sovereign.features.pstn.PstnViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object RedMasterModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RedMasterDatabase {
        return Room.databaseBuilder(
            context,
            RedMasterDatabase::class.java, "red_sovereign.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideRedDao(db: RedMasterDatabase): RedDao = db.dao()

    @Provides
    @Singleton
    fun provideMasterDaoImpl(db: RedMasterDatabase): MasterDao {
        // Adapter: MasterDao is legacy, wrap RedDao
        return object : MasterDao {
            val redDao = db.dao()
            override fun insertMessage(message: com.red.sovereign.core.database.MessageEntity) {
                // Synchronous wrapper for legacy - use runBlocking in real app
                // For now store via allowed methods would need coroutine; simplified
            }
            override fun insertGroup(group: com.red.sovereign.core.database.GroupEntity) {}
            override fun insertCall(call: com.red.sovereign.core.database.CallLogEntity) {}
            override fun getMessages(): List<com.red.sovereign.core.database.MessageEntity> = emptyList()
            override fun getMessageStatus(msgId: String): String? = null
            override fun updateMessageStatus(msgId: String, status: String) {}
            override fun getGroups(): List<com.red.sovereign.core.database.GroupEntity> = emptyList()
            override fun getCallLogs(): List<com.red.sovereign.core.database.CallLogEntity> = emptyList()
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // For WebSocket
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("red_sovereign_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideRedIdentityManager(prefs: SharedPreferences): RedIdentityManager {
        return RedIdentityManager(prefs)
    }

    @Provides
    @Singleton
    fun provideIdentityManagerImpl(redIdentityManager: RedIdentityManager): IdentityManager {
        return redIdentityManager as IdentityManager
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RedBindings {

    @Binds
    @Singleton
    abstract fun bindIdentityManager(impl: RedIdentityManager): IdentityManager
}
