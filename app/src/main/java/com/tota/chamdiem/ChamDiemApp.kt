package com.tota.chamdiem

import android.app.Application
import com.tota.chamdiem.data.AppDatabase
import com.tota.chamdiem.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChamDiemApp : Application() {

    val repository: Repository by lazy { Repository(AppDatabase.get(this)) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.ensureSeed() }
    }
}
