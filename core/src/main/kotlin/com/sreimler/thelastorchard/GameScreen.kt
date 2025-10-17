package com.sreimler.thelastorchard

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.sreimler.thelastorchard.assets.GameAssets
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.log.logger


class GameScreen(private val assets: GameAssets) : KtxScreen {
    private val log = logger<GameScreen>()

    private val batch = SpriteBatch()

    // Map renderer
    private lateinit var mapRenderer: OrthogonalTiledMapRenderer

    // Fixed timestep: 60 updates per second
    private val fixedDeltaTime = 1f / 60f
    private var accumulator = 0f

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(640f, 360f, camera).also {
        camera.position.set(320f, 180f, 0f)
    }

    override fun show() {
        mapRenderer = OrthogonalTiledMapRenderer(assets.farmMap)
        log.info { "GameScreen shown" }
    }

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
        viewport.apply()
        camera.update()

        // Render background first
        mapRenderer.setView(camera)
        mapRenderer.render()


        batch.projectionMatrix = camera.combined

        val characterX = viewport.worldWidth / 2f
        val characterY = viewport.worldHeight / 2f

        batch.use {
            it.draw(assets.charToriIdleFront, characterX, characterY)
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        viewport.update(width, height)
    }

    override fun dispose() {
        mapRenderer.dispose()
        batch.disposeSafely()
        log.debug { "Resources disposed" }
    }
}
