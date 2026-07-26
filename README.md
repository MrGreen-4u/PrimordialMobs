# Primordial Mobs

Six prehistoric creatures, a reworked Sniffer, and a reason to carry a brush everywhere.

Primordial Mobs rebuilds the Primordial Caves creatures from Alex's Caves as a
vanilla-style archaeology and survival feature for Minecraft Forge 1.20.1. It can
also be installed alongside Alex's Caves, where it acts as a compatibility overlay.

## Requirements

- Minecraft 1.20.1
- Forge 47.1.3 or newer
- Citadel 2.6.0 or newer
- Alex's Caves 2.0 or newer (optional)

## Building

Use Java 17 and run:

```text
gradlew.bat build
```

The optional Alex's Caves compatibility classes are compiled against
`libs/alexscaves-full-2.0.2.jar`. Download Alex's Caves 2.0.2 from
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/alexs-caves/files/5848216)
and place the JAR at that path before building. Third-party JARs in `libs` are
intentionally excluded from this repository.

The distributable JAR is created in `build/libs`.

## Credits and license

Primordial Mobs is maintained by MrGreen_4u and is a derivative work of
[Alex's Caves](https://github.com/AlexModGuy/AlexsCaves), by Alexthe668 and
Noonyeyz. Additional inherited credits are listed in the mod metadata.

This project is distributed under the
[GNU General Public License v3](LICENSE).
