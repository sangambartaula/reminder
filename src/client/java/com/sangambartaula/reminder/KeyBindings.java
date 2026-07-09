package com.sangambartaula.reminder;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
	public static final KeyMapping DISMISS_REMINDER = new KeyMapping(
		"key.reminder.dismiss",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_K,
		KeyMapping.Category.MISC
	);
}
