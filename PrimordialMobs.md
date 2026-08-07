# Primordial Mobs

**A humble add-on for [Alex's Caves](https://www.curseforge.com/minecraft/mc-mods/alexs-caves):
vanilla-style names, rare recoloured hides, two new mounts, a companion Sniffer, and a reason to
carry a brush everywhere.**

Alex's Caves owns everything you will meet — the Primordial Caves, the creatures, their models,
animations, sounds, items and mechanics. Primordial Mobs adds nothing of its own to compete with
that: it layers a handful of touches on top, and without Alex's Caves installed it simply does not
run.

Requires **Forge 1.20.1 · [Citadel](https://www.curseforge.com/minecraft/mc-mods/citadel) 2.6.0+ ·
[Alex's Caves](https://www.curseforge.com/minecraft/mc-mods/alexs-caves) 2.0+**.

---

## Vanilla-style names

The Primordial Caves creatures take simple, vanilla-sounding names, in all 13 languages Alex's
Caves ships — and everything named after them follows: egg blocks, spawn eggs, sound subtitles,
cave paintings, advancements, the Extinction Spear enchantment descriptions and the guide book.

| Alex's Caves | Primordial Mobs |
|---|---|
| Grottoceratops | **Grazer** |
| Relicheirus | **Logger** |
| Tremorsaurus | **Roarer** |
| Subterranodon | **Drifter** |
| Vallumraptor | **Stealer** |
| Tremorzilla | **Roarerzilla** |
| Atlatitan | **Rammer** |
| Luxtructosaurus | **Scorcher** |

The renames are a resource overlay over Alex's Caves' own translations, so each language keeps its
own grammar ("Huevo de Roarer", "Rammer蛋") — and a single config switch turns all of them off.

## Skins worth hunting for

A configurable share of newly arriving animals — **15% by default**, on every way an animal can
arrive: hatching, breeding, spawn eggs, natural spawns, even the Drifter roosts — are born as an
**exclusive recoloured variant**. Every hide has one, including the retro and tectonic looks the
Amber Curiosity and Tectonic Shard summon, and including the baby Grazer and the elder Stealer.

New in 3.0: the **Rammer** joins the lottery with three recolours of its own — a purple-toned
standard hide, a green-and-lime retro hide, and a hornet-toned tectonic hide: near-black scales
with its vein web glowing hot yellow. And the Stealer's six recolours were redone with care for what each pixel actually
is on the model: claws now read as keratin, teeth stay ivory, eyes stay eyes, and the tectonic
Stealer keeps its magma claws.

## The Grazer and the Logger, tamed

Alex's Caves leaves its two gentle giants wild; this add-on gives them the same treatment its
predators already enjoy:

* **Tame them with a Tree Star** (about one in three works). Shift-click cycles
  *wander → stay → follow*; a parked one genuinely parks — it does not slide when shoved, its
  hitbox drops with its body, and it rests in a real **sitting pose** made for this add-on.
* **Ride them**, with the saddle on the back rather than up the neck, and the seat tracking the
  gait so you rise and fall with the body.
* The **Logger fishes for real** — cod, salmon, tropical fish and tadpoles, not only the
  Trilocaris, swallowed whole with the same animation — swims faster than it walks, keeps its
  arms down while ridden, and cannot be mounted while a Seething Stew has it felling trees.
* Feeding a **Serene Salad to a Grazer** grants **Haste I for four minutes**.

Everything else about them is Alex's Caves' own, untouched.

## The Sniffer, rewritten

* **It digs up the prehistoric plants** (Tree Star, Curly Fern, Fiddlehead, Pewen Sapling, Pine
  Nuts and friends), mixed in with the vanilla torchflower and pitcher seeds. No Sniffer, no
  prehistoric garden.
* **Each prehistoric mixture does one distinct thing to it:**
  * a **Serene Salad** is how you **tame** one — about one in three works, just like handing one
    to a relaxed Stealer. Shift-click then cycles *wander → stay → follow*; told to stay it lies
    down in its own rest pose, and a salad handed to a tame one calms it back into resting.
  * a **Seething Stew** makes it **seethe**: for a minute it throws its snout in the air and
    headbutts the hostile mobs around it across the room.
  * a **Primordial Soup** is patience food: it **halves the remaining wait** until the next sniff
    (vanilla makes a Sniffer wait ages between digs), and stands a resting one up to get on with
    it.
* It drops **Tough Hide**, wears the **retro and tectonic** hides via the Amber Curiosity and
  Tectonic Shard, has its own recoloured variants — and **spawns naturally in the Primordial
  Caves**, where it is only a passive animal.

## The brush, and the Trilocaris

* Every vanilla archaeology site can turn up something prehistoric: **8%** of brushed suspicious
  blocks yield a relic from Alex's Caves — a Heavy Bone, a Tectonic Shard, an Amber Curiosity or
  one of the four pottery sherds — and **2%** yield **the egg block of one of the five dinosaurs**,
  to carry home and hatch. A successful vanilla Sniffer Egg roll is never replaced. Both
  probabilities are config options.
* The **Trilocaris** also spawns in **Lush Caves** — in water over clay, exactly like an axolotl
  and half again as common — so there is a reason to explore a lush cave before you go dinosaur
  hunting.

## Configuration

One small file, `config/primordialmobs-general.toml`:

| Option | Default | What it does |
|---|---|---|
| `renames.rename_mobs` | `true` | The vanilla-style names, everywhere they appear |
| `alternative-textures.enabled` | `true` | Whether new animals can roll a recoloured variant |
| `alternative-textures.chance` | `0.15` | The variant chance |
| `brushing.relic_chance` | `0.08` | Relics from brushed suspicious sand/gravel |
| `brushing.egg_chance` | `0.02` | Dinosaur egg blocks from brushed suspicious sand/gravel |

---

## Credits & license

Primordial Mobs is an add-on for, and a derivative work of, **Alex's Caves** by **Alexthe668** and
**Noonyeyz** (GPL-3.0). At the authors' request it reuses none of their assets: the base mod is
required, owns all of its content, and this add-on ships only its own additions — the rename
overlay, the recoloured variant textures, the sitting poses and the glue code. Requires Citadel by
Alexthe666.
