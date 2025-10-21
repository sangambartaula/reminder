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
    private long reminderStartTime = 0;
    private long lastPlayerInputTime = 0;
    private boolean isAFK = false;
    private static final long SOUND_INTERVAL = 300; // Play sound every 300ms
    private static final long AFK_THRESHOLD = 10000; // 10 seconds
    private boolean registered = false;
    
    private ReminderOverlay() {}
    
    public void setReminder(Reminder reminder) {
        if (activeReminder == null) {
            activeReminder = reminder;
            soundTimer = System.currentTimeMillis();
            reminderStartTime = System.currentTimeMillis();
            lastPlayerInputTime = System.currentTimeMillis();
            isAFK = false;
            playReminderSound();
            
            if (!registered) {
                MinecraftForge.EVENT_BUS.register(this);
                registered = true;
            }
        }
    }
    
    public void dismiss() {
        activeReminder = null;
        isAFK = false;
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
        
        long currentTime = System.currentTimeMillis();
        
        // Check for player input (keyboard or mouse movement)
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_W) || 
            Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_S) || 
            Keyboard.isKeyDown(Keyboard.KEY_D) || Mouse.getDX() != 0 || Mouse.getDY() != 0) {
            lastPlayerInputTime = currentTime;
            if (isAFK) {
                isAFK = false;
                soundTimer = currentTime; // Reset sound timer when coming back
            }
        }
        
        // Check if AFK (no input for 10 seconds)
        if (currentTime - lastPlayerInputTime >= AFK_THRESHOLD) {
            isAFK = true;
        }
        
        // Play repeating sound only if not AFK
        if (!isAFK && currentTime - soundTimer >= SOUND_INTERVAL) {
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
        String title = isAFK ? "REMINDER (AFK MODE)" : "REMINDER!";
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
        int buttonWidth = 140;
        int buttonHeight = 20;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = height / 2 + 30;
        
        drawRect(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFFAA0000);
        String keyName = Keyboard.getKeyName(KeyBindings.dismissReminder.getKeyCode());
        String buttonText = "Dismiss (Click [" + keyName + "])";
        int buttonTextWidth = mc.fontRendererObj.getStringWidth(buttonText);
        mc.fontRendererObj.drawStringWithShadow(buttonText, buttonX + (buttonWidth - buttonTextWidth) / 2, buttonY + 6, 0xFFFFFF);
        
        // Check for mouse click
        if (Mouse.isButtonDown(0)) {
            if (isMouseOverButton(buttonX, buttonY, buttonWidth, buttonHeight)) {
                ReminderEventHandler.dismissReminder();
            }
        }
    }
    
    private boolean isMouseOverButton(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        
        int mouseX = Mouse.getX() / scale;
        int mouseY = (mc.displayHeight - Mouse.getY()) / scale;
        
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    private void playReminderSound() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.playSound("note.pling", 1.0F, 1.0F);
        }
    }
}