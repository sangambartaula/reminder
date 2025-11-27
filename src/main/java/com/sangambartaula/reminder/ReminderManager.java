package com.sangambartaula.reminder;

import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReminderManager {
    public static final ReminderManager INSTANCE = new ReminderManager();
    
    private List<Reminder> reminders = new ArrayList<>();
    private File saveFile;
    
    private ReminderManager() {
        saveFile = new File(Minecraft.getMinecraft().mcDataDir, "reminders.dat");
    }
    
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
        saveReminders();
    }
    
    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
        saveReminders();
    }
    
    public List<Reminder> getReminders() {
        return new ArrayList<>(reminders);
    }
    
    public void checkReminders(long currentTime) {
        Iterator<Reminder> iterator = reminders.iterator();
        while (iterator.hasNext()) {
            Reminder reminder = iterator.next();
            if (reminder.shouldTrigger(currentTime)) {
                ReminderEventHandler.triggerReminder(reminder);
                
                if (!reminder.isRecurring()) {
                    iterator.remove();
                }
                // Note: Recurring reminders are reset at dismiss time in ReminderEventHandler.dismissReminder()
                // This ensures the timer resets from when the user actually dismisses, not when it triggered
            }
        }
        saveReminders();
    }
    
    public void saveReminders() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(reminders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadReminders() {
        if (!saveFile.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            reminders = (List<Reminder>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            reminders = new ArrayList<>();
        }
    }
    
    /**
     * Finds a reminder by task name (case-insensitive).
     * @param name The task name to search for
     * @return The matching Reminder, or null if not found
     */
    public Reminder findReminderByName(String name) {
        for (Reminder reminder : reminders) {
            if (reminder.getTaskName().equalsIgnoreCase(name)) {
                return reminder;
            }
        }
        return null;
    }
    
    /**
     * Resets a reminder to its full interval from the current time.
     * @param reminder The reminder to reset
     */
    public void resetReminderToFullInterval(Reminder reminder) {
        if (reminder != null) {
            reminder.reset(System.currentTimeMillis());
            saveReminders();
        }
    }
}
