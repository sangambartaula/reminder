package com.sangambartaula.reminder;

import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageHandler {
	private static final String MOTES_REMINDER_NAME = "Motes";
	private static final long MAX_TIME_FOR_MOTES_RESET_MILLIS = 119 * 60 * 1000;

	private static final Pattern MOTES_COLLECTED_PATTERN = Pattern.compile(
		"You earned [\\d,]+ Motes.*in this match",
		Pattern.CASE_INSENSITIVE
	);

	private static final Pattern SPLIT_COOLDOWN_PATTERN = Pattern.compile(
		"SPLIT!.*You need to wait (?:(\\d+)h\\s*)?(?:(\\d+)m)? before you can play again",
		Pattern.CASE_INSENSITIVE
	);

	public static void onGameMessage(Component message, boolean overlay) {
		if (message == null) {
			return;
		}

		String text = stripFormattingCodes(message.getString());

		if (MOTES_COLLECTED_PATTERN.matcher(text).find()) {
			handleMotesCollected();
			return;
		}

		Matcher splitMatcher = SPLIT_COOLDOWN_PATTERN.matcher(text);
		if (splitMatcher.find()) {
			handleSplitCooldown(splitMatcher);
		}
	}

	private static String stripFormattingCodes(String message) {
		return message.replaceAll("§.", "");
	}

	private static void handleMotesCollected() {
		Reminder motesReminder = ReminderManager.INSTANCE.findReminderByName(MOTES_REMINDER_NAME);
		if (motesReminder == null) {
			return;
		}

		long timeRemaining = motesReminder.getTriggerTime() - System.currentTimeMillis();
		if (timeRemaining < MAX_TIME_FOR_MOTES_RESET_MILLIS) {
			ReminderManager.INSTANCE.resetReminderToFullInterval(motesReminder);
		}
	}

	private static void handleSplitCooldown(Matcher matcher) {
		Reminder motesReminder = ReminderManager.INSTANCE.findReminderByName(MOTES_REMINDER_NAME);
		if (motesReminder == null) {
			return;
		}

		long cooldownMillis = parseTimeFromMatcher(matcher);
		if (cooldownMillis > 0) {
			motesReminder.setTriggerTimeFromNow(cooldownMillis);
			ReminderManager.INSTANCE.saveReminders();
		}
	}

	private static long parseTimeFromMatcher(Matcher matcher) {
		long totalMillis = 0;

		String hoursStr = matcher.group(1);
		String minutesStr = matcher.group(2);

		if (hoursStr != null && !hoursStr.isEmpty()) {
			totalMillis += Long.parseLong(hoursStr) * 60 * 60 * 1000;
		}

		if (minutesStr != null && !minutesStr.isEmpty()) {
			totalMillis += Long.parseLong(minutesStr) * 60 * 1000;
		}

		return totalMillis;
	}
}
