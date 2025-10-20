# Reminder Mod for Minecraft 1.8.9

A Forge mod that reminds you of tasks at certain times with title popups and sounds.

## Features

- **Command**: `/remindme <taskname> <time> [auto]`
  - Set reminders that will alert you when the time has elapsed
  - Time format: `1h`, `30m`, `2h30m`, `45s`
  - Add `auto` to make the reminder recurring
  
- **Persistent Storage**: Reminders are saved even when you close the game
  - If you set a 2h reminder, close the game, and open it 3h later, you'll be alerted immediately
  
- **Big Title Popup**: Shows the task name in large text when triggered
  - Example: "Motes!" will be displayed prominently on screen
  
- **Dismiss Button**: Click the dismiss button or press Enter/ESC to dismiss the alert
  
- **Repeating Sound**: Plays a notification sound every 2 seconds until dismissed
  
- **Recurring Reminders**: When enabled with `auto`, a new timer automatically starts after dismissal

## Usage Examples

```
/remindme Motes 2h        - Reminds you about "Motes" in 2 hours
/remindme Farm 30m auto   - Reminds you about "Farm" every 30 minutes
/remindme Boss 1h30m      - Reminds you about "Boss" in 1 hour 30 minutes
```

## Building

This mod is built for Minecraft 1.8.9 using Forge Gradle.

### Prerequisites
- Java 8 (JDK 1.8)
- Gradle 2.12+

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/sangambartaula/reminder.git
   cd reminder
   ```

2. Set up the workspace:
   ```bash
   ./gradlew setupDecompWorkspace
   ```

3. Build the mod:
   ```bash
   ./gradlew build
   ```

4. The compiled mod will be in `build/libs/reminder-1.0.jar`

## Installation

1. Install Minecraft Forge 1.8.9
2. Copy the built jar file to your `.minecraft/mods` folder
3. Launch Minecraft with the Forge profile

## Technical Details

- **Mod ID**: reminder
- **Version**: 1.0
- **Minecraft Version**: 1.8.9
- **Forge Version**: 11.15.1.2318-1.8.9

## File Structure

- `ReminderMod.java` - Main mod class with initialization
- `ReminderCommand.java` - Command handler for `/remindme`
- `Reminder.java` - Data model for individual reminders
- `ReminderManager.java` - Manages reminder storage and checking
- `ReminderEventHandler.java` - Handles game events and triggers reminders
- `ReminderOverlay.java` - GUI overlay for displaying reminder alerts

## Data Storage

Reminders are stored in `reminders.dat` in your Minecraft directory and persist across game sessions.

## License

See LICENSE file for details.
