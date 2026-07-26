package com.primordialmobs.mixin;

import net.minecraft.world.entity.animal.sniffer.Sniffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Sniffer.class)
public interface SnifferAccessor {

    @Invoker("getState")
    Sniffer.State ac_invokeGetState();

    /**
     * Raw state setter. Unlike {@link Sniffer#transitionTo}, this does NOT arm the seed-drop timer when
     * entering DIGGING, which is exactly what the sit pose wants: the lying-down digging animation with no
     * seed extraction.
     */
    @Invoker("setState")
    Sniffer ac_invokeSetState(Sniffer.State state);
}
