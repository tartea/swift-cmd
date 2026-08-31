package com.swiftcmd

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Settings page: Settings | Tools | SwiftCmd.
 */
class SwiftCmdSettingsPage : Configurable {

    private val commandField = JBTextField(50)

    private val settings: SwiftCmdSettings
        get() = SwiftCmdSettings.getInstance()

    override fun getDisplayName(): String = "SwiftCmd"

    override fun createComponent(): JComponent {
        commandField.toolTipText = "The shell command to run, e.g. \"cd xxx; dir\""

        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.insets = Insets(6, 6, 6, 6)
        gbc.anchor = GridBagConstraints.WEST
        gbc.fill = GridBagConstraints.HORIZONTAL

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JLabel("Command:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(commandField, gbc)

        return panel
    }

    override fun isModified(): Boolean =
        commandField.text != settings.current.command

    override fun apply() {
        settings.current.command = commandField.text
    }

    override fun reset() {
        commandField.text = settings.current.command
    }
}
