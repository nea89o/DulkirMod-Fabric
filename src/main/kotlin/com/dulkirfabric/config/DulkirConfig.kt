/**
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * If it is not possible or desirable to put the notice in a particular
 * file, then You may include the notice in a location (such as a LICENSE
 * file in a relevant directory) where a recipient would be likely to look
 * for such a notice.
 *
 * You may add additional accurate notices of copyright ownership.
 */
@file:UseSerializers(com.dulkirfabric.config.serializations.KeySerializer::class)
package com.dulkirfabric.config

import com.dulkirfabric.DulkirModFabric.mc
import com.dulkirfabric.config.ConfigHelper.mkKeyField
import com.dulkirfabric.config.ConfigHelper.mkStringField
import com.dulkirfabric.config.ConfigHelper.mkToggle
import com.dulkirfabric.util.render.AnimationPreset
import com.dulkirfabric.util.render.HudElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.shedaniel.clothconfig2.api.ConfigBuilder
import meteordevelopment.orbit.EventHandler
import moe.nea.jarvis.api.Point
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.InputUtil.UNKNOWN_KEY
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.io.File

class DulkirConfig {

    private val buttonText: Text =
        MutableText.of(LiteralTextContent("Dulkir")).formatted(Formatting.BOLD, Formatting.YELLOW)
    var screen: Screen

    init {
        val builder = ConfigBuilder.create().setTitle(buttonText)
        builder.setDefaultBackgroundTexture(Identifier("minecraft:textures/block/oak_planks.png"))
        builder.setGlobalized(true)
        builder.setGlobalizedExpanded(false)
        builder.setParentScreen(mc.currentScreen)
        builder.setSavingRunnable(::saveConfig)
        val entryBuilder = builder.entryBuilder()
        val general = builder.getOrCreateCategory(Text.literal("General"))
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Inventory Scale Toggle"),
                configOptions::invScaleBool,
                Text.literal("This is a tooltip"))
        )
        general.addEntry(
            entryBuilder.startFloatField(Text.literal("Inventory Scale"), configOptions.inventoryScale)
                .setTooltip(Text.literal("Size of GUI whenever you're in an inventory screen"))
                .setSaveConsumer { newValue ->
                    configOptions.inventoryScale = newValue
                }
                .build()
        )
        general.addEntry(
            entryBuilder.startFloatField(Text.literal("Tooltip Scale"), configOptions.tooltipScale)
                .setTooltip(Text.literal("Default Value for Scaling a particular tooltip without scroll input"))
                .setSaveConsumer { newValue -> configOptions.tooltipScale = newValue }
                .build()
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Ignore Reverse Third Person"), configOptions::ignoreReverseThirdPerson)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Disable Status Effect Rendering"), configOptions::statusEffectHidden)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Custom Block outlines"), configOptions::customBlockOutlines)
        )
        general.addEntry(
            entryBuilder.startIntSlider(Text.literal("Line Thickness"), configOptions.blockOutlineThickness, 1, 5)
                .setSaveConsumer { newValue -> configOptions.blockOutlineThickness = newValue }
                .build()
        )
        general.addEntry(
            entryBuilder.startColorField(Text.literal("Outline Color"), TextColor.fromRgb(configOptions.blockOutlineColor))
                .setSaveConsumer { newValue -> configOptions.blockOutlineColor = newValue }
                .build()
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Abiphone DND"), configOptions::abiPhoneDND)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Abiphone Caller ID"), configOptions::abiPhoneCallerID)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Inactive Effigy Waypoints"), configOptions::inactiveEffigyDisplay)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Disable Explosion Particles"), configOptions::disableExplosionParticles)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Durability-Based Cooldown Display"), configOptions::duraCooldown)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Hide Armor Overlay in Skyblock"), configOptions::hideArmorOverlay)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Hide Hunger Overlay in Skyblock"), configOptions::hideHungerOverlay)
        )
        general.addEntry(
            entryBuilder.startIntSlider(Text.literal("Anti Downtime Alarm"), configOptions.alarmTimeout, 0, 1000)
                .setSaveConsumer {
                    configOptions.alarmTimeout = it
                }
                .setTooltip(Text.literal("Set to 0 to disable. (Time in seconds)"))
                .build()
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Arachne Keeper Waypoints"), configOptions::arachneKeeperWaypoints)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Arachne Boss Spawn Timer"), configOptions::arachneSpawnTimer)
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Convert Action Bar to HUD elements"), configOptions::hudifyActionBar,
                tooltip = Text.literal("This converts Mana/Health/Def/Stacks as HUD elements"))
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Include EHP in def HUD element"), configOptions::showEHP,
                tooltip = Text.literal("Must have Action Bar HUD elements Enabled"))
        )
        general.addEntry(
            entryBuilder.mkToggle(Text.literal("Hide Held Item Tooltips"), configOptions::hideHeldItemTooltip,
                tooltip = Text.literal("This is for the pesky overlay that pops up on switching items"))
        )

        val shortcuts = builder.getOrCreateCategory(Text.literal("Shortcuts"))
        shortcuts.addEntry(
            entryBuilder.mkKeyField(Text.literal("Dynamic Key"), configOptions::dynamicKey)
        )
        shortcuts.addEntry(
            ConfigHelper.mkConfigList(
                Text.literal("Macros"),
                configOptions::macrosList,
                { Macro(UNKNOWN_KEY, "") },
                Text.literal("Macro"),
                { value ->
                    listOf(
                        entryBuilder.mkStringField(Text.literal("Command"), value::command),
                        entryBuilder.mkKeyField(Text.literal("KeyBinding"), value::keyBinding)
                    )
                }
            )
        )

        val aliases = builder.getOrCreateCategory(Text.literal("Shortcuts"))
        aliases.addEntry(
            ConfigHelper.mkConfigList(
                Text.literal("Aliases (do not include '/')"),
                configOptions::aliasList,
                { Alias("", "") },
                Text.literal("Alias"),
                { value ->
                    listOf(
                        entryBuilder.mkStringField(Text.literal("Command"), value::command),
                        entryBuilder.mkStringField(Text.literal("Alias"), value::alias)
                    )
                }
            )
        )
        val animations = builder.getOrCreateCategory(Text.literal("Animations"))

        //TODO: Come up with some custome float slider instead of int slider jank
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("posX"), configOptions.animationPreset.posX, -150, 150)
                .setSaveConsumer { newValue -> configOptions.animationPreset.posX = newValue }
                .setDefaultValue(0)
                .build()
        )
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("posY"), configOptions.animationPreset.posY, -150, 150)
                .setSaveConsumer { newValue -> configOptions.animationPreset.posY = newValue }
                .setDefaultValue(0)
                .build()
        )
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("posZ"), configOptions.animationPreset.posZ, -150, 50)
                .setSaveConsumer { newValue -> configOptions.animationPreset.posZ = newValue }
                .setDefaultValue(0)
                .build()
        )
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("rotationX"), configOptions.animationPreset.rotX, -180, 180)
                .setSaveConsumer { newValue -> configOptions.animationPreset.rotX = newValue }
                .setDefaultValue(0)
                .build()
        )
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("rotationY"), configOptions.animationPreset.rotY, -180, 180)
                .setSaveConsumer { newValue -> configOptions.animationPreset.rotY = newValue }
                .setDefaultValue(0)
                .build()
        )
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("rotationZ"), configOptions.animationPreset.rotZ, -180, 180)
                .setSaveConsumer { newValue -> configOptions.animationPreset.rotZ = newValue }
                .setDefaultValue(0)
                .build()
        )
        animations.addEntry(
            entryBuilder.startFloatField(Text.literal("Held Item Scale"), configOptions.animationPreset.scale)
                .setTooltip(Text.literal("Recommended range of .1 - 2"))
                .setSaveConsumer { newValue ->
                    configOptions.animationPreset.scale = newValue
                }
                .setDefaultValue(1f)
                .build()
        )
        animations.addEntry(
            entryBuilder.startIntSlider(Text.literal("Swing Speed"), configOptions.animationPreset.swingDuration, 2, 20)
                .setSaveConsumer { newValue -> configOptions.animationPreset.swingDuration = newValue }
                .setDefaultValue(6)
                .build()
        )
        animations.addEntry(
            entryBuilder.startBooleanToggle(Text.literal("Cancel Re-Equip Animation"), configOptions.animationPreset.cancelReEquip)
                .setSaveConsumer {newValue -> configOptions.animationPreset.cancelReEquip = newValue }
                .setDefaultValue(false)
                .build()
        )

        val bridge = builder.getOrCreateCategory(Text.literal("Bridge Features"))

        bridge.addEntry(
            entryBuilder.mkToggle(Text.literal("Format Bridge Messages"), configOptions::bridgeFormatter)
        )
        bridge.addEntry(
            entryBuilder.mkStringField(Text.literal("Bridge Bot IGN"), configOptions::bridgeBotName)
        )
        bridge.addEntry(
            entryBuilder.startColorField(Text.literal("Bridge User Color"), configOptions.bridgeNameColor)
                .setDefaultValue(Formatting.GOLD.colorValue!!)
                .setSaveConsumer { newValue -> configOptions.bridgeNameColor = newValue }
                .build()
        )

        builder.transparentBackground()
        screen = builder.build()
    }

    @Serializable
    data class ConfigOptions(
        var invScaleBool: Boolean = false,
        var inventoryScale: Float = 1f,
        var macrosList: List<Macro> = listOf(Macro(UNKNOWN_KEY, "")),
        var aliasList: List<Alias> = listOf(Alias("", "")),
        var ignoreReverseThirdPerson: Boolean = false,
        var dynamicKey: InputUtil.Key = UNKNOWN_KEY,
        var customBlockOutlines: Boolean = false,
        var blockOutlineThickness: Int = 3,
        var blockOutlineColor: Int = 0xFFFFFF,
        var abiPhoneDND: Boolean = false,
        var abiPhoneCallerID: Boolean = false,
        var tooltipScale: Float = 1f,
        var statusEffectHidden: Boolean = false,
        var inactiveEffigyDisplay: Boolean = false,
        var disableExplosionParticles: Boolean = false,
        var hideArmorOverlay: Boolean = false,
        var hideHungerOverlay: Boolean = false,
        var animationPreset: AnimationPreset = AnimationPreset(),
        var duraCooldown: Boolean = false,
        var alarmTimeout: Int = 300,
        var arachneKeeperWaypoints: Boolean = false,
        var arachneSpawnTimer: Boolean = false,
        var bridgeFormatter: Boolean = false,
        var bridgeBotName: String = "Dilkur",
        var bridgeNameColor: Int = Formatting.GOLD.colorValue!!,
        val positions: MutableMap<String, HudElement.Positioning> = mutableMapOf(),
        var hudifyActionBar: Boolean = true,
        var showEHP: Boolean = false,
        var hideHeldItemTooltip: Boolean = false
    )

    @Serializable
    data class Macro(
        var keyBinding: InputUtil.Key,
        var command: String,
    )

    @Serializable
    data class Alias(
        var alias: String,
        var command: String
    )

    /**
     * Object for storing all the actual config values that will be used in doing useful stuff with the config
     */
    companion object ConfigVars {

        var configOptions = ConfigOptions()

        val huds = mutableListOf<Triple<HudElement, Point, Float>>()

        fun hudElement(
            id: String, label: Text, width: Int, height: Int,
            defaultPosition: Point, scale: Float = 1f
        ): HudElement {
            val element = HudElement(
                configOptions.positions.getOrPut(
                    id
                ) { HudElement.Positioning(defaultPosition.x(), defaultPosition.y(), scale) },
                id,
                label, width, height,
            )
            huds.add(Triple(element, defaultPosition, scale))
            return element
        }

        fun saveConfig() {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

            val configDirectory = File(mc.runDirectory, "config")
            if (!configDirectory.exists()) {
                configDirectory.mkdir()
            }
            val configFile = File(configDirectory, "dulkirConfig.json")
            configFile.writeText(json.encodeToString(configOptions))
        }

        fun loadConfig() {
            val configDir = File(mc.runDirectory, "config")
            if (!configDir.exists()) return
            val configFile = File(configDir, "dulkirConfig.json")
            if (configFile.exists()) {
                val json = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
                configOptions = json.decodeFromString<ConfigOptions>(configFile.readText())
            }
            huds.forEach { (element, defaultPosition, scale) ->
                element.positioning = configOptions.positions.getOrPut(
                    element.key
                ) { HudElement.Positioning(defaultPosition.x(), defaultPosition.y(), scale) }
            }
        }
    }
}