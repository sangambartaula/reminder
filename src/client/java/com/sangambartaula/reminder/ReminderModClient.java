package com.sangambartaula.reminder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class ReminderModClient implements ClientModInitializer {
	public static final String MOD_ID = "reminder";

	@Override
	public void onInitializeClient() {
		ReminderManager.INSTANCE.loadReminders();
		KeyMappingHelper.registerKeyMapping(KeyBindings.DISMISS_REMINDER);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ReminderEventHandler.onClientTick();
			KeyInputHandler.onClientTick();
		});

		ClientReceiveMessageEvents.GAME.register(ChatMessageHandler::onGameMessage);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
			ReminderCommand.register(dispatcher));

		HudElementRegistry.addLast(
			Identifier.fromNamespaceAndPath(MOD_ID, "overlay"),
			ReminderOverlay.INSTANCE::extractRenderState
		);
	}
}
