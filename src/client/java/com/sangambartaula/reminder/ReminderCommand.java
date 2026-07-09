package com.sangambartaula.reminder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ReminderCommand {
	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
			ClientCommands.literal("remindme")
				.then(ClientCommands.literal("set")
					.then(ClientCommands.argument("taskName", StringArgumentType.word())
						.then(ClientCommands.argument("time", StringArgumentType.word())
							.executes(context -> handleSet(
								context.getSource(),
								StringArgumentType.getString(context, "taskName"),
								StringArgumentType.getString(context, "time"),
								false
							))
							.then(ClientCommands.literal("auto")
								.executes(context -> handleSet(
									context.getSource(),
									StringArgumentType.getString(context, "taskName"),
									StringArgumentType.getString(context, "time"),
									true
								))
							)
						)
					)
				)
				.then(ClientCommands.literal("list")
					.executes(context -> handleList(context.getSource())))
				.then(ClientCommands.literal("remove")
					.then(ClientCommands.argument("id", IntegerArgumentType.integer(0))
						.executes(context -> handleRemove(
							context.getSource(),
							IntegerArgumentType.getInteger(context, "id")
						))
					)
				)
				.then(ClientCommands.literal("toggle")
					.then(ClientCommands.argument("id", IntegerArgumentType.integer(0))
						.executes(context -> handleToggle(
							context.getSource(),
							IntegerArgumentType.getInteger(context, "id")
						))
					)
				)
				.executes(context -> {
					sendError(context.getSource(),
						"Usage: /remindme set <taskname> <time> [auto] | /remindme list | /remindme remove <id> | /remindme toggle <id>");
					return 0;
				})
		);
	}

	private static int handleSet(FabricClientCommandSource source, String taskName, String timeString, boolean recurring) {
		long intervalMillis = parseTime(timeString);
		if (intervalMillis <= 0) {
			sendError(source, "Invalid time format! Use format like: 1h, 30m, 2h30m");
			return 0;
		}

		long triggerTime = System.currentTimeMillis() + intervalMillis;
		Reminder reminder = new Reminder(taskName, triggerTime, recurring, intervalMillis);
		ReminderManager.INSTANCE.addReminder(reminder);

		String recurringText = recurring ? " (recurring)" : "";
		source.sendFeedback(Component.literal(
			"Reminder set: " + taskName + " in " + formatTime(intervalMillis) + recurringText
		).withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int handleList(FabricClientCommandSource source) {
		List<Reminder> reminders = ReminderManager.INSTANCE.getReminders();
		if (reminders.isEmpty()) {
			source.sendFeedback(Component.literal("No reminders set.").withStyle(ChatFormatting.YELLOW));
			return 1;
		}

		source.sendFeedback(Component.literal("=== Active Reminders ===").withStyle(ChatFormatting.GOLD));
		for (int i = 0; i < reminders.size(); i++) {
			Reminder reminder = reminders.get(i);
			long timeLeft = reminder.getTriggerTime() - System.currentTimeMillis();
			String recurring = reminder.isRecurring() ? "t" : "f";
			source.sendFeedback(Component.literal(String.format(
				"%d %s %s %s",
				i,
				reminder.getTaskName(),
				formatTime(Math.max(0, timeLeft)),
				recurring
			)));
		}
		return 1;
	}

	private static int handleRemove(FabricClientCommandSource source, int index) {
		List<Reminder> reminders = ReminderManager.INSTANCE.getReminders();
		if (index < 0 || index >= reminders.size()) {
			sendError(source, "Invalid reminder ID: " + index);
			return 0;
		}

		Reminder removed = reminders.get(index);
		ReminderManager.INSTANCE.removeReminder(removed);
		source.sendFeedback(Component.literal("Removed reminder: " + removed.getTaskName()).withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int handleToggle(FabricClientCommandSource source, int index) {
		List<Reminder> reminders = ReminderManager.INSTANCE.getReminders();
		if (index < 0 || index >= reminders.size()) {
			sendError(source, "Invalid reminder ID: " + index);
			return 0;
		}

		Reminder reminder = reminders.get(index);
		reminder.setActive(!reminder.isActive());
		ReminderManager.INSTANCE.saveReminders();

		String status = reminder.isActive() ? "ENABLED" : "DISABLED";
		source.sendFeedback(Component.literal("Reminder " + status + ": " + reminder.getTaskName()).withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static void sendError(FabricClientCommandSource source, String message) {
		source.sendFeedback(Component.literal(message).withStyle(ChatFormatting.RED));
	}

	private static long parseTime(String timeString) {
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

	private static String formatTime(long millis) {
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
}
