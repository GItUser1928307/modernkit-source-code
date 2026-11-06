# ModernKit

A Fabric mod for Minecraft Java **1.16.5** that backports modern Minecraft items and combat to the old version. It works alongside Sodium and Iris.

## What's inside

### Bedrock Pickaxe
- Mines bedrock in survival (about 3 seconds per block), in singleplayer and on servers
- Bedrock drops itself when mined with it
- 4000 durability, fireproof, repairable with bedrock
- Normal tools still cannot break bedrock

### Mace
- 6 damage, 0.6 attack speed, 500 durability, repaired with a Breeze Rod
- **Smash attack:** after falling more than 1.5 blocks (and not gliding), you get bonus damage: +4 per block for the first 3 blocks, +2 for the next 5, +1 beyond that
- A successful smash resets your fall damage, knocks back nearby mobs, and plays particle and sound effects
- A missed swing keeps you falling
- No sweep attack

### Spears (wooden, stone, golden, iron, diamond, netherite)
- **Jab:** a left-click attack with material-based damage, reach and cooldown
- **Charge:** hold right-click for a kinetic attack with Engaged, Tired and Disengaged stages
- **Lunge** enchantment (levels I to III), exclusive to spears
- Sharpness, Smite, Bane of Arthropods, Knockback, Fire Aspect and Looting work on spears (not Sweeping Edge)
- First- and third-person spear animations, plus effects and sounds
- **Mob Kabob** advancement: hit five mobs in a single charge

### Crafting items
- **Heavy Core**, **Breeze Rod**
- **Wind Charge**: a throwable projectile with custom sounds

## Recipes

| Item | Recipe |
|---|---|
| Bedrock Pickaxe | 3 Bedrock on top, 2 Sticks below |
| Heavy Core | Netherite Ingot in the center, 4 Obsidian up, down, left and right |
| Breeze Rod | Blaze Rod + Phantom Membrane (shapeless) |
| Mace | Heavy Core over Breeze Rod |
| Wind Charge | 1 Breeze Rod makes 4 (shapeless) |

## Requirements

- Minecraft 1.16.5
- Fabric Loader 0.14.0 or newer
- Fabric API 0.42.0+1.16
- Java 8 or newer to play (Java 17 to build)

## Install

Put the jar from `dist/` into your `mods` folder next to Fabric API. Use only one ModernKit jar at a time, and never the `-sources` or `-dev` jars.

## Building

Requires Java 17 and Node.js.

    npm run build:java

This reads the previous jar in `dist/`, bumps the middle version digit (1.4.0 becomes 1.5.0), and runs `./gradlew clean build`. On success, the new jar replaces the old one in `dist/`. On failure, `gradle.properties` is restored and the errors are written to `gradle-failed-build.log`.

You can also build manually with `./gradlew build`; the jar appears in `build/libs/`.

## Changelog

### Added
- Bedrock Pickaxe with bedrock mining and drops
- Mace with the smash attack, particles and sounds
- Heavy Core, Breeze Rod and Wind Charge (with a projectile entity and custom sounds)
- Six spears with jab and charge attacks, the Lunge enchantment and the Mob Kabob advancement
- First- and third-person spear animations
- Automated jar build script (`npm run build:java`) and a GitHub Action that builds on every push

### Changed
- Project renamed to ModernKit (mod id `modernkit`, package `com.example.modernkit`)
- Mace uses a plain tool item, so it no longer performs sword sweep attacks
- Wind Charge texture resized from 512x512 to 32x32

### Fixed
- Bedrock drops its own block through a Mixin, because vanilla skips the loot table for bedrock
- Stale jars in `dist/` no longer cause an outdated mod to load, because the build script replaces them

## Known limitations

- Mace enchantments (Density, Breach, Wind Burst) are not implemented yet
- Mace and spear sounds are built from vanilla sounds and may differ from the modern versions
- Some spear client files are still empty placeholders

## License

MIT
