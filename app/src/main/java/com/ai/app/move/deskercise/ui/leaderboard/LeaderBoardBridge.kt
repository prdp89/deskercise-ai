package com.ai.app.move.deskercise.ui.leaderboard

interface LeaderBoardBridge {
    fun shouldShowBackButton(): Boolean
}

class LeaderBoardBridgeDefault: LeaderBoardBridge{
    override fun shouldShowBackButton() = false
}