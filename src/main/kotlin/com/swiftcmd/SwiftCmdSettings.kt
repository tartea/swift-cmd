package com.swiftcmd

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Application-level persistent settings for the SwiftCmd plugin.
 */
@State(name = "SwiftCmdSettings", storages = [Storage("swiftCmdSettings.xml")])
@Service
class SwiftCmdSettings : PersistentStateComponent<SwiftCmdSettings.State> {

    class State {
        @JvmField var command: String = DEFAULT_COMMAND
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    val current: State
        get() = myState

    companion object {
        const val DEFAULT_COMMAND = "echo Hello, SwiftCmd!"

        @JvmStatic
        fun getInstance(): SwiftCmdSettings =
            ApplicationManager.getApplication().getService(SwiftCmdSettings::class.java)
    }
}
