package com.primordialmobs.server.enchantment;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.item.PMItemRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The six weapon enchantments of the two weapons this mod keeps, verbatim from upstream Alex's Caves
 * (rarities, level caps and XP costs unchanged). Upstream's other categories belong to gear this mod
 * does not include, so they are not registered.
 */
public class PMEnchantmentRegistry {

    public static final DeferredRegister<Enchantment> DEF_REG = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, PrimordialMobs.NAMESPACE);

    public static final EnchantmentCategory PRIMITIVE_CLUB = EnchantmentCategory.create("primitive_club", (item -> item == PMItemRegistry.PRIMITIVE_CLUB.get()));
    public static final EnchantmentCategory EXTINCTION_SPEAR = EnchantmentCategory.create("extinction_spear", (item -> item == PMItemRegistry.EXTINCTION_SPEAR.get()));

    public static final RegistryObject<Enchantment> SWIFTWOOD = DEF_REG.register("swiftwood", () -> new PMWeaponEnchantment("swiftwood", Enchantment.Rarity.RARE, PRIMITIVE_CLUB, 3, 8, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> BONKING = DEF_REG.register("bonking", () -> new PMWeaponEnchantment("bonking", Enchantment.Rarity.VERY_RARE, PRIMITIVE_CLUB, 1, 18, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> DAZING_SWEEP = DEF_REG.register("dazing_sweep", () -> new PMWeaponEnchantment("dazing_sweep", Enchantment.Rarity.RARE, PRIMITIVE_CLUB, 2, 10, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> PLUMMETING_FLIGHT = DEF_REG.register("plummeting_flight", () -> new PMWeaponEnchantment("plummeting_flight", Enchantment.Rarity.RARE, EXTINCTION_SPEAR, 3, 13, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> HERD_PHALANX = DEF_REG.register("herd_phalanx", () -> new PMWeaponEnchantment("herd_phalanx", Enchantment.Rarity.RARE, EXTINCTION_SPEAR, 3, 13, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> CHOMPING_SPIRIT = DEF_REG.register("chomping_spirit", () -> new PMWeaponEnchantment("chomping_spirit", Enchantment.Rarity.RARE, EXTINCTION_SPEAR, 2, 10, EquipmentSlot.MAINHAND));

    /**
     * Upstream declares mutually-exclusive pairs here; none of them involve the six enchantments this mod
     * keeps (they belong to the raygun, ortholance, dreadbow and friends), so nothing is incompatible.
     */
    public static boolean areCompatible(PMWeaponEnchantment enchantment1, Enchantment enchantment2) {
        return true;
    }

    public static void addAllEnchantsToCreativeTab(CreativeModeTab.Output output, EnchantmentCategory enchantmentCategory) {
        for (RegistryObject<Enchantment> enchantObject : DEF_REG.getEntries()) {
            if (enchantObject.isPresent()) {
                Enchantment enchant = enchantObject.get();
                if (enchant.category == enchantmentCategory) {
                    EnchantmentInstance instance = new EnchantmentInstance(enchant, enchant.getMaxLevel());
                    output.accept(EnchantedBookItem.createForEnchantment(instance));
                }
            }
        }
    }
}
