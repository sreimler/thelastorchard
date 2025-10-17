package com.sreimler.thelastorchard.assets

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.assets.async.AssetStorage
import ktx.log.logger


class GameAssets {
    private val log = logger<GameAssets>()

    // KTX AssetStorage (coroutine-based, replaces AssetManager)
    private val assetStorage = AssetStorage()

    // Load assets lazily
    private lateinit var charToriIdleTexture: Texture
    private lateinit var tilesetGrassSummerTexture: Texture

    // Define regions
    lateinit var charToriIdleFront: TextureRegion
        private set
    lateinit var tileGrassSummer: TextureRegion
        private set

    // Load all assets synchronously. For loading screens, use loadAsync() instead.
    suspend fun loadAll() {
        log.info { "Loading game assets.." }

        // Load textures (async but we'll await)
        charToriIdleTexture = assetStorage.loadAsync<Texture>(Assets.CHAR_TORI_IDLE).await().apply {
            setFilter(Nearest, Nearest)
        }
        tilesetGrassSummerTexture = assetStorage.loadAsync<Texture>(Assets.TILESET_GRASS_SUMMER).await().apply {
            setFilter(Nearest, Nearest)
        }

        extractRegions()
    }

    /**
     * Extract texture regions from loaded textures. Call this after textures are loaded.
     */
    private fun extractRegions() {
        // Character Tori - 32x32
        charToriIdleFront = TextureRegion(charToriIdleTexture, 0, 0, 32, 32)

        // Grass - 64x64
        tileGrassSummer = TextureRegion(tilesetGrassSummerTexture, 64, 128, 64, 64)
    }

    // Progress for loading screens (0.0 to 1.0)
    fun getProgress(): Float = assetStorage.progress.percent

    fun dispose() {
        assetStorage.dispose()
        log.info { "Assets disposed" }
    }
}
