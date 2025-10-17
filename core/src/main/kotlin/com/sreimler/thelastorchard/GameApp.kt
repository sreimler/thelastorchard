package com.sreimler.thelastorchard

import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.log.logger


class GameApp : KtxGame<KtxScreen>() {
    private val log = logger<GameApp>()

    override fun create() {
        KtxAsync.initiate()

        addScreen(GameScreen())
        setScreen<GameScreen>()
        log.debug { "Game initialized" }
    }
}
