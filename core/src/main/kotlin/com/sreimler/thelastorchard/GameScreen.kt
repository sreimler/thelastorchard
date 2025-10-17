package com.sreimler.thelastorchard

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.graphics.use
import ktx.log.logger


class GameScreen : KtxScreen {
    private val characterTexture =
        Texture("Character and Portrait/Character/Pre-made/Tori/idle.png".toInternalFile(), true).apply {
            setFilter(Nearest, Nearest)
        }
    val characterChillFrontRegion = TextureRegion(characterTexture, 0, 0, 32, 32)
    private val batch = SpriteBatch()

    // Fixed timestep: 60 updates per second
    private val fixedDeltaTime = 1f / 60f
    private var accumulator = 0f

    private val camera = OrthographicCamera()
    private val fitViewport = FitViewport(640f, 360f, camera).also {
        camera.position.set(320f, 180f, 0f)
    }

    private val log = logger<GameScreen>()

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

        fitViewport.apply()
        camera.update()
        batch.projectionMatrix = camera.combined

        val characterX = fitViewport.worldWidth / 2f
        val characterY = fitViewport.worldHeight / 2f

        batch.use {
            it.draw(characterChillFrontRegion, characterX, characterY)
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        fitViewport.update(width, height)
    }

    override fun dispose() {
        characterTexture.disposeSafely()
        batch.disposeSafely()
        log.debug { "Resources disposed" }
    }
}
