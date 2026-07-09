package com.sangambartaula.reminder;

public class KeyInputHandler {
	public static void onClientTick() {
		while (KeyBindings.DISMISS_REMINDER.consumeClick()) {
			ReminderEventHandler.dismissReminder();
		}
	}
}
