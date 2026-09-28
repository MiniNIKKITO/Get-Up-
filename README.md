# Get Up!

Fabric client mod for Minecraft 26.2.

Version 1.3.3 continues the stable Get Up! release line with the custom death presentation, post-respawn exposure recovery, independent HUD fade, and Mod Menu configuration.

The mod bundles its shader library dependency so Iris is not required.

Build with JDK 25:

```text
gradle build
```

The generated mod jar will be in `build/libs/`.

## Configuration

Open the mod in **Mod Menu** and press its configuration button. The following timings can be edited in seconds:

- You died delay
- Get up delay
- Black-to-white transition
- Exposure recovery
- Vanilla Disc 13 death music (loaded directly from Minecraft)

The configuration is saved to `config/getup.properties` and persists between launches.

The existing chat commands remain available:

- `/getup set you_died <seconds>`
- `/getup set get_up <seconds>`
- `/getup set white <seconds>`
- `/getup set exposure <seconds>`
- `/getup get`
- `/getup reset`

The default timings are 2s / 5s / 2s / 2s respectively. The Disc 13 pitch transition follows the black-to-white transition, and both Disc 13 fade-out and vanilla game-audio fade-in use the full exposure-recovery duration.


## Version 1.3.0
- Added an ON/OFF toggle for the entire mod in Mod Menu configuration.
- On singleplayer respawn, all currently loaded hostile mobs (`Monster`) are discarded immediately. Passive mobs are untouched.

## Version 1.3.3
- Added Minecraft's vanilla Disc 13 as the death-screen music without bundling a copy of the audio file.
- Disc 13 starts immediately on death at normal pitch and volume.
- If the disc finishes before Get Up! is pressed, it remains silent and does not restart.
- When Get Up! is activated, Disc 13's pitch rises to Minecraft's maximum effective pitch over the black-to-white transition.
- When maximum white is reached, Disc 13 fades out over half of the configured exposure-recovery duration.
- At the same moment, normal Minecraft audio fades in over the full configured exposure-recovery duration.
