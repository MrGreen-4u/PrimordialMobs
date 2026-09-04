# Primordial Mobs

A humble **add-on** for [Alex's Caves](https://github.com/AlexModGuy/AlexsCaves) (Forge 1.20.1) 
**approved by the authors** of the original mod.

**Alex's Caves is required and owns every mob, block, item and asset**. This add-on layers a
few touches on top of its Primordial Caves: vanilla-style creature names
(Grazer, Logger, Roarer, Drifter, Stealer, Rammer, Scorcher, Roarerzilla), a configurable
recoloured-variant chance for the dinosaurs and the Sniffer, taming/riding/sitting for the
Grottoceratops and the Relicheirus, a reworked Sniffer, prehistoric relics and dinosaur eggs
brushed out of vanilla suspicious sand and gravel, and Trilocaris spawns in the Lush Caves.
At the Alex's Caves authors' request it reuses none of their assets — it ships only its own
additions (the rename overlay, the variant textures, the sitting poses and the glue code).

See [PrimordialMobs.md](PrimordialMobs.md) for the player-facing description.

## Requirements

- Minecraft 1.20.1, Forge 47.1.3+
- Citadel 2.6.0+
- Alex's Caves 2.0+ (hard dependency; tested against 2.0.2)

## Configuration

`config/primordialmobs-general.toml`: `renames.rename_mobs`,
`alternative-textures.{enabled,chance}` (default 15%), and
`brushing.{relic_chance,egg_chance}` (defaults 8% / 2%; the Tectonic Shard uses the egg chance).

## Credits and license

Primordial Mobs is an add-on for, and a derivative work of,
[Alex's Caves](https://github.com/AlexModGuy/AlexsCaves) by Alexthe668 and Noonyeyz, released
under the GPL-3.0; that attribution is kept because the licence requires it and because the
add-on exists entirely on top of their work. Requires Citadel by Alexthe666.

This project is distributed under the
[GNU General Public License v3](LICENSE).
