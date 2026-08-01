package com.primordialmobs.server.block;

import com.primordialmobs.server.misc.PMSoundRegistry;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

/** Custom block sound types, mirroring upstream ACSoundTypes for the absorbed amber family. */
public class PMSoundTypes {

    public static final SoundType AMBER = new ForgeSoundType(1.0F, 1.0F, () -> PMSoundRegistry.AMBER_BREAK.get(), () -> PMSoundRegistry.AMBER_STEP.get(), () -> PMSoundRegistry.AMBER_PLACE.get(), () -> PMSoundRegistry.AMBER_BREAKING.get(), () -> PMSoundRegistry.AMBER_STEP.get());
    public static final SoundType AMBER_MONOLITH = new ForgeSoundType(1.0F, 1.0F, () -> PMSoundRegistry.AMBER_BREAK.get(), () -> PMSoundRegistry.AMBER_STEP.get(), () -> PMSoundRegistry.AMBER_MONOLITH_PLACE.get(), () -> PMSoundRegistry.AMBER_BREAKING.get(), () -> PMSoundRegistry.AMBER_STEP.get());
}
