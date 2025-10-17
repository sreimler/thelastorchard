package com.sreimler.thelastorchard

import com.sreimler.thelastorchard.assets.GameAssets
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.log.logger


class GameApp : KtxGame<KtxScreen>() {
    private val log = logger<GameApp>()

    lateinit var assets: GameAssets
        private set

    override fun create() {
        // Initialize KTX coroutines (required for async assets)
        KtxAsync.initiate()

        assets = GameAssets()

        KtxAsync.launch {
            assets.loadAll()

            // Once loaded, switch to game screen
            addScreen(GameScreen(assets))
            setScreen<GameScreen>()
            log.debug { "Game initialized" }
        }
    }

    override fun dispose() {
        super.dispose()
        assets.dispose()
        log.info { "Game disposed" }
    }
}
