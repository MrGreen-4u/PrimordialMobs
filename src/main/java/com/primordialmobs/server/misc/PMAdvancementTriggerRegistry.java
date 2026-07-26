package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;

public class PMAdvancementTriggerRegistry {

    public static final PMAdvancementTrigger DINOSAURS_MINECART = new PMAdvancementTrigger(new ResourceLocation(PrimordialMobs.NAMESPACE, "dinosaurs_minecart"));

    public static void setup() {
        CriteriaTriggers.register(DINOSAURS_MINECART);
    }
}
