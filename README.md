Creative Blocks
===============

A Minecraft mod that allows specified blocks to be placed as if the player was in creative mode

### Current features:
- Config for whitelisting blocks as 'creative' (see config/CreativeBlocks/default.json after running Minecraft once with the mod installed)
- Whitelisted blocks do not get used up when placed (like in creative)
- Whitelisted blocks do not drop anything when broken (like in creative)
- Whitelisted blocks get broken in one hit (like in creative)

### Building The Mod
1. Clone this repository
2. If you have [Gradle](http://www.gradle.org/) installed, open a command line in the cloned directory and execute: ```gradle build```. To give the build a version number, use ```gradle build -Pversion=<version>``` instead (example: ```gradle build -Pversion=1.0.0```)
 * If you don't have Gradle installed, you can use [ForgeGradle](http://www.minecraftforge.net/forum/index.php?topic=14048.0)'s gradlew/gradlew.bat instead