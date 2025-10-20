# Reminder Mod - Usage Guide

## How It Works

```
┌─────────────────────────────────────────────────────────────────┐
│                     REMINDER MOD FLOW                            │
└─────────────────────────────────────────────────────────────────┘

1. SETTING A REMINDER
   ┌────────────────────────────────────────────────┐
   │  Player types command:                         │
   │  /remindme Motes 2h auto                      │
   └─────────────────┬──────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────────┐
   │  ReminderCommand parses:                        │
   │  • Task: "Motes"                                │
   │  • Time: 2 hours (7200000 ms)                   │
   │  • Recurring: Yes (auto)                        │
   └─────────────────┬───────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────────┐
   │  ReminderManager:                               │
   │  • Creates Reminder object                      │
   │  • Saves to reminders.dat                       │
   │  • Confirms with green chat message             │
   └─────────────────────────────────────────────────┘


2. CHECKING REMINDERS (Every Second)
   ┌─────────────────────────────────────────────────┐
   │  ReminderEventHandler (ClientTick):             │
   │  • Checks current time vs trigger time          │
   │  • If time elapsed → Trigger reminder           │
   └─────────────────┬───────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────────┐
   │  Time Check Results:                            │
   │  • Not yet due: Continue checking               │
   │  • Time passed: Show overlay                    │
   └─────────────────────────────────────────────────┘


3. SHOWING ALERT
   ┌─────────────────────────────────────────────────┐
   │  ReminderOverlay:                               │
   │  ┌───────────────────────────────────────────┐  │
   │  │                                           │  │
   │  │         🔔 REMINDER! 🔔                   │  │
   │  │                                           │  │
   │  │              Motes!                       │  │
   │  │         (Large Text)                      │  │
   │  │                                           │  │
   │  │       [Dismiss (Click)]                   │  │
   │  │                                           │  │
   │  └───────────────────────────────────────────┘  │
   │                                                 │
   │  • Semi-transparent black background            │
   │  • Plays "pling" sound every 2 seconds          │
   │  • Blocks game interaction until dismissed      │
   └─────────────────────────────────────────────────┘


4. DISMISSING
   ┌─────────────────────────────────────────────────┐
   │  User Actions:                                  │
   │  • Click dismiss button, OR                     │
   │  • Press Enter key, OR                          │
   │  • Press Escape key                             │
   └─────────────────┬───────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────────┐
   │  For Non-Recurring:                             │
   │  • Removes reminder from list                   │
   │  • Saves updated list to file                   │
   └─────────────────────────────────────────────────┘
   
   ┌─────────────────────────────────────────────────┐
   │  For Recurring (auto):                          │
   │  • Sets new trigger time (current + interval)   │
   │  • Keeps reminder active                        │
   │  • Saves updated list to file                   │
   └─────────────────────────────────────────────────┘


5. OFFLINE HANDLING
   ┌─────────────────────────────────────────────────┐
   │  Scenario: Game closed during reminder          │
   │                                                 │
   │  Set reminder: 2h                               │
   │  Close game                                     │
   │  Wait 3h                                        │
   │  Open game                                      │
   └─────────────────┬───────────────────────────────┘
                     │
                     ▼
   ┌─────────────────────────────────────────────────┐
   │  On Game Start:                                 │
   │  • ReminderManager loads from reminders.dat     │
   │  • First tick checks all reminders              │
   │  • Finds elapsed reminder (3h > 2h)             │
   │  • IMMEDIATELY shows alert                      │
   └─────────────────────────────────────────────────┘
```

## Command Examples

### Basic Reminder
```
/remindme Cobblestone 30m
```
Reminds you about "Cobblestone" in 30 minutes (one-time).

### Recurring Reminder
```
/remindme Farm 1h auto
```
Reminds you about "Farm" every 1 hour (recurring).

### Complex Time Format
```
/remindme Boss_Fight 2h30m
```
Reminds you about "Boss_Fight" in 2 hours and 30 minutes.

### Short Reminder
```
/remindme Check_Furnace 5m
```
Reminds you about "Check_Furnace" in 5 minutes.

## Time Format Reference

| Format | Description | Example |
|--------|-------------|---------|
| `Xh`   | Hours       | `2h` = 2 hours |
| `Xm`   | Minutes     | `30m` = 30 minutes |
| `Xs`   | Seconds     | `45s` = 45 seconds |
| Combined | Multiple units | `1h30m` = 1 hour 30 minutes |

## Features Summary

✅ **Persistent Storage**: Works across game sessions
✅ **Recurring Reminders**: Auto-restart with `auto` parameter
✅ **Big Title Display**: Large, visible task name
✅ **Multiple Dismiss Options**: Click, Enter, or Escape
✅ **Repeating Sound**: Alert sound every 2 seconds
✅ **Offline Support**: Triggers immediately if time passed while game closed
✅ **Multiple Reminders**: Set as many as you need
✅ **Flexible Time Format**: Hours, minutes, seconds, or combinations

## File Storage

Reminders are stored in:
```
.minecraft/reminders.dat
```

This file is automatically created and managed by the mod. You don't need to edit it manually.

## Troubleshooting

### Reminder doesn't trigger
- Check that you're in-game (not in a menu)
- Verify the time format is correct
- Check `.minecraft/reminders.dat` exists

### Sound not playing
- Check your Minecraft sound settings
- Ensure sounds are enabled in game settings
- The mod uses the note.pling sound

### Can't dismiss reminder
- Try clicking the dismiss button
- Try pressing Enter or Escape
- If stuck, rejoin the world

## Advanced Usage

### Multiple Task Reminders
```
/remindme Task1 30m
/remindme Task2 1h
/remindme Task3 2h auto
```
All three reminders will work independently.

### Task Names with Underscores
Use underscores for multi-word tasks:
```
/remindme Check_Your_Crops 45m auto
```

### Very Long Reminders
```
/remindme Long_Term_Task 24h
```
Sets a reminder for 24 hours (1 day).
