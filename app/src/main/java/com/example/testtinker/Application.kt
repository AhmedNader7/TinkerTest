package com.example.testtinker

import android.app.Application
import android.content.Context
import android.content.Intent
import com.tencent.tinker.anno.DefaultLifeCycle
import com.tencent.tinker.entry.DefaultApplicationLike
import com.tencent.tinker.lib.tinker.TinkerInstaller
import com.tencent.tinker.loader.shareutil.ShareConstants

@DefaultLifeCycle(
    application = "com.example.texttinker.SampleApplication",             //application name to generate
    flags = ShareConstants.TINKER_ENABLE_ALL
)                                //tinkerFlags above

open class TinkerApplication(
    application: Application?,
    tinkerFlags: Int,
    tinkerLoadVerifyFlag: Boolean,
    applicationStartElapsedTime: Long,
    applicationStartMillisTime: Long,
    tinkerResultIntent: Intent?
) : DefaultApplicationLike(
    application,
    tinkerFlags,
    tinkerLoadVerifyFlag,
    applicationStartElapsedTime,
    applicationStartMillisTime,
    tinkerResultIntent
) {

    override fun onBaseContextAttached(base: Context?) {
        super.onBaseContextAttached(base)
        TinkerInstaller.install(this)
    }

    fun registerActivityLifecycleCallbacks(callback: Application.ActivityLifecycleCallbacks) {
        application.registerActivityLifecycleCallbacks(callback)
    }
}
