package com.hgwxr.pluginsamples

import android.app.Activity
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle

class AppInstrumentation(
    var realContext: Context,
    var base: Instrumentation,
    var pluginContext: PluginContext
) :
    Instrumentation() {
    private val KEY_COMPONENT = "commontec_component"

    companion object {
        fun inject(activity: Activity, pluginContext: PluginContext) {
            var reflect = Reflect.on(activity)
            var activityThread = reflect.get<Any>("mMainThread")
            var base = Reflect.on(activityThread).get<Instrumentation>("mInstrumentation")
            var appInstrumentation = AppInstrumentation(activity, base, pluginContext)
            Reflect.on(activityThread).set("mInstrumentation", appInstrumentation)
            Reflect.on(activity).set("mInstrumentation", appInstrumentation)
        }
    }

    override fun newActivity(cl: ClassLoader, className: String, intent: Intent): Activity? {
        val componentName = intent.getParcelableExtra<ComponentName>(KEY_COMPONENT)
        val clazz = pluginContext.classLoader.loadClass(componentName!!.className)
        intent.component = componentName
        return clazz.newInstance() as Activity?
    }

    private fun injectActivity(activity: Activity?) {
        val intent = activity?.intent
        val base = activity?.baseContext
        try {
            Reflect.on(base).set("mResources", pluginContext.resources)
            Reflect.on(activity).set("mResources", pluginContext.resources)
            Reflect.on(activity).set("mBase", pluginContext)
            Reflect.on(activity).set("mApplication", pluginContext.applicationContext)
            // for native activity
            val componentName = intent!!.getParcelableExtra<ComponentName>(KEY_COMPONENT)
            val wrapperIntent = Intent(intent)
            wrapperIntent.setClassName(componentName!!.packageName, componentName.className)
            activity.intent = wrapperIntent

        } catch (e: Exception) {
        }
    }

    override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
        injectActivity(activity)
        super.callActivityOnCreate(activity, icicle)
    }

    override fun callActivityOnCreate(
        activity: Activity?,
        icicle: Bundle?,
        persistentState: PersistableBundle?
    ) {
        injectActivity(activity)
        super.callActivityOnCreate(activity, icicle, persistentState)
    }


}
