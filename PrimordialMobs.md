# Primordial Mobs

**Initial Note: this add-on has been approved by the creators of its base mod, [Alex's Caves](https://www.curseforge.com/minecraft/mc-mods/alexs-caves).**

A modest add-on for the Primordial Caves content from [Alex's Caves](https://www.curseforge.com/minecraft/mc-mods/alexs-caves): vanilla-style mob names (adjective-like names inspired by the "Sniffer"), rare colour variants, two new mounts, a rework of the Sniffer, and the possibility of finding Primordial Cave content inside suspicious blocks.

Alex's Caves is responsible for everything you will find here: the Primordial Caves, their creatures, models, animations, sounds, items, and mechanics. Primordial Mobs simply adds a number of small changes and improvements on top of that content, and it will not run without Alex's Caves installed.

Requires **Forge 1.20.1 · [Citadel](https://www.curseforge.com/minecraft/mc-mods/citadel) 2.6.0+ · [Alex's Caves](https://www.curseforge.com/minecraft/mc-mods/alexs-caves) 2.0+**.

---

## Vanilla-style names

The creatures from the Primordial Caves receive simpler, more "vanilla-style" names inspired by the **Sniffer**, across all 13 languages supported by Alex's Caves. Anything whose name depends on those creatures is renamed accordingly, including egg blocks, spawn eggs, sound subtitles, cave paintings, advancements, Extinction Spear enchantment descriptions, and the guide book.

| Alex's Caves    | Primordial Mobs |
| --------------- | --------------- |
| Grottoceratops  | **Grazer**      |
| Relicheirus     | **Logger**      |
| Tremorsaurus    | **Roarer**      |
| Subterranodon   | **Drifter**     |
| Vallumraptor    | **Stealer**     |
| Tremorzilla     | **Roarerzilla** |
| Atlatitan       | **Rammer**      |
| Luxtructosaurus | **Scorcher**    |

The new names work as a resource overlay on top of Alex's Caves' own translations. **This entire feature can also be disabled from the config.**

## Rare texture variants

A configurable percentage of newly spawned animals is born with an **exclusive colour variant** (**15% by default**, regardless of how they appear: natural spawning, hatching, spawn eggs, etc.).

Every appearance has its own variant, including the retro and tectonic forms triggered by the Amber Curiosity and Tectonic Shard, as well as the baby Grottoceratops and elder Vallumraptor.

## Grottoceratops and Relicheirus behaviour changes

This mod makes the Grottoceratops and Relicheirus tameable. More specifically:

* **You can tame them with a Tree Star** (roughly one in three attempts succeeds). Shift-clicking cycles between *wandering -> staying -> following*; when told to stay, they rest using a proper **sitting pose** created specifically for this mod.
* **You can ride them**, with the saddle positioned on their back rather than near the neck. The player's riding position follows the creature's gait, so you naturally move up and down with its body.
* The Relicheirus **actually fishes**: cod, salmon, tropical fish, and tadpoles, rather than only Trilocaris, swallowing them whole using the same animation. It swims faster than it walks, keeps its arms lowered while being ridden, and cannot be mounted while a Seething Stew has it busy knocking down trees.
* Feeding a Grottoceratops **a Serene Salad** grants **Haste I for four minutes**.

Everything else about these creatures remains exactly as it is in Alex's Caves.

## The Sniffer, reworked

* **It digs up prehistoric plants** such as Tree Star, Curly Fern, Fiddlehead, Pewen Sapling, Pine Nuts, and others, mixed in with vanilla torchflower and pitcher plant seeds. No Sniffer, no prehistoric garden.
* **Each prehistoric food mixture has a different effect on it:**

  * A **Serene Salad** can be used to **tame it**: roughly one in three attempts succeeds, just like giving one to a relaxed Stealer. Afterwards, shift-clicking cycles between *wandering -> staying -> following*. When told to stay, it lies down using its own resting pose, and giving another salad to an already-tamed Sniffer calms it down and makes it rest again.
  * A **Seething Stew** sends it into a **rage**: for one minute, it raises its snout and headbutts nearby hostile mobs, launching them away.
  * A **Primordial Soup** rewards patience by **halving the remaining cooldown** before its next sniff. Since vanilla Sniffers take a long time between digs, this helps get them moving again, and it also makes a resting Sniffer stand back up.
* It drops **Tough Hide**, can use the **retro and tectonic** appearances through the Amber Curiosity and Tectonic Shard, has its own colour variants, and **spawns naturally in the Primordial Caves**, where it behaves purely as a passive animal.

## Brush

* Any vanilla archaeology site, by using a brush on **suspicious gravel or suspicious sand**, can contain something prehistoric. There is an **8%** chance for a suspicious block to yield a common item from Alex's Caves, such as a Heavy Bone, Amber Curiosity, or one of the four pottery sherds, a **2%** chance to yield a **Tectonic Shard**, and a **2%** chance to yield **the egg block of one of the five mobs** (excluding the Atlatitan), which can then be hatched. Vanilla Sniffer Egg loot is not modified. Both probabilities can be configured (the Tectonic Shard shares the egg chance).

## Trilocaris

* The **Trilocaris** can also spawn in **Lush Caves**, in water above clay, just like an axolotl.


## Configuration

The config file (`config/primordialmobs-general.toml`) allows you to:

| Option | Default | What it does |
|---|---|---|
| `renames.rename_mobs` | `true` | The vanilla-style names, everywhere they appear |
| `alternative-textures.enabled` | `true` | Whether new animals can roll a recoloured variant |
| `alternative-textures.chance` | `0.15` | The variant chance |
| `brushing.relic_chance` | `0.08` | Relics from brushed suspicious sand/gravel |
| `brushing.egg_chance` | `0.02` | Dinosaur egg blocks (and, separately, the Tectonic Shard) from brushed suspicious sand/gravel |

---

## Credits & license

Primordial Mobs is an add-on for, and a derivative work of, **Alex's Caves** by **Alexthe668** and
**Noonyeyz** (GPL-3.0),  who approve of this project. At the authors' request it reuses none of their assets: the base mod is
required, owns all of its content, and this add-on ships only its own additions - the rename
overlay, the recoloured variant textures, the sitting poses and the glue code. Requires Citadel by
Alexthe668.
