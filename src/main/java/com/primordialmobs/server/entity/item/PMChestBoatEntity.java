package com.primordialmobs.server.entity.item;

import com.primordialmobs.server.entity.PMEntityRegistry;
import com.primordialmobs.server.entity.util.PMBoat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

public class PMChestBoatEntity extends ChestBoat implements PMBoat {

    public PMChestBoatEntity(EntityType type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public PMChestBoatEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(PMEntityRegistry.CHEST_BOAT.get(), level);
        this.setBoundingBox(this.makeBoundingBox());
    }

    public PMChestBoatEntity(Level level, double x, double y, double z) {
        this(PMEntityRegistry.CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public PMChestBoatEntity(Level level, Vec3 location, PMBoat.Type type) {
        this(level, location.x, location.y, location.z);
        this.setACBoatType(type);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putString("ACBoatType", getACBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("ACBoatType")) {
            this.entityData.set(DATA_ID_TYPE, PMBoat.Type.byName(nbt.getString("ACBoatType")).ordinal());
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.lastYd = this.getDeltaMovement().y;
        if (!this.isPassenger()) {
            if (onGround) {
                if (this.fallDistance > 3.0F) {
                    if (this.status != Boat.Status.ON_LAND) {
                        this.resetFallDistance();
                        return;
                    }

                    this.causeFallDamage(this.fallDistance, 1.0F, this.damageSources().fall());
                    if (!this.level().isClientSide && !this.isRemoved()) {
                        this.kill();
                        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            for (int i = 0; i < 3; ++i) {
                                this.spawnAtLocation(this.getACBoatType().getPlankSupplier().get());
                            }

                            for (int j = 0; j < 2; ++j) {
                                this.spawnAtLocation(Items.STICK);
                            }
                        }
                    }
                }

                this.resetFallDistance();
            } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && y < 0.0D) {
                this.fallDistance -= (float) y;
            }
        }
    }

    public void setACBoatType(PMBoat.Type type) {
        this.entityData.set(DATA_ID_TYPE, type.ordinal());
    }

    public PMBoat.Type getACBoatType() {
        return PMBoat.Type.byId(this.entityData.get(DATA_ID_TYPE));
    }

    @Override
    public void setVariant(Boat.Type vanillaType) {
    }

    @Override
    public Item getDropItem() {
        return getACBoatType().getChestDropSupplier().get();
    }

    @Override
    public Boat.Type getVariant() {
        return Boat.Type.OAK;
    }

}
