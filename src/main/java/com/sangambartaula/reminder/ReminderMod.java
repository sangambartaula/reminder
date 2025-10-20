package com.sangambartaula.reminder;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ReminderMod.MODID, version = ReminderMod.VERSION, name = ReminderMod.NAME)
public class ReminderMod {
    public static final String MODID = "reminder";
    public static final String VERSION = "1.0";
    public static final String NAME = "Reminder Mod";
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ReminderManager.INSTANCE.loadReminders();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ReminderEventHandler());
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ReminderCommand());
    }
}
