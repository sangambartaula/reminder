package com.sangambartaula.reminder;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class ReminderCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "remindme";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/remindme <set|list|remove|toggle> [args]";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /remindme set <taskname> <time> [auto] | /remindme list | /remindme remove <id> | /remindme toggle <id>"));
            return;
        }
        
        String subcommand = args[0].toLowerCase();
        
        if (subcommand.equals("set")) {
            handleSet(sender, args);
        } else if (subcommand.equals("list")) {
            handleList(sender);
        } else if (subcommand.equals("remove")) {
            handleRemove(sender, args);
        } else if (subcommand.equals("toggle")) {
            handleToggle(sender, args);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand));
        }
    }
    
    private void handleSet(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "/remindme set <taskname> <time> [auto]"));
            return;
        }
        
        String taskName = args[1];
        String timeString = args[2];
        boolean recurring = args.length >= 4 && args[3].equalsIgnoreCase("auto");
        
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
    
    private void handleList(ICommandSender sender) {
        List<Reminder> reminders = ReminderManager.INSTANCE.getReminders();
        if (reminders.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "No reminders set."));
            return;
        }
        
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "=== Active Reminders ==="));
        for (int i = 0; i < reminders.size(); i++) {
            Reminder r = reminders.get(i);
            long timeLeft = r.getTriggerTime() - System.currentTimeMillis();
            String recurring = r.isRecurring() ? "t" : "f";
            sender.addChatMessage(new ChatComponentText(
                String.format("%s%d %s%s %s %s", 
                    EnumChatFormatting.WHITE, i, 
                    r.getTaskName(), 
                    EnumChatFormatting.GRAY,
                    formatTime(Math.max(0, timeLeft)), 
                    recurring)
            ));
        }
    }
    
    private void handleRemove(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "/remindme remove <id>"));
            return;
        }
        
        try {
            int index = Integer.parseInt(args[1]);
            List<Reminder> reminders = ReminderManager.INSTANCE.getReminders();
            
            if (index < 0 || index >= reminders.size()) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid reminder ID: " + index));
                return;
            }
            
            Reminder removed = reminders.get(index);
            ReminderManager.INSTANCE.removeReminder(removed);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Removed reminder: " + removed.getTaskName()));
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid number format!"));
        }
    }
    
    private void handleToggle(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "/remindme toggle <id>"));
            return;
        }
        
        try {
            int index = Integer.parseInt(args[1]);
            List<Reminder> reminders = ReminderManager.INSTANCE.getReminders();
            
            if (index < 0 || index >= reminders.size()) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid reminder ID: " + index));
                return;
            }
            
            Reminder reminder = reminders.get(index);
            reminder.setActive(!reminder.isActive());
            ReminderManager.INSTANCE.saveReminders();
            
            String status = reminder.isActive() ? "ENABLED" : "DISABLED";
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Reminder " + status + ": " + reminder.getTaskName()));
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid number format!"));
        }
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
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "set", "list", "remove", "toggle");
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("set") && args.length == 4) {
            return getListOfStringsMatchingLastWord(args, "auto");
        }
        return new ArrayList<>();
    }
}