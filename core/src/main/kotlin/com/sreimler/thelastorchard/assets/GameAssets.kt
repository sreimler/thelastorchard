package com.sreimler.thelastorchard.assets

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import ktx.assets.async.AssetStorage
import ktx.log.logger


class GameAssets {
    private val log = logger<GameAssets>()

    // KTX AssetStorage (coroutine-based, replaces AssetManager)
    private val assetStorage = AssetStorage()

    // Load assets lazily
    private lateinit var charToriIdleTexture: Texture

    // Define regions
    lateinit var charToriIdleFront: TextureRegion
        private set

    // Tiled Map (loaded directly, not via AssetStorage)
    lateinit var farmMap: TiledMap
        private set

    // Load all assets synchronously. For loading screens, use loadAsync() instead.
    suspend fun loadAll() {
        log.info { "Loading game assets.." }

        // Load textures (async but we'll await)
        charToriIdleTexture = assetStorage.loadAsync<Texture>(Assets.CHAR_TORI_IDLE).await().apply {
            setFilter(Nearest, Nearest)
        }

        // Load map
        farmMap = TmxMapLoader().load(Assets.MAP_FARM)

        extractRegions()
        log.info { "Assets loaded" }
    }

    /**
     * Extract texture regions from loaded textures. Call this after textures are loaded.
     */
    private fun extractRegions() {
        // Character Tori - 32x32
        charToriIdleFront = TextureRegion(charToriIdleTexture, 0, 0, 32, 32)
    }

    // Progress for loading screens (0.0 to 1.0)
    fun getProgress(): Float = assetStorage.progress.percent

    fun dispose() {
        assetStorage.dispose()
        log.info { "Assets disposed" }
    }
}
