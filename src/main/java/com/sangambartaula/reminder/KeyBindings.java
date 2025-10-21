package com.sangambartaula.reminder;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {
    public static KeyBinding dismissReminder;
    
    public static void init() {
        dismissReminder = new KeyBinding("key.remindme.dismiss", Keyboard.KEY_K, "key.categories.misc");
        ClientRegistry.registerKeyBinding(dismissReminder);
    }
}