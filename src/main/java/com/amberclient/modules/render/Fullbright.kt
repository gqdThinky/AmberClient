package com.amberclient.modules.render

import com.amberclient.utils.module.Module

class Fullbright : Module("Fullbright", "Maximizes brightness", "Render") {
    companion object {
        const val FULLBRIGHT_GAMMA = 10.0
    }

    private var originalGamma = 1.0

    override fun onEnable() {
        client.options?.let { options ->
            originalGamma = options.gamma.value
            options.gamma.setValue(FULLBRIGHT_GAMMA)
        }
    }

    override fun onDisable() {
        client.options?.gamma?.setValue(originalGamma)
    }
}