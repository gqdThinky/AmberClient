package com.amberclient.modules.render;

import com.amberclient.utils.module.Module;

public class NoHurtCam extends Module {
    private static NoHurtCam instance;

    public NoHurtCam() {
        super("NoHurtCam", "X", "Render");
        instance = this;
    }

    public static NoHurtCam getInstance() {
        return instance;
    }
}