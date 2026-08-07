# REFACTOR — Primordial Mobs becomes a true Alex's Caves add-on

Date: 2026-08-07. Driver: the Alex's Caves authors asked that this mod not reuse their assets.
We may add our own textures/animations **on top of** their content, but the add-on must not be
playable without the original mod installed.

## Decision

Delete the standalone mode entirely. The mod becomes the compat overlay only, with
`alexscaves` a **mandatory** dependency in mods.toml. This was already the better half of the
codebase: COMPAT_PLAN.md (2026-07-26) measured that four of the five dinosaurs were line-identical
clones of upstream, and the compat overlay reproduces every player-visible feature through ~12
mixins + events against Alex's Caves' own entities. Nothing the player sees changes; what
disappears is the cloned code and assets that made the mod playable alone.

## What stays (the mod's own work)

| Piece | Where |
|---|---|
| Taming/riding/sitting the Grazer & Logger | `mixin/compat/*EntityMixin`, `compat/CompatEvents` |
| Sitting poses (Grotto/Relicheirus), ridden cadence, held-fish fix | `mixin/compat/client/*ModelMixin`, `RelicheirusHeldFishLayerMixin` |
| 15% recoloured-variant system + variant textures (5 dinos + Sniffer, now + Atlatitan) | `DinosaurEntityMixin` (PMRecolorable), `CompatEvents`, renderer mixins, `assets/alexscaves/textures/entity/*_variant.png` |
| The whole Sniffer rework (taming, sit pose, digging, mixtures, hide drop, skins) | `mixin/Sniffer*`, `server/event/SnifferEvents`, `server/entity/util/*`, sniffer textures |
| Brushing loot (relics + dinosaur eggs from vanilla archaeology) | `PrimordialBrushingLootModifier` + GLM data |
| Trilocaris in Lush Caves (axolotl-style, on clay) + Sniffer spawns in Primordial Caves | `PMBiomeModifierRegistry`, `TrilocarisSpawns`, biome_modifier data |
| Logger fishes everything in `#alexscaves:relicheirus_fishes` | tag data + `RelicheirusMeleeGoalMixin` + entity mixin |
| Vanilla-style renames in 13 languages + guide/advancement/subtitle derivations | `resourcepacks/primordial_compat` + `CompatLangPack` + `tools/*.py` |

## What goes

- **All 602 byte-identical Alex's Caves asset clones** and ~140 modified derivatives that only
  served standalone registration (blockstates, models, sounds+sounds.json, textures, block/entity
  loot tables, recipes, worldgen, vanilla tag additions, `assets/minecraft/texts/end.txt`,
  `assets/minecraft/atlases/armor_trims.json`).
- **All standalone Java**: `PMEntityRegistry/PMBlockRegistry/PMItemRegistry/...`, the cloned
  entities/goals/models/renderers/particles/shaders, `CommonEvents`, enchantments, blocks, items,
  boat, monolith, network channel + both messages, keybind, creative tab, standalone mixins
  (`FoodDataMixin`, `MobMixin`, `ClientPacketListenerMixin`), the fat `CommonProxy` clone.
- Main-jar `assets/alexscaves/lang/*` (renames live only in the dedicated overlay pack, which is
  deterministic against Alex's Caves' own lang; the jar copy was the standalone path).

Justifications for the not-obvious keeps:
- `PrimordialMixinPlugin` stays even though AC is now mandatory: mixins are applied before Forge
  shows the friendly "missing dependency" screen, so gating compat mixins on AC's presence keeps a
  user who forgot to install Alex's Caves on the clean error screen instead of a mixin crash.
- `data/alexscaves/tags/entity_types/relicheirus_fishes.json` is ours (Alex's Caves has no such
  tag); it only steers our added goal.
- `data/minecraft/loot_tables/entities/sniffer.json` (+ `sniffer_digging.json`) override vanilla,
  not Alex's Caves — they are the Sniffer rework, kept.
- The `*_variant` textures are this mod's recolours (allowed: our own textures on top).

## New in this refactor

1. **Config** (`config/primordialmobs-general.toml` + `-client.toml`):
   - `renames` (client): master switch for the rename overlay pack (mob names, spawn eggs, egg
     blocks, advancements, subtitles, guide book). Gated at the pack `RepositorySource`, with a
     direct TOML read as fallback for the first scan (pack finding runs before Forge loads
     configs).
   - `alternative_textures` + `alternative_texture_chance` (common, default 0.15): gates and tunes
     the recoloured-variant roll (server side; already-rolled animals keep their look).
   - `brushing_relic_chance` (default 0.08) + `brushing_egg_chance` (default 0.02) (common): the
     suspicious sand/gravel probabilities, read by the loot modifier at apply time (JSON keeps only
     the archaeology-table conditions).
2. **Atlatitan → Rammer, Luxtructosaurus → Scorcher** in all 13 locales + every derived string
   (subtitles, advancements, guide pages, egg block, spawn eggs), via the same two-stage tooling.
3. **Atlatitan variants**: `atlatitan_variant` (purple), `atlatitan_retro_variant` (green/lime),
   `atlatitan_tectonic_variant` (ash grey), model-aware recolours; `AtlatitanRendererMixin`;
   `atlatitan` joins the 15% roll (its `AtlatitanEntity extends SauropodBaseEntity extends
   DinosaurEntity`, so the existing PMRecolorable mixin already covers it).
4. **Vallumraptor variant textures** re-derived with per-model-part masks (claws, eyes, teeth).
5. Player-skin/Steve bug: under investigation (separate report).

## Verification

JDK17 compile; headless Forge 47.x server with `alexscaves-full-2.0.2.jar` + Citadel: clean boot
(`Done`, 0 mixin errors with `defaultRequire:1`), RCON checks (summon the five + atlatitan +
trilocaris + sniffer, tags/recipes/GLM resolve, variant NBT roll), plus a boot WITHOUT Alex's
Caves must stop on Forge's missing-dependency screen rather than crash.
