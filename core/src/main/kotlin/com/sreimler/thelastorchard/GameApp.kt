package com.sreimler.thelastorchard

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use

class GameApp : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(FirstScreen())
        setScreen<FirstScreen>()
        Gdx.app.log("GameApp", "Game initialized")
    }
}

class FirstScreen : KtxScreen {
    private val image = Texture("logo.png".toInternalFile(), true).apply { setFilter(Linear, Linear) }
    private val batch = SpriteBatch()

    // Fixed timestep: 60 updates per second
    private val fixedDeltaTime = 1f / 60f
    private var accumulator = 0f

    override fun render(delta: Float) {
        val deltaTime = Gdx.graphics.deltaTime.coerceAtMost(0.25f)
        accumulator += deltaTime

        // Fixed-step logic updates
        while (accumulator >= fixedDeltaTime) {
            update(fixedDeltaTime)
            accumulator -= fixedDeltaTime
        }

        // Render at full speed
        draw()
    }

    private fun update(dt: Float) {
        // Game logic goes here (input, movement, physics)
    }

    private fun draw() {
        // Clear screen to black
        clearScreen(red = 0f, green = 0f, blue = 0f, alpha = 1f)

        batch.use {
            it.draw(image, 100f, 160f)
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
        Gdx.app.log("GameApp", "Resources disposed")
    }
}
