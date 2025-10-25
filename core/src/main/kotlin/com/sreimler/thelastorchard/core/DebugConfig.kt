package com.sreimler.thelastorchard.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

object DebugConfig {
    // Get build-time config from Gradle
    private val isDebugBuild = System.getProperty("debug", "false").toBoolean()

    // Runtime toggles
    var drawCollisionBoxes = isDebugBuild // Default to build setting
    // Add more flags as needed

    fun processInput() {
        // Debug settings
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            drawCollisionBoxes = !drawCollisionBoxes
        }
    }
}
