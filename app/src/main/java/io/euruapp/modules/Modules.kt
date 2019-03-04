package io.euruapp.modules

import io.euruapp.core.EuruApplication
import io.euruapp.room.RoomAppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {
    single { RoomAppDatabase.getInstance(androidContext() as EuruApplication) }
}