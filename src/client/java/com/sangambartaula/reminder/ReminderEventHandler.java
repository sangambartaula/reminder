package com.sangambartaula.reminder;

import net.minecraft.client.Minecraft;

public class ReminderEventHandler {
	private static Reminder currentReminder = null;
	private static long lastCheckTime = 0;
	private static final long CHECK_INTERVAL = 1000;

	public static void onClientTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			return;
		}

		long currentTime = System.currentTimeMillis();

		if (currentTime - lastCheckTime >= CHECK_INTERVAL) {
			lastCheckTime = currentTime;
			ReminderManager.INSTANCE.checkReminders(currentTime);
		}

		if (currentReminder != null) {
			ReminderOverlay.INSTANCE.setReminder(currentReminder);
		}
	}

	public static void triggerReminder(Reminder reminder) {
		currentReminder = reminder;
	}

	public static void dismissReminder() {
		if (currentReminder != null && currentReminder.isRecurring()) {
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
