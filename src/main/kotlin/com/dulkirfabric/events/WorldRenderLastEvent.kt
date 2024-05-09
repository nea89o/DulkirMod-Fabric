package com.dulkirfabric.events

import com.dulkirfabric.events.base.Event
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.util.math.MatrixStack

data class
WorldRenderLastEvent(val context: WorldRenderContext, val matrixStack: MatrixStack): Event()
