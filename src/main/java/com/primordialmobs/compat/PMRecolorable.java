package com.primordialmobs.compat;

/**
 * Implemented on Alex's Caves' DinosaurEntity by {@code mixin.compat.DinosaurEntityMixin}, so a compat-mode
 * dinosaur carries the recoloured-variant flag as its OWN synched value instead of borrowing a slot in
 * Alex's Caves' AltSkin.
 *
 * Why this exists: the first compat implementation encoded the variant as {@code AltSkin = 3}. AltSkin is
 * also what the Amber Curiosity (1) and the Tectonic Shard (2) write, so applying either to a recoloured
 * dinosaur showed Alex's Caves' plain retro/tectonic texture instead of our recoloured one, and clicking a
 * second time reset AltSkin to 0 and destroyed the variant permanently. Splitting the two makes compat
 * behave exactly like standalone, where the variant is an independent boolean and the textures compose
 * ({@code tremorsaurus_retro_variant}, {@code vallumraptor_tectonic_elder_variant}, ...).
 */
public interface PMRecolorable {

    boolean pm_isRecolored();

    void pm_setRecolored(boolean recolored);
}
