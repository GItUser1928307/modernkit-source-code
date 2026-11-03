# Bedrock Pickaxe

A Fabric mod for Minecraft Java 1.16.5 that adds a pickaxe able to mine bedrock in survival mode.

## Features

- Bedrock Pickaxe: durability 4000, fireproof, repairable with bedrock
- Mines bedrock in survival (about 3 seconds per block), on both singleplayer and servers
- Bedrock drops itself when mined with the pickaxe
- Normal tools still cannot break bedrock
- Works alongside Sodium and Iris

## Recipe

    B B B
      S
      S

B = Bedrock, S = Stick

## Requirements

- Minecraft 1.16.5
- Fabric Loader 0.10.0 or newer
- Fabric API 0.42.0+1.16 (or any 1.16.5 build)
- Java 8 or newer to play

## Building

Requires Java 17 to build (the jar itself targets Java 8):

    ./gradlew build

The jar is created in build/libs/. Every push also builds it automatically; download it from the Actions tab.

## Warning

Mining the bottom bedrock layer of the Overworld will drop you into the void.
