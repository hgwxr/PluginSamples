package com.hgwxr.pluginsamples

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.Method

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        val myViewObj = loadView()
        val myViewObj2 = loadView("com.hgwxr.plugin1.MyView2")
        val contentRoot = findViewById<LinearLayout>(R.id.contentRoot)
        if (myViewObj is View) {
            contentRoot?.addView(myViewObj, 200, 200)
            contentRoot?.addView(myViewObj2, 250, 250)
        }

    }

    fun loadView(className: String = "com.hgwxr.plugin1.MyView"): View? {
        val myViewClass = pluginClassLoader?.loadClass(className)
        myViewClass?.apply {
            val myViewObj = getConstructor(Context::class.java).newInstance(baseContext)
            return myViewObj as View
        }
        return null
    }

    private fun extractPlugin() {
        var inputStream = assets.open("plugin1.apk")
        File(filesDir.absolutePath, "plugin1.apk").writeBytes(inputStream.readBytes())
    }

    var pluginClassLoader: DexClassLoader? = null
    private fun init() {
        extractPlugin()
        val pluginPath = File(filesDir.absolutePath, "plugin1.apk").absolutePath
        val nativeLibDir = File(filesDir, "plugin1lib").absolutePath
        val dexOutPath = File(filesDir, "dexout").absolutePath
        // 生成 DexClassLoader 用来加载插件类
        pluginClassLoader = DexClassLoader(
            pluginPath,
            dexOutPath,
            nativeLibDir,
            this::class.java.classLoader
        )


//        try {
//            val assetManager = AssetManager::class.java.newInstance()
//            val method: Method =
//                assetManager.javaClass.getMethod("addAssetPath", String::class.java)
//            method.invoke(assetManager, pluginPath)
//            val resources = Resources(
//                assetManager,
//                resources.displayMetrics,
//                resources.configuration
//            )
//            Reflect.on(baseContext).set("mResources", resources)
//            Reflect.on(this).set("mResources", resources)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        val pluginContext = PluginContext(pluginPath, this, application, pluginClassLoader)
//        Reflect.on(baseContext).set("mResources", pluginContext.resources)
//        Reflect.on(this).set("mResources", pluginContext.resources)
//        Reflect.on(this).set("mBase", pluginContext)
//        Reflect.on(this).set("mApplication", pluginContext.applicationContext)
//        AppInstrumentation.inject(this, pluginContext)
        AppInstrumentation.inject(this, PluginContext(pluginPath, this, application, pluginClassLoader))

    }
}