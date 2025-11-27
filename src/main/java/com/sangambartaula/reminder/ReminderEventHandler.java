package com.sangambartaula.reminder;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ReminderEventHandler {
    
    private static Reminder currentReminder = null;
    private static long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 1000; // Check every second
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Check reminders periodically
        if (currentTime - lastCheckTime >= CHECK_INTERVAL) {
            lastCheckTime = currentTime;
            ReminderManager.INSTANCE.checkReminders(currentTime);
        }
        
        // Show reminder overlay if there's an active reminder
        if (currentReminder != null) {
            ReminderOverlay.INSTANCE.setReminder(currentReminder);
        }
    }
    
    public static void triggerReminder(Reminder reminder) {
        currentReminder = reminder;
    }
    
    public static void dismissReminder() {
        if (currentReminder != null && currentReminder.isRecurring()) {
            // Reset recurring reminder from dismiss time, not trigger time
            currentReminder.reset(System.currentTimeMillis());
            ReminderManager.INSTANCE.saveReminders();
        }
        currentReminder = null;
        ReminderOverlay.INSTANCE.dismiss();
    }
    
    public static Reminder getCurrentReminder() {
        return currentReminder;
    }
}
