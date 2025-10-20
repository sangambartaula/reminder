package com.sangambartaula.reminder;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class ReminderCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "remindme";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/remindme <taskname> <time> [auto] - Set a reminder. Time format: 1h, 30m, 2h30m. Use 'auto' for recurring.";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + getCommandUsage(sender)));
            return;
        }
        
        String taskName = args[0];
        String timeString = args[1];
        boolean recurring = args.length >= 3 && args[2].equalsIgnoreCase("auto");
        
        long intervalMillis = parseTime(timeString);
        if (intervalMillis <= 0) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid time format! Use format like: 1h, 30m, 2h30m"));
            return;
        }
        
        long triggerTime = System.currentTimeMillis() + intervalMillis;
        Reminder reminder = new Reminder(taskName, triggerTime, recurring, intervalMillis);
        ReminderManager.INSTANCE.addReminder(reminder);
        
        String recurringText = recurring ? " (recurring)" : "";
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Reminder set: " + taskName + " in " + formatTime(intervalMillis) + recurringText));
    }
    
    private long parseTime(String timeString) {
        long totalMillis = 0;
        String current = "";
        
        for (char c : timeString.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                current += c;
            } else if (c == 'h') {
                if (!current.isEmpty()) {
                    totalMillis += Long.parseLong(current) * 3600000;
                    current = "";
                }
            } else if (c == 'm') {
                if (!current.isEmpty()) {
                    totalMillis += Long.parseLong(current) * 60000;
                    current = "";
                }
            } else if (c == 's') {
                if (!current.isEmpty()) {
                    totalMillis += Long.parseLong(current) * 1000;
                    current = "";
                }
            }
        }
        
        return totalMillis;
    }
    
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        String result = "";
        if (hours > 0) {
            result += hours + "h";
            minutes = minutes % 60;
        }
        if (minutes > 0) {
            result += (result.isEmpty() ? "" : " ") + minutes + "m";
        }
        if (result.isEmpty()) {
            seconds = seconds % 60;
            result = seconds + "s";
        }
        
        return result;
    }
    
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 3) {
            return Arrays.asList("auto");
        }
        return null;
    }
}
