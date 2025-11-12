Icon Placeholders
==================

This directory should contain the app launcher icons for hdpi density.

To add icons:
1. Create or design an icon (512x512 recommended source)
2. Use Android Studio's Image Asset tool:
   - Right-click on res → New → Image Asset
   - Choose Launcher Icons (Adaptive and Legacy)
   - Configure your icon
   - Android Studio will generate all densities automatically

Or use online tools:
- https://romannurik.github.io/AndroidAssetStudio/
- https://icon.kitchen/

Required files:
- ic_launcher.png (legacy icon)
- ic_launcher_round.png (round icon variant)

Or for adaptive icons:
- ic_launcher_background.xml or .png
- ic_launcher_foreground.xml or .png

Until icons are added, Android will use default system icons.
