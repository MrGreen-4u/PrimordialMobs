# anotaciones — Primordial Mobs (Forge 1.20.1, add-on de Alex's Caves)

Notas no obvias y reutilizables. Leer antes de tocar nada.

## Arquitectura (desde 3.0.0, 2026-08-07)

- **El mod es SOLO un add-on**: Alex's Caves es dependencia obligatoria (mods.toml) y dueño de
  todos los assets/registros. No queda modo standalone: 36 clases Java, 0 registros de
  bloques/items/entidades propios. Todo se resuelve por id contra los registros de AC
  (`CompatDinosaurs.treeStar()` etc.) o se injerta con mixins/eventos.
- Los deltas de comportamiento (domar/montar/sentarse Grazer+Logger, pesca por tag, variantes
  15%, Sniffer) están catalogados en COMPAT_PLAN.md §0 (la medición 2026-07-26 que justificó
  este diseño: 4 de los 5 dinos eran clones línea a línea).
- `PrimordialMixinPlugin` se conserva AUNQUE AC sea obligatorio: los mixins se aplican antes de
  que Forge valide dependencias; sin el gate, arrancar sin AC daría un crash de mixin en vez de
  la pantalla amable de "missing dependency".
- El overlay de renombres es el pack `resourcepacks/primordial_compat` (required + TOP via
  AddPackFindersEvent). Desde 3.0.0 es un overlay MÍNIMO (solo claves/páginas que cambian, no
  copias completas de los lang de AC). Se regenera con `tools/generate_rename_pack.py`.
- El gate de config del pack: el `RepositorySource` se consulta en CADA escaneo de packs; el
  primer escaneo corre ANTES de que Forge cargue configs → `PMServerConfig.renamesEnabled()`
  lee el TOML a mano (NightConfig) como fallback y usa el spec cargado después.
- Config COMMON única (`primordialmobs-general.toml`): rename_mobs, alternative-textures
  (enabled + chance 0.15), brushing (relic 0.08 + egg 0.02). El GLM lee el config en apply();
  sus campos JSON egg_chance/relic_chance son opcionales (-1 = usa config) por si un datapack
  quiere fijarlos.
- `Mob.goalSelector`/`targetSelector` son PUBLIC en vanilla 1.20.1 — no hace falta AT. El AT
  se eliminó entero en 3.0.0 (todas sus líneas servían al código standalone borrado).

## Jerarquías de AC útiles

- `AtlatitanEntity extends SauropodBaseEntity extends DinosaurEntity` → el mixin de
  `DinosaurEntity` (flag PMRecolorable synched) y el roll de variantes cubren al Atlatitan
  gratis. `LuxtructosaurusEntity` también es SauropodBase (solo renombrado, sin variantes).
- `AtlatitanRenderer.getTextureLocation` propio de AC resuelve retro/tectónica por
  `getAltSkin()`; nuestro mixin solo intercepta cuando `PMRecolorable.pm_isRecolored()`.
- El roll de variante va en `EntityJoinLevelEvent` (primer join, flag `PMVariantRolled` en
  ForgeData), NO en FinalizeSpawn: el SubterranodonRoostFeature de AC spawnea con
  addFreshEntity sin finalizeSpawn.

## Texturas de variantes (tools/recolor_variants.py)

- Los recolores son conscientes del modelo: el script reconstruye el UV unwrap por caja
  (part → rects) desde la geometría decompilada (VallumraptorModel 64×64, SauropodBaseModel +
  AtlatitanModel 512×512, transcrita dentro del script). Cajas de anchura 0 (garra hoz, quills,
  dewlap, foot plano h=0) = solo caras este/oeste o top/bottom.
- Los OJOS del Atlatitan (azul bioluminiscente) están en los MISMOS píxeles UV en las 3 pieles;
  la máscara se deriva UNA vez de atlatitan.png (azul saturado, +1px de crecimiento) y se
  protege en las tres — en la retro los bordes del ojo son azul tenue y un umbral por color
  fallaba.
- Paletas establecidas (mantener): Stealer estándar azul→verde (-88°), retro hielo→arena
  (190→37), tectónica óxido→púrpura (10→279). Garras = tratamiento uniforme deliberado
  (estándar: cuerno pardo h28; retro: pizarra h200; tectónica: INTACTAS, son de magma).
  Dientes/ojos/puntas ígneas de quills siempre protegidos.
- Asimetría de mapeo del Vallumraptor: `lfoot` es un plano h=0 con las garras PINTADAS en él
  (u20,v0), y `lclaw` (garra hoz) es un plano w=0 (u21,v34). El "verde garra" del base vive ahí.

## Build / verificación

- JDK: los /usr/lib/jvm del sistema son JRE. Usar /tmp/jdk17 (Temurin). `chmod +x gradlew`
  tras clonar. ForgeGradle 5, mappings parchment 2023.09.03.
- AC como dependencia de compilación: `implementation fg.deobf("blank:alexscaves-full:2.0.2")`
  vía flatDir libs/ (el jar NO está en el repo; bajarlo de CurseForge, README).
- El skin Steve en runClient NO es bug del mod: es la sesión offline "Dev" de ForgeGradle
  (uuid offline → hash par → Steve). Solución en build.gradle: propiedades mc_username/mc_uuid
  en gradle.properties (la descarga de skins no requiere token).
- Servidor de prueba: /tmp/mcserver (Forge 47.4.10 + citadel 2.6.x + alexscaves-full-2.0.2).
  RCON puerto 25575 pass test. Recordar re-copiar el jar recién construido a mods/ y verificar
  que no está stale.
- Los renombres/pack/texturas variantes son cliente puro: no verificables headless. El server
  verifica: arranque limpio con defaultRequire:1 (= todos los mixins compat resolvieron),
  summon de los 8 mobs, GLM registrado, tags, biome modifiers.
- Auditoría "add-on limpio" (script en la sesión 2026-08-08, re-ejecutable): byte-compare de CADA
  entrada del jar contra alexscaves-full-2.0.2.jar (también con el prefijo resourcepacks/ pelado)
  → 0 idénticos; los lang del overlay con 0 claves iguales a las de AC; los png del sniffer
  distintos del vanilla (client-extra.jar de la caché de ForgeGradle tiene los assets vanilla).
  El global_loot_modifiers.json comparte ruta con el de AC a propósito: Forge los FUSIONA
  (`replace:false` en ambos).
- `pkill -f unix_args` en este harness se auto-mata (el patrón matchea la propia shell del
  comando): usar `pgrep -f 'unix_arg[s]'` o matar por PID.

## Sesión 2026-08-08 — mixturas del Sniffer y el giro del Rammer montado

- **Mixturas del Sniffer (v3.1)**: Serene Salad DOMESTICA (1/3, sustituye al Tree Star; en uno
  domado = calmar/descansar), Seething Stew ENFURECE (1200 ticks: persigue y embiste monstruos;
  daño directo con `hurt()` porque el Sniffer vanilla NO tiene atributo ATTACK_DAMAGE y
  `doHurtTarget` petaría), Primordial Soup REDUCE A LA MITAD el TTL restante de la memoria
  SNIFF_COOLDOWN (`brain.getTimeUntilExpiry` + `setMemoryWithExpiry`). La rabia vive en
  ForgeData `ACSnifferRage` (restaurada en EntityJoinLevel) y corre en el patrón
  customServerAiStep-cancel ya usado para sit/follow.
- **Rammer montado que anda "roto"**: `SauropodBaseEntity.turningFast` (público, sin sync)
  decide la velocidad de giro del CUERPO (10°/tick vs 2°/tick) y SOLO lo activan los melee
  goals. Montado, `tickRidden` fija el yaw de movimiento al de la cámara al instante y el cuerpo
  (piernas/cuello/cola cuelgan de yBodyRot) se arrastra a 2°/tick → anda de lado. Fix:
  AtlatitanEntityMixin TAIL en tickRidden → turningFast = (input del jinete) o (desvío
  cuerpo-vs-yaw > 20°). tickRidden corre en AMBOS lados (el despacho está antes del check
  isControlledByLocalInstance en LivingEntity.travelRidden), así que el campo queda coherente.
- Verificación headless de la rabia: summon sniffer con ForgeData{ACSnifferRage:900} + zombie —
  el sniffer lo persigue y la vida del zombie baja.
- Postura de rabia del Sniffer (rediseño 3.1.3): la cabeza YA NO se mantiene alzada toda la
  rabia — solo al ACERTAR un cabezazo sube ~1 s y baja con easing. Mecanismo: ac_rageStep
  emite `broadcastEntityEvent(self, 78)` al conectar el golpe (los event bytes vanilla llegan
  hasta 63; 78 libre); el cliente lo recibe en un override de `handleEntityEvent` MERGEADO por
  el mixin (el Sniffer vanilla NO declara ese método — Animal sí, id 18 corazones — así que el
  merge como override es legal) y arma un contador de 15 ticks → easing 0..5 al subir, hold,
  easing al bajar (20 ticks totales). AC_ENRAGED se conserva synched solo para suprimir
  sniffing/scenting. ForgeGradle reobfusca el override a `m_7822_` solo (verificado en el jar).
  Convención verificada en el fuente vanilla: headPitch POSITIVO = mirar abajo
  (`head.xRot = headPitch * π/180`), así que subir = restar (SnifferModelMixin, −0.55 rad).
- Tectónica del Rammer v5 (REHECHA 2026-08-08; la v4 "aplanadora" fue un desastre en el
  modelo): la base tectónica NO es "cuerpo negro + venas brillantes" — es un dino negro con
  ZONAS rojo sangre OSCURAS (manos, punta de cola, cuello, cara, dewlap; v mediana 0.27, p95
  0.50) con modelado interno rico y celosía de escamas negras encima. Clasificar todo ese rojo
  como "glow" y comprimirlo a una banda brillante borra el modelado → placas ámbar planas.
  Lo correcto es RE-TEMPERAR: solo la familia roja saturada (h330-55, s>.45, ojos excluidos;
  las púas NO se excluyen — el magma de su base debe seguir la luz), curva de v MONÓTONA sobre
  los 8 tonos discretos de la base y hue en función del v FINAL por la rampa muestreada de
  grottoceratops_tectonic (26→40; su paleta no tiene blanco quemado, v máx 0.87).
  Afinado v5.1 (feedback del usuario 2026-08-08): los 3 tonos oscuros (0.12/0.16/0.28) NO se
  estiran hacia arriba (leían como parches marrones en pecho/muslos/brazos/cuello) — se hunden
  a 0.10/0.14/0.30 con desaturación en el extremo oscuro (s ×0.35..1 interp por v) para fundirse
  con el cuerpo negro; del oro oscuro hacia arriba (base 0.35→0.52, 0.44→0.65, 0.50→0.74,
  0.60+→0.87) el mapeo quedó congelado tal cual (aprobado). Cuerpo/púas/dientes/ojos = píxeles
  de AC intactos.
- **tools/preview_atlatitan.py**: renderer ortográfico del Rammer (geometría transcrita del
  SauropodBaseModel+AtlatitanModel decompilados: rotationPoints, ángulos, texOffs, mirror;
  convención UV de Cube vanilla con UP V-flipped y mirror = U-flip por cara; painter + backface
  cull). USARLO SIEMPRE para juzgar un recolor del Rammer — la lámina UV plana engaña.
  Validado contra la piel estándar (ojos/dientes/dewlap caen donde deben).
- Geometría no obvia del AtlatitanModel: las púas del cuello son cube_r11/r12 con UV (227,259)
  8×24×8 y (139,62) 8×17×8 (faltaban en la transcripción → se oscurecían como cuerpo); las púas
  de hombro (0,123) 11×38×11 usan la ESQUINA LIBRE del unwrap del chest (x<57,y<180), no
  solapan sus caras.
- REVERTIDO (v3.1.2, petición del usuario): el AtlatitanEntityMixin de turningFast. El andar
  montado del Rammer debe quedar INTACTO como en el mod original. Verificado contra upstream
  main (1.20.1, 2026-08): tickRidden/tickWalking/areLegsMoving/getLegSlamAmount/turningFast son
  idénticos a 2.0.2 — el "giro lento del cuerpo montado" ES el comportamiento vigente de AC.
  Tras el revert, NADA nuestro toca el movimiento/animación del Atlatitan (el renderer mixin es
  solo textura; el sit-pin de DinosaurEntityMixin nunca se activa porque el Atlatitan no tiene
  goal de sentarse).
