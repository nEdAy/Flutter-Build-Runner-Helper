# Flutter-Build-Runner-Helper
A plugin for Android Studio that speeds up your day to day flutter development.
Flutter Build Runner Helper, to make it easier to execute build_runner commands.

Adds the following build_runner commands to Android Studio:
 - Build : flutter packages pub run build_runner build
 - Rebuild : flutter packages pub run build_runner build --delete-conflicting-outputs
 - Watch : flutter packages pub run build_runner watch
 - Clean : flutter packages pub run build_runner clean
 
 - Kill Gradle : Kill Gradle if a current task is running
 - Kill Flutter : Kill Flutter if a current task is running

There are two basic ways to invoke a command:
 - Click the action button in the Toolbar.
 - Use the shortcut key.