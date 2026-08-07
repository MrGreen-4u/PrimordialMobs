# Primordial Mobs

A humble add-on for [Alex's Caves](https://github.com/AlexModGuy/AlexsCaves) (Forge 1.20.1).

Alex's Caves is required and owns every creature, block, item and asset. This add-on layers a
few touches on top of its Primordial Caves: vanilla-style creature names in 13 languages
(Grazer, Logger, Roarer, Drifter, Stealer, Rammer, Scorcher, Roarerzilla), a configurable
recoloured-variant chance for the dinosaurs and the Sniffer, taming/riding/sitting for the
Grazer and the Logger, a reworked companion Sniffer, prehistoric relics and dinosaur eggs
brushed out of vanilla suspicious sand and gravel, and Trilocaris spawns in the Lush Caves.
At the Alex's Caves authors' request it reuses none of their assets — it ships only its own
additions (the rename overlay, the variant textures, the sitting poses and the glue code).

See [PrimordialMobs.md](PrimordialMobs.md) for the player-facing description and
[REFACTOR_PLAN.md](REFACTOR_PLAN.md) for the 3.0.0 architecture.

## Requirements

- Minecraft 1.20.1, Forge 47.1.3+
- Citadel 2.6.0+
- Alex's Caves 2.0+ (hard dependency; tested against 2.0.2)

## Building

Use Java 17 and run:

```text
./gradlew build
```

Alex's Caves is a compile *and* dev-runtime dependency, resolved from `libs/`. Download
Alex's Caves 2.0.2 from
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/alexs-caves/files/5848216) and place
the JAR at `libs/alexscaves-full-2.0.2.jar` before building. Third-party JARs in `libs` are
intentionally excluded from this repository.

The distributable JAR is created in `build/libs`.

Tooling (both re-runnable, both read the Alex's Caves jar in `libs/`):

- `tools/generate_rename_pack.py` regenerates the rename overlay pack
  (`src/main/resources/resourcepacks/primordial_compat`).
- `tools/recolor_variants.py` regenerates the Stealer and Rammer variant textures with
  model-aware masking.

Tip: the dev `runClient` logs in as the offline user "Dev", which always renders the default
Steve skin. To see your own skin, set `mc_username`/`mc_uuid` in `gradle.properties` (see
`build.gradle`).

## Configuration

`config/primordialmobs-general.toml`: `renames.rename_mobs`,
`alternative-textures.{enabled,chance}` (default 15%), and
`brushing.{relic_chance,egg_chance}` (defaults 8% / 2%).

## Credits and license

Primordial Mobs is maintained by MrGreen_4u. It is an add-on for, and a derivative work of,
[Alex's Caves](https://github.com/AlexModGuy/AlexsCaves) by Alexthe668 and Noonyeyz, released
under the GPL-3.0; that attribution is kept because the licence requires it and because the
add-on exists entirely on top of their work. Requires Citadel by Alexthe666.

This project is distributed under the
[GNU General Public License v3](LICENSE).
