package com.amberclient.modules.render

import com.amberclient.utils.module.Module

class NoHurtCam : Module("NoHurtCam", "Disables the camera shake effect", "Render") {

    init {
        instance = this
    }

    companion object {
        @JvmStatic
        private var instance: NoHurtCam? = null

        @JvmStatic
        fun getInstance(): NoHurtCam? {
            return instance
        }
    }
}