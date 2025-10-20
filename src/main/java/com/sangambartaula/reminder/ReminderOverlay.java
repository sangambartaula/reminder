package com.sangambartaula.reminder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ReminderOverlay extends Gui {
    
    public static final ReminderOverlay INSTANCE = new ReminderOverlay();
    
    private Reminder activeReminder = null;
    private long soundTimer = 0;
    private static final long SOUND_INTERVAL = 2000; // Play sound every 2 seconds
    private boolean registered = false;
    
    private ReminderOverlay() {}
    
    public void setReminder(Reminder reminder) {
        if (activeReminder == null) {
            activeReminder = reminder;
            soundTimer = System.currentTimeMillis();
            playReminderSound();
            
            if (!registered) {
                MinecraftForge.EVENT_BUS.register(this);
                registered = true;
            }
        }
    }
    
    public void dismiss() {
        activeReminder = null;
        if (registered) {
            MinecraftForge.EVENT_BUS.unregister(this);
            registered = false;
        }
    }
    
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || activeReminder == null) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();
        
        // Play repeating sound
        long currentTime = System.currentTimeMillis();
        if (currentTime - soundTimer >= SOUND_INTERVAL) {
            playReminderSound();
            soundTimer = currentTime;
        }
        
        // Draw semi-transparent background
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.6F);
        drawRect(0, 0, width, height, 0x99000000);
        
        // Draw title text
        String title = "REMINDER!";
        int titleWidth = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawStringWithShadow(title, (width - titleWidth) / 2, height / 2 - 50, 0xFFFF55);
        
        // Draw task name
        String taskName = activeReminder.getTaskName();
        int taskWidth = mc.fontRendererObj.getStringWidth(taskName) * 2;
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        mc.fontRendererObj.drawStringWithShadow(taskName, (width / 2 - taskWidth / 2) / 2, (height / 2 - 20) / 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        
        // Draw dismiss button
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = height / 2 + 30;
        
        drawRect(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFFAA0000);
        String buttonText = "Dismiss (Click)";
        int buttonTextWidth = mc.fontRendererObj.getStringWidth(buttonText);
        mc.fontRendererObj.drawStringWithShadow(buttonText, buttonX + (buttonWidth - buttonTextWidth) / 2, buttonY + 6, 0xFFFFFF);
        
        // Handle mouse click
        if (Mouse.isButtonDown(0)) {
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
            
            if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                ReminderEventHandler.dismissReminder();
            }
        }
        
        // Handle keyboard dismiss (ESC or ENTER)
        if (Keyboard.isKeyDown(Keyboard.KEY_RETURN) || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            try {
                Thread.sleep(200); // Debounce
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ReminderEventHandler.dismissReminder();
        }
        
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    private void playReminderSound() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.thePlayer != null) {
            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, 
                "note.pling", 1.0F, 1.0F, false);
        }
    }
}
