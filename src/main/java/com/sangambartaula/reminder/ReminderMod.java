package com.sangambartaula.reminder;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = ReminderMod.MODID, version = ReminderMod.VERSION, name = ReminderMod.NAME, clientSideOnly = true)
public class ReminderMod {
    public static final String MODID = "reminder";
    public static final String VERSION = "1.0";
    public static final String NAME = "Reminder Mod";
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ReminderManager.INSTANCE.loadReminders();
        KeyBindings.init();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ReminderEventHandler());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        ClientCommandHandler.instance.registerCommand(new ReminderCommand());
    }
}