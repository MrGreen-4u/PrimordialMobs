package com.primordialmobs.server.entity.util;

import com.primordialmobs.server.block.PMBlockRegistry;
import com.primordialmobs.server.item.PMItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Boat wood types. Upstream also has THORNWOOD (a Toxic Caves wood this mod does not include), so only
 * PEWEN is declared here; byId/byName both fall back to it, which keeps old NBT loading safely.
 */
public interface PMBoat {

    PMBoat.Type getACBoatType();

    enum Type {
        PEWEN("pewen", PMBlockRegistry.PEWEN_PLANKS, PMItemRegistry.PEWEN_BOAT, PMItemRegistry.PEWEN_CHEST_BOAT);

        private final String name;
        private final Supplier<Block> plankSupplier;
        private final Supplier<Item> dropSupplier;
        private final Supplier<Item> chestDropSupplier;

        Type(String name, Supplier<Block> plankSupplier, Supplier<Item> dropSupplier, Supplier<Item> chestDropSupplier) {
            this.name = name;
            this.plankSupplier = plankSupplier;
            this.dropSupplier = dropSupplier;
            this.chestDropSupplier = chestDropSupplier;
        }

        public String getName() {
            return this.name;
        }

        public Supplier<Block> getPlankSupplier() {
            return this.plankSupplier;
        }

        public Supplier<Item> getDropSupplier() {
            return this.dropSupplier;
        }

        public Supplier<Item> getChestDropSupplier() {
            return this.chestDropSupplier;
        }

        public String toString() {
            return this.name;
        }

        public static Type byName(String name) {
            return Arrays.stream(values()).filter(t -> t.getName().equals(name)).findFirst().orElse(values()[0]);
        }

        public static Type byId(int id) {
            return values()[id < 0 || id >= values().length ? 0 : id];
        }
    }
}
