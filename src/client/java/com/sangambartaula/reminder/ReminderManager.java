package com.sangambartaula.reminder;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReminderManager {
	public static final ReminderManager INSTANCE = new ReminderManager();

	private List<Reminder> reminders = new ArrayList<>();
	private File saveFile;

	private ReminderManager() {
	}

	private File getSaveFile() {
		if (saveFile == null) {
			saveFile = new File(Minecraft.getInstance().gameDirectory, "reminders.dat");
		}
		return saveFile;
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
			}
		}
		saveReminders();
	}

	public void saveReminders() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSaveFile()))) {
			oos.writeObject(reminders);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void loadReminders() {
		File file = getSaveFile();
		if (!file.exists()) {
			return;
		}

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			reminders = (List<Reminder>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			reminders = new ArrayList<>();
		}
	}

	public Reminder findReminderByName(String name) {
		for (Reminder reminder : reminders) {
			if (reminder.getTaskName().equalsIgnoreCase(name)) {
				return reminder;
			}
		}
		return null;
	}

	public void resetReminderToFullInterval(Reminder reminder) {
		if (reminder != null) {
			reminder.reset(System.currentTimeMillis());
			saveReminders();
		}
	}
}
