package com.sangambartaula.reminder;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageHandler {
    
    private static final String MOTES_REMINDER_NAME = "Motes";
    private static final long MAX_TIME_FOR_MOTES_RESET_MILLIS = 119 * 60 * 1000; // 1h 59m = 119 minutes
    
    // Regex pattern for Motes collected message: "You earned 30,000 Motes in this match!"
    private static final Pattern MOTES_COLLECTED_PATTERN = Pattern.compile(
        "You earned .*?(\\d{1,3}(,\\d{3})*|\\d+) Motes.*in this match",
        Pattern.CASE_INSENSITIVE
    );
    
    // Regex pattern for SPLIT/Cooldown message: "SPLIT! You need to wait 45m before you can play again."
    // Time format can be: 45m, 1h 59m, 1h, etc.
    private static final Pattern SPLIT_COOLDOWN_PATTERN = Pattern.compile(
        "SPLIT!.*You need to wait (\\d+h\\s*)?(\\d+m)? before you can play again",
        Pattern.CASE_INSENSITIVE
    );
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (event.message == null) {
            return;
        }
        
        // Strip Minecraft formatting codes (§x) from the message
        String message = stripFormattingCodes(event.message.getUnformattedText());
        
        // Check for Motes collected message
        if (MOTES_COLLECTED_PATTERN.matcher(message).find()) {
            handleMotesCollected();
            return;
        }
        
        // Check for SPLIT/Cooldown message
        Matcher splitMatcher = SPLIT_COOLDOWN_PATTERN.matcher(message);
        if (splitMatcher.find()) {
            handleSplitCooldown(splitMatcher);
        }
    }
    
    /**
     * Strips Minecraft formatting codes (§x) from a message.
     */
    private String stripFormattingCodes(String message) {
        return message.replaceAll("§.", "");
    }
    
    /**
     * Handles the Motes collected message.
     * If a reminder named "Motes" exists and has less than 1h 59m remaining, reset it to full 2h interval.
     */
    private void handleMotesCollected() {
        Reminder motesReminder = ReminderManager.INSTANCE.findReminderByName(MOTES_REMINDER_NAME);
        if (motesReminder == null) {
            return;
        }
        
        long timeRemaining = motesReminder.getTriggerTime() - System.currentTimeMillis();
        
        // Only reset if less than 1h 59m (119 minutes) remaining
        if (timeRemaining < MAX_TIME_FOR_MOTES_RESET_MILLIS) {
            ReminderManager.INSTANCE.resetReminderToFullInterval(motesReminder);
        }
    }
    
    /**
     * Handles the SPLIT/Cooldown message.
     * Sets the Motes timer to have exactly the specified time remaining.
     */
    private void handleSplitCooldown(Matcher matcher) {
        Reminder motesReminder = ReminderManager.INSTANCE.findReminderByName(MOTES_REMINDER_NAME);
        if (motesReminder == null) {
            return;
        }
        
        long cooldownMillis = parseTimeFromMatcher(matcher);
        if (cooldownMillis > 0) {
            // Set the trigger time to now + cooldown, keeping the same interval for future cycles
            motesReminder.setTriggerTimeFromNow(cooldownMillis);
            ReminderManager.INSTANCE.saveReminders();
        }
    }
    
    /**
     * Parses time from the SPLIT matcher.
     * Group 1: hours part (e.g., "1h " or null)
     * Group 2: minutes part (e.g., "59m" or null)
     */
    private long parseTimeFromMatcher(Matcher matcher) {
        long totalMillis = 0;
        
        String hoursStr = matcher.group(1);
        String minutesStr = matcher.group(2);
        
        if (hoursStr != null) {
            // Extract number from "1h " or "1h"
            String hourNum = hoursStr.replaceAll("[^0-9]", "");
            if (!hourNum.isEmpty()) {
                totalMillis += Long.parseLong(hourNum) * 60 * 60 * 1000;
            }
        }
        
        if (minutesStr != null) {
            // Extract number from "59m" or "45m"
            String minuteNum = minutesStr.replaceAll("[^0-9]", "");
            if (!minuteNum.isEmpty()) {
                totalMillis += Long.parseLong(minuteNum) * 60 * 1000;
            }
        }
        
        return totalMillis;
    }
}
