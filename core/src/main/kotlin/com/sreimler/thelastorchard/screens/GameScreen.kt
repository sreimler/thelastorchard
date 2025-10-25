package com.sreimler.thelastorchard.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.sreimler.thelastorchard.assets.GameAssets
import com.sreimler.thelastorchard.core.Position
import com.sreimler.thelastorchard.input.KeyboardInput
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

    // Collision rectangles
    private val collisionRects = mutableListOf<Rectangle>()

    // Draw objects for debugging, e.g. collision boxes
    private val debugShapeRenderer = ShapeRenderer()

    override fun show() {
        mapRenderer = OrthogonalTiledMapRenderer(assets.farmMap)

        // Calculate initial viewport dimensions
        updateViewportDimensions()

        // Load static collision objects
        loadCollisionRects()

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
            val newPosition = Position(
                characterPosition.x + movement.x * MOVE_SPEED * dt,
                characterPosition.y + movement.y * MOVE_SPEED * dt
            )

            if (!wouldCollide(newPosition)) {
                // Set position, ensuring map boundaries
                characterPosition = newPosition.apply { clampToMapBounds() }

                // Calculate corresponding camera position
                val cameraPosition = getCenteredCameraFor(characterPosition)
                camera.position.set(cameraPosition.x, cameraPosition.y, 0f)
            }
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

        // For debugging only
        drawCollisionBoxes()
    }

    /**
     * Ensures that the character does not move outside of the map boundaries.
     */
    private fun Position.clampToMapBounds() {
        x = x.coerceIn(characterWidth / 2f, maxCharacterPosition.x)
        y = y.coerceIn(characterHeight / 2f, maxCharacterPosition.y)
    }

    /**
     * Load all collision rectangles from the corresponding map layer and store in [collisionRects].
     */
    private fun loadCollisionRects() {
        val collisionLayer = assets.farmMap.layers.get("Collision") as MapLayer

        for (obj in collisionLayer.objects) {
            if (obj is RectangleMapObject) {
                val rect = obj.rectangle
                collisionRects.add(rect)
                log.debug { "Found collision rect with dimensions ${rect.width}x${rect.height} and center ${rect.x}|${rect.y}" }
            }
        }

        log.info { "Loaded ${collisionRects.size} collision rectangles." }
    }

    private fun wouldCollide(position: Position): Boolean {
        // Calculate character collision box
        val characterRect = Rectangle(
            position.x - COLLISION_WIDTH / 2 + COLLISION_OFFSET_X,
            position.y - COLLISION_HEIGHT / 2 + COLLISION_OFFSET_Y,
            COLLISION_WIDTH,
            COLLISION_HEIGHT
        )

        for (rect in collisionRects) {
            if (characterRect.x < rect.x + rect.width &&
                characterRect.x + characterRect.width > rect.x &&
                characterRect.y < rect.y + rect.height &&
                characterRect.y + characterRect.height > rect.y
            ) return true
        }

        return false
    }

    private fun drawCollisionBoxes() {
        debugShapeRenderer.projectionMatrix = camera.combined
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        // Draw red map collision rects
        debugShapeRenderer.color = Color.RED
        for (rect in collisionRects) {
            debugShapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
        }

        // Draw character collision box in green
        val characterRect = Rectangle(
            characterPosition.x - COLLISION_WIDTH / 2 + COLLISION_OFFSET_X,
            characterPosition.y - COLLISION_HEIGHT / 2 + COLLISION_OFFSET_Y,
            COLLISION_WIDTH,
            COLLISION_HEIGHT
        )

        debugShapeRenderer.color = Color.GREEN
        debugShapeRenderer.rect(characterRect.x, characterRect.y, characterRect.width, characterRect.height)

        debugShapeRenderer.end()
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
        debugShapeRenderer.dispose()
        log.debug { "Resources disposed" }
    }

    companion object {
        private const val MOVE_SPEED = 200f // pixels per second
        private const val TILE_SIZE = 16f

        // Character collision box dimensions (smaller than sprite)
        private const val COLLISION_WIDTH = 12f
        private const val COLLISION_HEIGHT = 8f
        private const val COLLISION_OFFSET_X = 0.5f
        private const val COLLISION_OFFSET_Y = -6f

    }
}


fun Boolean.toFloat(): Float = if (this) 1f else 0f

