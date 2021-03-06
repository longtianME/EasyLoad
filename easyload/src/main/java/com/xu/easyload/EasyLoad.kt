package com.xu.easyload

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import com.xu.easyload.service.ILoadService
import com.xu.easyload.service.LoadService
import com.xu.easyload.state.BaseState
import java.io.*
import java.util.*

/**
 * @author 言吾許
 */
class EasyLoad private constructor() {

    companion object {
        /**
         * 是否已经初始化
         */
        private var hasInit = false

        /**
         * 单例
         */
        private val instance: EasyLoad by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            EasyLoad()
        }

        /**
         * 单例
         */
        private val builder: Builder by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Builder()
        }


        /**
         * 初始化全局
         * 只能初始化一次
         */
        fun initGlobal(): GlobalBuilder {
            if (hasInit) {
                throw IllegalAccessException("Only allow to initialize once")
            }
            hasInit = true
            return GlobalBuilder()
        }

        /**
         * 初始化局部
         */
        fun initLocal(): LocalBuilder {
            return LocalBuilder()
        }
    }

    /**
     * 注入Activity
     * @param target 目标Activity
     */
    fun inject(target: Activity): ILoadService {
        return LoadService(target, resetLocalState())
    }

    /**
     * 注入View
     * @param target 目标View
     */
    fun inject(target: View): ILoadService {
        return LoadService(target, resetLocalState())
    }


    private fun resetLocalState(): Builder {
        val cloneBuilder = builder.copy()
        builder.localDefaultState = null
        builder.onReloadListener = null
        builder.onStateChangeListener = null
        builder.localStates.clear()
        builder.showDefault = true
        builder.delay = 100
        builder.specialSupport = false
        return cloneBuilder
    }

    /**
     * 局部builder
     */
    class LocalBuilder internal constructor() {
        /**
         *添加局部的state
         */
        fun addLocalState(state: BaseState) = apply {
            if (!builder.localStates.contains(state)) {
                builder.localStates.add(state)
            }
        }

        /**
         * 添加全局的默认的state
         */
        fun setLocalDefaultState(localDefault: Class<out BaseState>) = apply {
            builder.localDefaultState = localDefault
        }

        /**
         * 设置重新加载监听
         */
        fun setOnReloadListener(onReloadListener: ((iLoadService: ILoadService, clickState: BaseState, view: View?) -> Unit)) = apply {
            builder.onReloadListener = onReloadListener
        }

        /**
         * 设置重新加载监听
         */
        fun setOnStateChangeListener(onStateChangeListener: ((view: View?, currentState: BaseState) -> Unit)) = apply {
            builder.onStateChangeListener = onStateChangeListener
        }

        /**
         * 是否展示默认state
         */
        fun showDefault(showDefault: Boolean = true) = apply {
            builder.showDefault = showDefault
        }

        /**
         * 约束布局中有时候会出现bug，如若出现问题，开启此项
         * 只对约束布局起作用
         */
        fun specialSupport(support: Boolean, delay: Long = 100L) = apply {
            builder.delay = delay
            builder.specialSupport = support
        }

        /**
         * 注入 Activity
         */
        inline fun inject(target: Activity, func: LocalBuilder.() -> Unit): ILoadService = run {
            this.func()
            inject(target)
        }

        /**
         * 注入 Activity
         */
        fun inject(target: Activity): ILoadService {
            return instance.inject(target)
        }


        /**
         * 注入 View
         */
        inline fun inject(target: View, func: LocalBuilder.() -> Unit): ILoadService = run {
            this.func()
            inject(target)
        }

        /**
         * 注入 View
         */
        fun inject(target: View): ILoadService {
            return instance.inject(target)
        }
    }

    /**
     * 全局builder
     */
    class GlobalBuilder internal constructor() {
        /**
         * 初始化
         */
        fun init(func: GlobalBuilder.() -> Unit) = run {
            this.func()
        }

        /**
         * 添加局部状态
         */
        fun addGlobalState(state: BaseState) = apply {
            if (!builder.globalStates.contains(state)) {
                builder.globalStates.add(state)
            }
        }

        /**
         * 添加局部默认
         */
        fun setGlobalDefaultState(defaultState: Class<out BaseState>) = apply {
            builder.globalDefaultState = defaultState
        }

    }

    class Builder internal constructor() : Serializable {
        /**
         * 全局的status
         */
        internal val globalStates: MutableList<BaseState> = ArrayList()

        /**
         * 局部status
         */
        internal val localStates: MutableList<BaseState> = ArrayList()

        /**
         * 默认的全局status
         */
        internal var globalDefaultState: Class<out BaseState>? = null

        /**
         * 默认的局部status
         */
        internal var localDefaultState: Class<out BaseState>? = null

        /**
         * 重新加载监听
         */
        internal var onReloadListener: ((iLoadService: ILoadService, clickState: BaseState, view: View) -> Unit)? = null


        /**
         * 状态改变监听
         */
        internal var onStateChangeListener: ((view: View?, currentState: BaseState) -> Unit)? = null


        /**
         * 默认展示默认的state
         */
        internal var showDefault = true

        /**
         * 特殊布局支持
         */
        internal var specialSupport = false

        /**
         * 特殊布局加载延迟时间
         */
        internal var delay = 100L

        /**
         * 复制对象
         */
        internal fun copy(): Builder {
            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)
            oos.writeObject(this@Builder)
            val bis = ByteArrayInputStream(bos.toByteArray())
            val ois = ObjectInputStream(bis)
            return ois.readObject() as Builder
        }

    }


}