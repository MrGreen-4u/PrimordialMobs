# COMPAT_PLAN — imponer los mobs y el comportamiento de Primordial Mobs en modo compat

Fecha: 2026-07-26. Contexto: `MOD-PrimordialVanilla/primordialmobs` (Forge 1.20.1, modid `primordialmobs`,
namespace de registro `alexscaves`), con Alex's Caves 2.0.2 instalado (`libs/alexscaves-full-2.0.2.jar`).

**Objetivo del usuario**: que en la Primordial Cave, con AC instalado, los mobs sean los del mod con SUS
mecánicas (Logger pescando peces de verdad y derribando árboles, Rammer con Serene Salad→Prisa I, variantes,
loot) en lugar de los Grottoceratops/Relicheirus de Alex's Caves.

---

## 0. La medición que decide el diseño

Antes de comparar vías hice lo que faltaba en todas las sesiones anteriores: **medir cuánto se diferencian de
verdad nuestros mobs de los de Alex's Caves**. Recipe: descargar el fuente upstream de
`AlexModGuy/AlexsCaves` (rama `main`), normalizar paquetes/prefijos (`com.primordialmobs`↔
`com.github.alexmodguy.alexscaves`, `PM*`↔`AC*`) y `diff -u`. Contrastado además contra el jar de producción
2.0.2 con `javap -p` (la rama `main` podría ir por delante de 2.0.2; no lo va en estas clases).

| Clase | Líneas (upstream / nuestra) | Líneas de diff | Veredicto |
|---|---|---|---|
| `TremorsaurusEntity` (Roarer) | 623 / 623 | **0** | idéntica |
| `VallumraptorEntity` (Stealer) | 688 / 688 | **0** | idéntica |
| `SubterranodonEntity` (Glider) | 675 / 675 | **0** | idéntica |
| `TrilocarisEntity` | 390 / 390 | **0** | idéntica |
| `DinosaurEntity` (base de las 5) | 384 / 429 | 79 | 4 deltas |
| `GrottoceratopsEntity` (Rammer) | 238 / 377 | 159 | 4 deltas |
| `RelicheirusEntity` (Logger) | 374 / 503 | 173 | 5 deltas |

Y en las goals (8 comparadas): `RelicheirusPushTreesGoal`, `RelicheirusNibblePewensGoal`,
`GrottoceratopsEatPlantsGoal`, `GrottoceratopsMeleeGoal`, `AnimalBreedEggsGoal`, `AnimalLayEggGoal` →
**0 diferencias**. `RelicheirusMeleeGoal` → 1 línea lógica (`instanceof TrilocarisEntity` →
`isFishable(target)`). `MobTargetItemGoal` → solo un import muerto.

### Consecuencia inmediata (y contraintuitiva)

**"El Logger derriba árboles" ya funciona hoy en compat**: `RelicheirusPushTreesGoal` está en el jar de
Alex's Caves 2.0.2 (`com/github/alexmodguy/alexscaves/server/entity/ai/RelicheirusPushTreesGoal.class`,
10 873 B) y es idéntica a la nuestra. Lo mismo con el ramoneo de pewens, el pastoreo del Rammer, sus ataques,
sus animaciones, sus sonidos, su loot y sus huevos. Cuatro de los cinco mobs son, línea a línea, el mismo
código.

**Lo que de verdad es "nuestro" y hoy NO está en compat** son 9 deltas concretos:

| # | Delta | Dónde vive en standalone |
|---|---|---|
| D1 | Variante recoloreada 15 % con flag propio `Recolored` | `DinosaurEntity.finalizeSpawn` + synched data |
| D2 | Sentado que no derrapa (anula el delta-movement, para la navegación) | `DinosaurEntity.tick` |
| D3 | Hitbox que encoge al sentarse (`getSittingDimensionScale`: 0.78 Rammer, 0.85 Logger) | `DinosaurEntity.getDimensions` |
| D4 | Sin partículas orbitales del cambio de piel ámbar/tectónico | `DinosaurEntity.handleEntityEvent` |
| D5 | **Rammer y Logger domesticables** con Tree Star (1/3), sit/follow, OwnerHurt targets | `mobInteract` + `registerGoals` |
| D6 | **Rammer y Logger montables y dirigibles** (asiento en el lomo, velocidad, desmontaje) | `canOwnerMount/Command`, `getControllingPassenger`, `getRiddenInput/Speed`, `tickRidden`, `positionRider` |
| D7 | **Rammer: Serene Salad → Prisa I 4 min al jugador** | `GrottoceratopsEntity.onFeedMixture` |
| D8 | **Logger pesca cualquier pez de `#alexscaves:relicheirus_fishes`**, no solo Trilocaris | target goal + `tick` + `RelicheirusMeleeGoal` |
| D9 | Logger un pelín más rápido (0.20→0.22) y sin animaciones de reposo sentado/montado | `createAttributes` + `tick` |

En AC, `DinosaurEntity.canOwnerMount()` y `canOwnerCommand()` devuelven **false** en la base; solo
Tremorsaurus, Subterranodon y Vallumraptor los overridean. Por eso el Grottoceratops y el Relicheirus de AC
no se doman ni se montan — pero **toda la maquinaria (el `mobInteract` con ciclo de órdenes y montura, el
`SitWhenOrderedToGoal`, los mensajes `entity.alexscaves.all.command_N`) ya está en su clase base**.

---

## 1. Vía A — entidades propias con ids `primordialmobs:*` y sustitución de spawns

**Idea**: registrar en compat `primordialmobs:rammer`, `primordialmobs:logger`, … quitar los spawns de AC en
`alexscaves:primordial_caves` con un BiomeModifier (`forge:remove_spawns`) y añadir los nuestros replicando
pesos y tamaños (grottoceratops w27 2-4, relicheirus w13 1-1, tremorsaurus w5 1-1, vallumraptor w6 6-7,
subterranodon w6 3-5, minecraft:frog w7 1-2, trilocaris w15 1-2 en water_ambient).

### Qué se rompe — inventario concreto, verificado en el jar de AC 2.0.2

1. **La receta del huevo de Tremorzilla se vuelve incraftable.**
   `tremorzilla_egg = tectonic_shard + immortal_embryo + alexscaves:tremorsaurus_egg`. El
   `alexscaves:tremorsaurus_egg` solo lo pone un `alexscaves:tremorsaurus` vivo
   (`TremorsaurusEntity.createEggBlockState`). Si el Tremorsaurus deja de generarse, **el Tremorzilla —el jefe
   de OTRA cueva, que el usuario quiere conservar y renombrar a Roarerzilla— queda inalcanzable en
   supervivencia**. Esto choca de frente con el requisito 4 del encargo.
   *Parche posible*: que nuestro Roarer ponga el bloque `alexscaves:tremorsaurus_egg` resolviéndolo por id.
   Funciona, pero entonces del huevo de AC eclosiona un Tremorsaurus de AC — el mob que acabamos de expulsar.

2. **Las mixturas dejan de funcionar sobre nuestros mobs.**
   `PrehistoricMixtureItem.interactLivingEntity` de AC hace
   `livingEntity instanceof DinosaurEntity dinosaur && dinosaur.onFeedMixture(...)` contra **su**
   `DinosaurEntity`. Nuestras entidades extienden la NUESTRA ⇒ el `instanceof` falla ⇒ **Serene Salad,
   Primordial Soup y Seething Stew no hacen nada**, incluido el D7 (Rammer→Prisa I) que es literalmente lo
   que el usuario pide que funcione. Mismo problema en `DinosaurEggBlock` (eclosión), `AnimalLayEggGoal` y
   `CommonEvents`. No hay tag que arregle un `instanceof`.

3. **El Glider se sigue generando aunque le quites el spawn.**
   `alexscaves:subterranodon_roost` es una **feature de worldgen** en el bioma (paso 4 de la lista de
   `features`), y `SubterranodonRoostFeature` invoca `ACEntityRegistry.SUBTERRANODON` cableado en bytecode
   (`getstatic #232`). Quitar la entrada del spawner no lo detiene; quitar la feature del bioma borra también
   los nidos (el bloque `subterranodon_egg` y su estructura). Fuente de spawn incontrolable.

4. **9 advancements de AC se quedan muertos**: `breed_grottoceratops`, `defeat_big_dinosaur`,
   `discover_dinosaur`, `feed_relicheirus`, `fiddlehead`, `cave_painting`, `tame_subterranodon`,
   `tame_tremorsaurus`, `tame_vallumraptor` — todos con `"entity": {"type": "alexscaves:<dino>"}` literal.
   Un jugador ya no puede completarlos con los mobs que ve. Duplicarlos en nuestro datapack es imposible sin
   tocar sus ficheros (y el `parent` los encadena en el árbol de AC).

5. **12 entradas de la guía** (`assets/alexscaves/books/primordial_caves/*.json`) describen mobs que ya no
   existen en la cueva; el índice del capítulo los enlaza.

6. **5 huevos generadores duplicados**: `alexscaves:spawn_egg_grottoceratops` sigue existiendo y sigue
   invocando al Grottoceratops de AC. En creativo aparecen dos familias visualmente idénticas.

7. **Tags**: `#alexscaves:dinosaurs` (mobs que no pisan huevos), `resists_tremorsaurus_roar`,
   `subterranodon_flees`, `vallumraptor_targets`, `amber_monolith_skips` — estas SÍ se arreglan añadiendo
   nuestros ids por tag (las tags se fusionan). Es el único apartado barato.

8. **Mundos en curso**: los dinos de AC ya generados siguen siendo entidades de AC; conviven con los
   nuestros indefinidamente. Dos especies idénticas al ojo, una domesticable y otra no.

9. **Coste de implementación real**: en compat NO registramos `PMItemRegistry`, `PMBlockRegistry`,
   `PMSoundRegistry`, `PMEffectRegistry`, `PMParticleRegistry`. Nuestras entidades los usan por todas partes
   (`PMBlockRegistry.TREE_STAR.get()` en el `TemptGoal` y en `isTamingFood`, `PMSoundRegistry.*` en cada
   sonido, `PMEffectRegistry.STUNNED`, `PMItemRegistry.SERENE_SALAD`…). Un `RegistryObject.get()` sin
   registrar **lanza NPE al construir el mob**. Registrar entidades sin registrar todo lo demás no arranca;
   registrarlo todo bajo `primordialmobs:` duplica la mitad del contenido de la cueva (dos Tree Stars, dos
   Serene Salads, dos loot tables) y rompe el requisito 4 por otro lado. La salida sería refactorizar cada
   referencia a resolución dinámica por id — ~40 ficheros, alto riesgo de NPE en runtime.

**Coste estimado**: alto (refactor transversal). **Riesgo**: alto. **Rotura neta**: 6 apartados que el
encargo pide preservar explícitamente.

---

## 2. Vía B — las entidades siguen siendo las de AC, y les imponemos nuestro comportamiento con mixins

**Idea**: no registrar nada nuevo. Inyectar los 9 deltas sobre las clases de Alex's Caves con mixins
`remap=false` (para sus métodos propios) o remapeados (para los overrides de vanilla), cargados **solo** en
modo compat mediante un `IMixinConfigPlugin` que consulte
`FMLLoader.getLoadingModList().getModFileById("alexscaves")` — el mismo patrón ya verificado en Iron's
Classes contra `irons_restrictions` (anotaciones 2026-07-25).

### Cobertura real, delta a delta

| Delta | Hook | ¿Existe en la clase de AC 2.0.2? | Mecanismo | Veredicto |
|---|---|---|---|---|
| D1 variante 15 % | — | — | ya resuelto por evento (`MobSpawnEvent.FinalizeSpawn` + `AltSkin 3`) | **ya hecho** |
| D2 sentado sin derrape | `DinosaurEntity.tick` (`m_8119_`) | sí | `@Inject` TAIL | limpio |
| D3 hitbox al sentarse | `getDimensions` (`m_6972_`) | **no** (heredado de `Entity`) | método NUEVO añadido por mixin a `DinosaurEntity` (override de heredado); reobf le pone el nombre SRG | limpio, con nota |
| D4 sin partículas de skin-swap | `handleEntityEvent` (`m_7822_`) | sí | `@Inject` HEAD cancellable para b=82/83 | limpio (cosmético, no verificable headless) |
| D5 domesticar | `mobInteract` (`m_6071_`) | sí en `DinosaurEntity` | **evento Forge `PlayerInteractEvent.EntityInteract`**, sin mixin (mismo idiom que `SnifferEvents`) | limpio |
| D5 goals de sit/follow/owner-hurt | `registerGoals` (`m_8099_`) | sí | `@Inject` TAIL en las dos subclases, o `EntityJoinLevelEvent` | limpio |
| D6 montar/mandar | `canOwnerMount` / `canOwnerCommand` | **sí, en `DinosaurEntity`, devolviendo false** | `@Inject` HEAD cancellable `remap=false` → true si el tipo es grottoceratops/relicheirus y está domado | limpio — **reutiliza el pipeline de montura entero de AC** |
| D6 dirigir la montura | `getControllingPassenger` (`m_6688_`), `getRiddenInput` (`m_274296_`), `getRiddenSpeed` (`m_245547_`), `tickRidden` (`m_274498_`) | **no** en Grotto/Relicheirus (sí en Tremorsaurus: mismo recetario) | métodos NUEVOS añadidos por mixin | limpio |
| D6 asiento en el lomo | `positionRider` (`m_7332_`) | no | método NUEVO añadido por mixin | limpio; **la altura del asiento NO es verificable headless** |
| D7 Serene Salad → Prisa I | `onFeedMixture` | **sí, en `DinosaurEntity`** | `@Inject` HEAD cancellable `remap=false` | limpio |
| D8 Logger pesca por tag | `registerGoals` + `tick` + `RelicheirusMeleeGoal.tick` | sí las tres | `@Inject` TAIL (goal) + `@Inject`/`@Redirect` en el gate `instanceof TrilocarisEntity` | limpio; la tag se fusiona con la de AC |
| D9 velocidad 0.22 | `createAttributes` (estático, ya consumido en el registro) | sí pero corre antes | `AttributeModifier` persistente en `EntityJoinLevelEvent`, o se descarta | **degradado a decisión**: ver §4 |

**Nada de la lista queda sin cobertura**, salvo D9 (una diferencia de 0.02 de velocidad, cosmética) que se
resuelve con un modificador de atributo o se declara descartada.

### Por qué B no rompe nada

Las entidades siguen siendo `alexscaves:grottoceratops` y `alexscaves:relicheirus`. Por construcción:
los pesos y tamaños de grupo de spawn son **exactamente** los de la tabla del encargo (no hay que
replicarlos: son los originales); los huevos generadores, las loot tables, los huevos-bloque, la receta del
huevo de Tremorzilla, la lanza de extinción, el `#alexscaves:dinosaurs`, los 9 advancements, las 12 entradas
de la guía, la feature del nido de Glider y **los mundos en curso** siguen funcionando sin tocarlos.

**Coste estimado**: medio-bajo (≈8 hooks en 3 clases). **Riesgo**: medio (mixins contra un jar de
producción). **Rotura neta**: ninguna.

### Riesgos concretos de B y su mitigación

- **`Apply mixin failed` si AC cambia de versión.** Mitigación: `@Pseudo`+plugin de config que salta los
  mixins si `alexscaves` no está; `defaultRequire: 1` para que un fallo sea un crash ruidoso al arrancar y no
  un silencio (lección de anotaciones: "con defaultRequire:1 sería crash, no silencio"). Declarar en
  `mods.toml` `versionRange="[2.0,2.1)"` es tentador pero rompería a quien tenga 2.0.3+; se deja `[2.0,)` y
  se documenta que los mixins están probados contra 2.0.2.
- **Nombres SRG de métodos añadidos.** Un método nuevo `positionRider` en un mixin se reobfusca a `m_7332_`
  por ForgeGradle (mismo mecanismo ya verificado con el bridge `getTextureLocation`→`m_5478_` de los
  renderers de compat). Verificación: `javap` sobre el .class del mixin en el jar final.
- **Doble aplicación de goals.** `@Inject` TAIL en `registerGoals` corre una vez por entidad; los índices de
  prioridad de AC son 1..9, los nuestros se añaden por encima sin colisionar (GoalSelector admite prioridades
  repetidas).
- **Divergencia de la variante recoloreada.** En standalone el flag es `Recolored` (bool); en compat es
  `AltSkin 3`. Se mantiene (ya documentado); unificar exigiría escribir en el NBT de AC un campo que su
  `addAdditionalSaveData` no persiste.

---

## 3. Decisión

**Se elige la vía B.**

El argumento no es de preferencia sino de medición: **cuatro de los cinco mobs ya son, línea a línea, el
mismo código en los dos mods**, así que la vía A no "traería nuestros mobs" — traería copias con ids nuevos
del mismo comportamiento, a cambio de romper la receta del Tremorzilla, las mixturas (incluida la del Rammer
que el encargo pide), 9 advancements, 12 entradas de guía, los huevos generadores y los mundos en curso. La
vía B produce el resultado que el usuario describe (en la Primordial Cave los mobs se llaman Rammer/Logger,
se doman, se montan, el Logger pesca peces de verdad y tira árboles, el Rammer da Prisa I con la Serene
Salad, con sus variantes y su loot) sin romper nada, porque impone los 9 deltas sobre las entidades que ya
están ahí.

Dicho de forma honesta: **la vía B no sustituye a los mobs de Alex's Caves, los convierte en los nuestros.**
Un jugador no puede distinguir el resultado de "se generan el Rammer y el Logger del mod"; lo que no habrá es
un id `primordialmobs:rammer` en `/summon`. Si el usuario quiere específicamente ids propios visibles en
comandos, eso es la vía A con las 6 roturas de arriba, y hay que decidirlo a la vista de esa lista.

---

## 4. Plan por fases

Cada fase termina con **compilación (JDK17) + arranque de servidor headless en LOS DOS modos** (standalone y
con `alexscaves-full-2.0.2.jar`), 0 errores, verificado por RCON.

### Fase 0 — infraestructura de mixins de compat
- `compat/PrimordialMixinPlugin implements IMixinConfigPlugin`: `shouldApplyMixin` devuelve true solo para
  los mixins `compat.*` cuando `alexscaves` está en la lista de mods.
- Registrar el plugin en `primordialmobs.mixins.json`; añadir el paquete `com.primordialmobs.mixin.compat`.
- **Criterio de salida**: arranque limpio en los dos modos, sin `InjectionError` y sin que las clases de AC
  se carguen en standalone.

### Fase 1 — el Logger pesca peces de verdad (D8)
- Mixin sobre `RelicheirusEntity`: `@Inject` TAIL en `registerGoals` añadiendo
  `NearestAttackableTargetGoal<LivingEntity>` con el predicado de `#alexscaves:relicheirus_fishes`;
  `@Inject` en `tick` para el gate del pez sostenido; mixin sobre `RelicheirusMeleeGoal` para la elección de
  animación.
- La tag `data/alexscaves/tags/entity_types/relicheirus_fishes.json` ya la enviamos y **se fusiona**.
- **Verificación RCON**: la piscina de 1 bloque de profundidad de la sesión 2026-07-25 (7 cod + 1 trilocaris
  + Logger, control sin Logger, 180 s). Prueba directa de bajas.

### Fase 2 — Rammer y Logger domesticables y montables (D5, D6)
- `canOwnerMount`/`canOwnerCommand` por `@Inject` (`remap=false`) sobre `DinosaurEntity`, gateados por id.
- Domar con Tree Star (1/3) desde `PlayerInteractEvent.EntityInteract`, con la precedencia ya establecida
  (item de piel → domar → mixtura → ciclo de órdenes con shift).
- Goals de sit/follow/OwnerHurt por `@Inject` TAIL en `registerGoals`.
- Riding: `getControllingPassenger`, `getRiddenInput`, `getRiddenSpeed`, `tickRidden`, `positionRider`,
  `getDismountLocationForPassenger` como métodos añadidos, copiando exactamente las constantes de las clases
  standalone (asiento Logger `RIDER_BACK_HEIGHT=3.1`, offsets z 0.15/0.35, velocidad ×0.7).
- **Verificación RCON**: `summon` de un Grottoceratops con `{Owner:...}` y comprobar por NBT que
  `Command`/`Sitting` cambian y que un jugador falso puede montarlo (`ride`); el **asiento visual y la
  interacción de domar con un jugador real NO son verificables headless** — knobs declarados.

### Fase 3 — sentarse sin derrapar y con hitbox reducido (D2, D3)
- `@Inject` TAIL en `DinosaurEntity.tick`; método `getDimensions` añadido con la escala por especie.
- **Verificación RCON**: mob sentado empujado por vacas 35-45 s → desplazamiento 0.00 (mismo test que el
  Sniffer de v2.6.1); `data get entity ... Pos` antes/después.

### Fase 4 — Rammer: Serene Salad → Prisa I (D7)
- `@Inject` HEAD cancellable en `DinosaurEntity.onFeedMixture` (`remap=false`), gateado a
  `alexscaves:grottoceratops` + `alexscaves:serene_salad`.
- **Verificación**: el efecto necesita un jugador real (igual que en standalone). Headless se verifica que el
  mixin aplica (arranque limpio con `defaultRequire:1`) y que el item y el mob resuelven.

### Fase 5 — nombres derivados en el pack de compat (13 idiomas)
- `Tremorzilla` → **`Roarerzilla`** en: `entity.alexscaves.tremorzilla`, `item.alexscaves.spawn_egg_tremorzilla`,
  `block.alexscaves.tremorzilla_egg`, los 6 advancements (`hatch_tremorzilla_egg`, `tame_tremorzilla`,
  `tremorzilla_egg`, `tremorzilla_kill_beam` — títulos y descripciones), los 15 subtítulos de sonido
  `alexscaves.sound.subtitle.tremorzilla_*`, y las entradas de la guía que lo nombran.
- Mismo criterio para el resto de derivados de los cinco: subtítulos de sonido
  `grottoceratops_*`/`relicheirus_*`/`tremorsaurus_*`/`vallumraptor_*`/`subterranodon_*`, los `.desc` de las
  pinturas rupestres (`cave_painting_*`), las descripciones de encantamientos de la lanza de extinción
  (`chomping_spirit`, `herd_phalanx`, `plummeting_flight`) y las descripciones de advancement que citan el
  nombre viejo.
- Reejecutable desde `tools/localize_names.py` (extendido), sustituyendo dentro de la cadena localizada de AC
  para conservar la gramática de cada idioma.
- **El Atlatitan queda intacto** (entidad, huevo, advancements, loot, guía): no deriva de los cinco.

### Fase 6 — funcionalidad cruzada y entrega
- Verificar por RCON la receta del huevo de Tremorzilla/Roarerzilla y la lanza de extinción (colocando los
  ingredientes o con `/loot`), más `ominous_catalyst`, `amber_monolith`, `bone_meal_from_heavy_bone` y la
  armadura primordial.
- Actualizar `PrimordialMobs.md` (sección de compatibilidad) y `anotaciones.md`.
- Refrescar `MOD-PrimordialVanilla/PrimordialMobs-1.20.1.jar` + sha256 + dependencias.

## 4bis. RESULTADO (2026-07-26) — implementado y verificado

Todas las fases cerradas. `PrimordialMobs-1.20.1.jar` v2.7.0, sha256 `089cdfe4…`.

| Fase | Estado | Evidencia |
|---|---|---|
| 0 infraestructura | hecho | `PrimordialMixinPlugin` + `injectors.defaultRequire:1`; arranque limpio en los dos modos |
| 1 Logger pesca | **RCON-VERIFICADO** | piscina con Logger: 7 bacalaos + 1 trilocaris → **0 en <60 s**; piscina de control **intacta a los 181 s** |
| 2 domar/mandar/montar | **RCON-VERIFICADO (parcial)** | asiento medido: pasajero en `z = mob.z + 0.35` exacto; desmontaje en `(x, bbMinY, z)` = nuestro `getDismountLocationForPassenger`. Domar/montar con clic derecho exige jugador real |
| 3 sentado sin derrape + hitbox | **RCON-VERIFICADO** | Rammer y Logger sentados: **0.00 bloques en 60 s** con 4 vacas empujando cada uno, y **0.00 en 90 s** muestreado cada 15 s en el arranque final (control de pie: 1.18 bloques). Hay 1-2 bloques de asentamiento si las vacas se invocan solapadas con el mob (el empujón viene del tick de la vaca, tras nuestro TAIL); idéntico en standalone. Caja medida por sondeo `dx=0,dy=0,dz=0`: de pie **2.49**, sentado **1.94** = ×0.78 exacto |
| 4 Serene Salad → Prisa I | mixin aplicado | arranque limpio con `defaultRequire:1` = el inject resolvió; el efecto exige un jugador real |
| 5 renombres derivados | hecho | **892 claves** en 13 idiomas + **83 páginas de guía**; 0 restos de nombres de AC en el pack |
| 6 funcionalidad cruzada | verificado | ver abajo |

**Cosas que la implementación aprendió y que el diseño no preveía:**
- El refmap de Mixin **sí** remapea un método de nombre vanilla declarado en una clase de otro mod:
  `tick` → `Lcom/github/alexmodguy/alexscaves/…/RelicheirusEntity;m_8119_()V`. No hizo falta cablear SRG.
- Los métodos **añadidos** por mixin se reobfuscan como se esperaba (`positionRider`→`m_19956_`,
  `getRiddenInput`→`m_274312_`, `getDimensions`→`m_6972_`…) y quedan realmente mergeados en la clase de AC
  (comprobado con `-Dmixin.debug.export=true` + `javap` sobre la clase transformada).
- La goal del Logger y las de domesticación se añaden por `EntityJoinLevelEvent`, no por `@Inject` en
  `registerGoals`: mismo efecto, un hook menos sobre el bytecode ajeno.
- El gate de montura de AC (`canOwnerMount`/`canOwnerCommand`, que devuelven `false` en su
  `DinosaurEntity`) era todo lo que hacía falta abrir: su `mobInteract` ya trae el ciclo de órdenes, el
  montaje y los mensajes `entity.alexscaves.all.command_N`.

**Funcionalidad cruzada (requisito 4):**
- Los 10 ficheros de receta que enviamos a una ruta que AC también ocupa son **idénticos en contenido**
  (comparación JSON), así que el shadowing es inocuo; los 2 propios (`primitive_club_pewen`,
  `seething_stew_primordial`) tienen nombre único + condición `forge:not(mod_loaded alexscaves)`.
- Recuento de recetas cargadas **idéntico con y sin nuestro jar** en compat: no quitamos ninguna.
- Todos los ingredientes y productos resuelven en runtime: `tremorsaurus_egg` (bloque y objeto),
  `immortal_embryo`, `tectonic_shard`, `amber`, `amber_curiosity`, `heavy_bone`, `tough_hide`,
  `extinction_spear`, `tremorzilla_egg`, `spawn_egg_tremorzilla`.
- **Regresión standalone** (mundo nuevo, sin AC): `Done`, **0 errores**, los 5 mobs + Trilocaris
  invocables y renombrados, `tree_star`/`pewen_log` colocables, `serene_salad`/`seething_stew`/
  `primitive_club` presentes. Los mixins de compat se saltan (el arranque limpio lo demuestra: sin el
  plugin, con `defaultRequire:1`, no encontrar las clases de AC sería un crash).

## 5. Qué queda declarado como no verificable sin cliente

- La altura y orientación del jinete en el lomo del Rammer/Logger (knobs: `RIDER_BACK_HEIGHT`, el offset `z`
  del `seatOffset` y el factor `0.8` del leg-solver).
- El texto renombrado del pack de compat (es cliente puro).
- Las texturas `_variant` de los renderers de compat.
- El efecto Prisa I al alimentar (requiere un jugador real).
- La ausencia de partículas del cambio de piel (D4).
