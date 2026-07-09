package com.sangambartaula.reminder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class ReminderOverlay {
	public static final ReminderOverlay INSTANCE = new ReminderOverlay();

	private Reminder activeReminder = null;
	private long soundTimer = 0;
	private long lastPlayerInputTime = 0;
	private boolean isAFK = false;
	private boolean mouseWasDown = false;
	private double lastMouseX = Double.NaN;
	private double lastMouseY = Double.NaN;
	private static final long SOUND_INTERVAL = 300;
	private static final long AFK_THRESHOLD = 10000;

	private ReminderOverlay() {
	}

	public void setReminder(Reminder reminder) {
		if (activeReminder == null) {
			activeReminder = reminder;
			soundTimer = System.currentTimeMillis();
			lastPlayerInputTime = System.currentTimeMillis();
			isAFK = false;
			playReminderSound();
		}
	}

	public void dismiss() {
		activeReminder = null;
		isAFK = false;
		mouseWasDown = false;
		lastMouseX = Double.NaN;
		lastMouseY = Double.NaN;
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (activeReminder == null) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();
		long currentTime = System.currentTimeMillis();

		if (hasPlayerInput(mc)) {
			lastPlayerInputTime = currentTime;
			if (isAFK) {
				isAFK = false;
				soundTimer = currentTime;
			}
		}

		if (currentTime - lastPlayerInputTime >= AFK_THRESHOLD) {
			isAFK = true;
		}

		if (!isAFK && currentTime - soundTimer >= SOUND_INTERVAL) {
			playReminderSound();
			soundTimer = currentTime;
		}

		graphics.fill(0, 0, width, height, 0x99000000);

		Font font = mc.font;
		String title = isAFK ? "REMINDER (AFK MODE)" : "REMINDER!";
		int titleWidth = font.width(title);
		graphics.text(font, title, (width - titleWidth) / 2, height / 2 - 50, 0xFFFF55, true);

		String taskName = activeReminder.getTaskName();
		graphics.pose().pushMatrix();
		graphics.pose().scale(2.0F, 2.0F);
		int taskWidth = font.width(taskName);
		graphics.text(font, taskName, (width / 2 - taskWidth) / 2, (height / 2 - 20) / 2, 0xFFFFFF, true);
		graphics.pose().popMatrix();

		int buttonWidth = 140;
		int buttonHeight = 20;
		int buttonX = (width - buttonWidth) / 2;
		int buttonY = height / 2 + 30;

		graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFFAA0000);

		String keyName = KeyMappingHelper.getBoundKeyOf(KeyBindings.DISMISS_REMINDER).getDisplayName().getString();
		String buttonText = "Dismiss (Click [" + keyName + "])";
		int buttonTextWidth = font.width(buttonText);
		graphics.text(
			font,
			buttonText,
			buttonX + (buttonWidth - buttonTextWidth) / 2,
			buttonY + 6,
			0xFFFFFF,
			true
		);

		boolean mouseDown = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		if (mouseDown && !mouseWasDown && isMouseOverButton(mc, buttonX, buttonY, buttonWidth, buttonHeight)) {
			ReminderEventHandler.dismissReminder();
		}
		mouseWasDown = mouseDown;
	}

	private boolean hasPlayerInput(Minecraft mc) {
		double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
		double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());
		boolean mouseMoved = !Double.isNaN(lastMouseX)
			&& (mouseX != lastMouseX || mouseY != lastMouseY);
		lastMouseX = mouseX;
		lastMouseY = mouseY;

		return InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
			|| mc.options.keyUp.isDown()
			|| mc.options.keyLeft.isDown()
			|| mc.options.keyDown.isDown()
			|| mc.options.keyRight.isDown()
			|| mouseMoved;
	}

	private static boolean isMouseOverButton(Minecraft mc, int x, int y, int width, int height) {
		double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
		double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	private void playReminderSound() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
		}
	}
}
