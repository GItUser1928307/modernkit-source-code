# ModernKit — Codex Project Context and Working Instructions

## 1. Purpose

This file provides persistent project context and instructions for Codex.
Read it before inspecting or modifying the repository.

This handoff is based on the project history available to ChatGPT. It is
not a verbatim transcript of every previous conversation. The repository,
Git history, and current build output remain the authoritative sources.

Do not invent missing implementation details or claim that a feature is
complete without verifying it.

## 2. Project identity

- Project: ModernKit
- Repository: GitUser1928307/urban-robot
- Working directory: /workspaces/bedrock-mod
- Minecraft: Java Edition 1.16.5
- Mod loader: Fabric
- Language: Java
- Target bytecode: Java 8
- Main package: com.example.modernkit
- Development environment: GitHub Codespaces
- Build system: Gradle
- Known build command: ./gradlew build

## 3. Overall project vision

ModernKit is a Minecraft 1.16.5 Fabric mod that brings selected modern
Minecraft equipment and mechanics into the older version.

The project contains or has been developing:

1. A Bedrock Pickaxe.
2. A Mace with custom smash-attack mechanics.
3. A collection of modern-style spears with associated mechanics.
4. Supporting recipes, enchantments, loot, advancements, sounds,
   particles, and animations.

The goal is to implement these features properly rather than create
visual placeholders.

Preserve the existing project architecture and compatibility.

## 4. Spear project

The spear backport is a major part of the project.

The planned feature set includes:

- Six material variants.
- A Lunge enchantment.
- Spear charging mechanics.
- Spear combat behavior.
- Appropriate animations.
- Sounds.
- Particles.
- Crafting recipes.
- Smithing recipes where appropriate.
- Loot-table integration.
- An advancement.

These are the known requirements. The exact implementation details
and completion status must be established by inspecting the source.

Do not assume all six variants or all supporting features are complete.

When investigating the spear implementation, inspect:

- Item registration.
- Item classes.
- Material statistics.
- Attack and charge behavior.
- Enchantment registration and compatibility.
- Client-side rendering and animations.
- Sound event registration.
- Particle registration and spawning.
- Recipe JSON files.
- Loot-table JSON files.
- Advancement JSON files.
- Language translation files.
- Any mixins or other compatibility code.

Check consistency between registry identifiers, resource paths,
translation keys, and Java references.

## 5. Mace project

The project has an existing Mace implementation.

Historical design decisions included:

- MaceItem should extend ToolItem rather than SwordItem.
- The reason was to avoid sword sweep behavior.
- Intended statistics were 6 attack damage, 0.6 attack speed,
  and 500 durability.
- Breeze Rod was intended as a repair ingredient.
- A smash-attack implementation was planned using a mixin.
- The planned falling trigger was greater than 1.5 blocks.
- Additional damage tiers were planned.
- The planned smash radius was 3.5 blocks.
- Fall damage was intended to be reset or handled by the smash mechanic.

These are historical design notes, not proof that the current
implementation matches them.

Inspect the current Mace code before changing it. Preserve working
behavior and verify the actual implementation against these notes.

Do not rewrite the Mace system without first explaining the reason.

## 6. Bedrock Pickaxe project

The Bedrock Pickaxe is an existing part of ModernKit.

The project originally included work on a Fabric 1.16.5 Bedrock Pickaxe,
using the Yarn mappings and the project's existing Java and Gradle setup.

Inspect its current implementation, registration, statistics,
durability, recipes, translations, and assets before making changes.

Do not assume its current behavior from historical descriptions alone.

## 7. Confirmed build history

A recent build failed because the main mod class did not expose
ModernKitMod.LOGGER.

The reported locations were:

- advancement/MobKabobAdvancement.java, around line 35.
- loot/SpearWorldLoot.java, around line 87.

The repair added the following imports to ModernKitMod.java:

    import org.apache.logging.log4j.LogManager;
    import org.apache.logging.log4j.Logger;

And the following logger declaration:

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

After this repair, the user reported a successful build:

    BUILD SUCCESSFUL

This is the latest confirmed build result in the available history.
It does not guarantee that the current working tree is unchanged.

Verify the current source and Git state before relying on this result.

## 8. Technical compatibility requirements

These requirements are important:

- Keep Minecraft at 1.16.5.
- Keep Fabric as the mod loader.
- Maintain Java 8 bytecode compatibility.
- Do not upgrade Minecraft or dependencies without explicit approval.
- Use the project's actual mappings and dependency versions.
- Use APIs available in the target version.
- Do not accidentally introduce APIs from newer Minecraft versions.
- Follow existing registration and resource conventions.
- Preserve existing working functionality.
- Avoid unnecessary dependencies.
- Avoid unrelated refactoring.
- Do not silently replace existing systems with different architectures.

If a requested feature requires a newer API, investigate a compatible
1.16.5 implementation before proposing an upgrade.

## 9. Repository inspection procedure

Before editing anything:

1. Read this AGENTS.md file.
2. Read any other applicable AGENTS.md files.
3. Inspect the current working directory.
4. Inspect the repository structure.
5. Run git status --short.
6. Review the current Git diff.
7. Inspect relevant source files and resources.
8. Inspect build.gradle, settings.gradle, gradle.properties,
   and other relevant project configuration.
9. Identify the exact task and its dependencies.
10. Report what you found before modifying files.

Do not overwrite the user's uncommitted work.

Do not assume that a clean build from an earlier conversation means
the current checkout is clean.

## 10. Build and testing procedure

The known build command is:

    ./gradlew build

Run it from the repository root.

When a build fails:

- Read the complete error output.
- Identify the first meaningful failure.
- Inspect the relevant source and configuration.
- Find the underlying cause.
- Make a focused repair.
- Run the build again.
- Report the actual result.

Do not suppress errors simply to make the build pass.

A successful compilation does not automatically prove that gameplay,
recipes, loot, advancements, sounds, particles, or rendering work.

Use additional validation when available.

## 11. Java implementation rules

When changing Java code:

- Follow the existing package structure.
- Follow the project's naming conventions.
- Keep Java 8 compatibility.
- Avoid unsupported language features.
- Use the project's existing Fabric and Minecraft APIs.
- Avoid unnecessary class duplication.
- Keep responsibilities clear.
- Handle client-only code appropriately.
- Avoid introducing server/client class-loading problems.
- Preserve existing registrations and identifiers.

Before adding a new class, inspect whether an equivalent class already
exists.

Before changing a registry identifier, search for all references to it.

## 12. Resource implementation rules

When changing JSON resources or assets:

- Follow the existing directory layout.
- Validate JSON syntax.
- Check every referenced item and identifier.
- Check translation keys.
- Check recipe ingredients and outputs.
- Check loot-table references.
- Check advancement criteria and rewards.
- Avoid missing textures or sound references.
- Avoid inconsistent capitalization and naming.

Do not create placeholder resources and describe them as finished assets.

## 13. Combat and gameplay rules

For weapons and combat mechanics, inspect existing implementations
before changing behavior.

Consider:

- Attack damage.
- Attack speed.
- Attack cooldown.
- Durability consumption.
- Enchantment compatibility.
- Entity damage.
- Knockback.
- Player movement.
- Fall damage.
- Server-side authority.
- Client-side animation and effects.
- Multiplayer behavior.

Avoid accidentally changing unrelated weapon behavior.

Do not assume that a mechanic works correctly just because its code
compiles.

## 14. Git safety

Never discard user changes.

Do not run destructive commands such as:

- git reset --hard
- git clean -fd
- git checkout -- .
- Force-push operations

unless the user explicitly requests the particular operation and
understands its consequences.

Inspect Git status before and after significant changes.

Do not commit or push changes unless the user asks.

## 15. Communication preferences

The user prefers practical, direct technical assistance.

When asked to provide code:

- Provide complete code when appropriate.
- Avoid unexplained fragments.
- Make commands copyable.
- Identify the terminal and directory where commands should run.
- Explain important changes.
- Do not claim that something was tested if it was not.

When continuing project development:

- Establish the actual repository state first.
- Explain what is complete.
- Explain what is incomplete.
- Identify uncertainty.
- Propose the next safe step.
- Avoid unnecessary questions when the repository already contains
  the information needed.
- Do not start editing before explaining findings when the task
  requires investigation.

Do not use excessive celebratory language or claim progress that
has not been verified.

## 16. Current starting point

The last confirmed successful build followed the logger repair
described in Section 7.

The next task is to inspect the actual repository and determine:

- Which spear features are implemented.
- Which spear features are incomplete.
- Whether all six material variants exist.
- Whether Lunge is registered and functional.
- Whether charging and combat behavior are implemented.
- Whether sounds, particles, and animations are connected.
- Whether recipes and loot integration are present.
- Whether the advancement is valid.
- Whether the Mace implementation matches its intended behavior.
- Whether the Bedrock Pickaxe remains functional.
- Whether the project builds successfully now.

Do not guess which development stage comes next.

First inspect and report the current state. Then propose the next
safe development step.

## 17. Historical information limitations

This document preserves the project details available in the
conversation context. It does not contain every previous message,
every stage checklist, every source file, or every debugging log.

If an exact historical requirement is needed but is not recorded
here, do not fabricate it.

Use the repository, Git history, and any transcript supplied by
the user to recover the missing information.

## 18. Instructions to Codex

Treat this file as persistent project guidance.

Start by inspecting the repository and its Git status.

Do not modify any files yet.

Compare the actual project with the goals and history documented
here. Report:

1. The current repository structure.
2. The current Git status.
3. The actual state of the spear implementation.
4. The actual state of the Mace implementation.
5. The actual state of the Bedrock Pickaxe.
6. Any build or compatibility problems found.
7. The next recommended development step.

Wait for the user's approval before making changes.
