# UniversalGraves — Modified Source
## Changes from original (3.12.0+26.2)

### 1. Protection Time → 5 minutes
**File:** `src/main/java/eu/pb4/graves/config/Config.java`
**Line 51:** `protectionTime = 300` (was 900 / 15 minutes)
Other players can loot your grave after 5 minutes instead of 15.

### 2. Free Instant Teleport Button
**File:** `src/main/java/eu/pb4/graves/ui/GraveGui.java`
- Added `getFreeInstantTeleport()` method — slot 3 in the grave GUI
- Button label rendered in enchanted-table (alt) font: "TP" in purple
- Bypasses ALL cost checks and permission nodes
- Teleports instantly (0 tick delay) — no timer, no movement check
- Works cross-dimension
- Grants 2 seconds invulnerability on arrival
- Only visible to the grave owner (hasAccess check preserved)
- No OP required, no cheats required, works for any player

## How to build
Requires Java 25 + Gradle. From the source/ directory:
```
./gradlew build
```
Output jar: `build/libs/graves-3.12.0+26.2.jar`

## Pre-built jar
The patched jar (bytecode-patched, no recompile needed) is:
`graves-3_12_0_26_2-freetp.jar`
