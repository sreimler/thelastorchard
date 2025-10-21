package com.sreimler.thelastorchard

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
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
    private val viewport = FitViewport(640f, 360f, camera)

    private var halfViewportWidth = 0f
    private var halfViewportHeight = 0f

    // Calculate map size from tmx properties
    private val mapProperties = assets.farmMap.properties
    private val mapWidth = mapProperties.get("width", Int::class.java) * TILE_SIZE
    private val mapHeight = mapProperties.get("height", Int::class.java) * TILE_SIZE

    private val characterWidth = assets.charToriIdleFront.regionWidth.toFloat()
    private val characterHeight = assets.charToriIdleFront.regionHeight.toFloat()

    // Character position
    private var characterPosition = Position(mapWidth / 2f, mapHeight / 2f) // Initially set to the center of the map

    private val maxCharacterPosition = Position(
        (mapWidth - characterWidth / 2).coerceAtLeast(0f),
        (mapHeight - characterHeight / 2).coerceAtLeast(0f) // // On the top of the map we'll leave characterHeight/2 more space
    )

    override fun show() {
        mapRenderer = OrthogonalTiledMapRenderer(assets.farmMap)

        // Calculate initial viewport dimensions
        updateViewportDimensions()

        // Center camera on character (which is initally centered on the map)
        val cameraPos = getCenteredCameraFor(characterPosition)
        camera.position.set(cameraPos.x, cameraPos.y, 0f)
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
        val input = KeyboardInput(
            Gdx.input.isKeyPressed(Keys.W),
            Gdx.input.isKeyPressed(Keys.A),
            Gdx.input.isKeyPressed(Keys.S),
            Gdx.input.isKeyPressed(Keys.D)
        )

        // Calculate horizontal and vertical movement
        val movement = Vector2(
            input.d.toFloat() - input.a.toFloat(), // -1 to 1
            input.w.toFloat() - input.s.toFloat() // -1 to 1
        )

        if (!movement.isZero) {
            movement.nor() // Normalize for diagonal movement

            // Calculate position after movement
            characterPosition.x += movement.x * MOVE_SPEED * dt
            characterPosition.y += movement.y * MOVE_SPEED * dt

            // Ensure map boundaries
            characterPosition.clampToMapBounds()

            // Calculate corresponding camera position
            val cameraPosition = getCenteredCameraFor(characterPosition)
            camera.position.set(cameraPosition.x, cameraPosition.y, 0f)
//        log.info { "${characterWidth} ${characterHeight}x char: $characterX, map: $mapWidth, vp.screen: ${viewport.screenWidth}, vp.world: ${viewport.worldWidth}" }
        }
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
        batch.use {
            // Draw character in the center of its position
            it.draw(
                assets.charToriIdleFront,
                characterPosition.x - characterWidth / 2f,
                characterPosition.y - characterHeight / 2f
            )
        }
    }

    /**
     * Ensures that the character does not move outside of the map boundaries.
     */
    private fun Position.clampToMapBounds() {
        x = x.coerceIn(characterWidth / 2f, maxCharacterPosition.x)
        y = y.coerceIn(characterHeight / 2f, maxCharacterPosition.y)
    }

    /**
     * Calculates the camera position relative to a provided [Position], usually that of the character.
     *
     * When the camera reaches the end of the map in one direction, it starts moving further in that direction.
     * This ensures that the full view is always covered by the map and not by darkness surrounding it.
     */
    private fun getCenteredCameraFor(position: Position): Position {
        return Position(
            if (mapWidth <= viewport.worldWidth) mapWidth / 2f else position.x.coerceIn(
                halfViewportWidth,
                mapWidth - halfViewportWidth
            ),
            if (mapHeight <= viewport.worldHeight) mapHeight / 2f else position.y.coerceIn(
                halfViewportHeight,
                mapHeight - halfViewportHeight
            )
        )
    }

    private fun updateViewportDimensions() {
        halfViewportWidth = viewport.worldWidth / 2
        halfViewportHeight = viewport.worldHeight / 2
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        updateViewportDimensions()
    }

    override fun dispose() {
        mapRenderer.dispose()
        batch.disposeSafely()
        log.debug { "Resources disposed" }
    }

    companion object {
        private const val MOVE_SPEED = 400f // pixels per second
        private const val TILE_SIZE = 16f
    }
}

data class KeyboardInput(
    var w: Boolean,
    var a: Boolean,
    var s: Boolean,
    var d: Boolean
)

fun Boolean.toFloat(): Float = if (this) 1f else 0f

data class Position(
    var x: Float,
    var y: Float
)
