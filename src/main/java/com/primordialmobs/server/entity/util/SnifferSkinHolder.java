package com.primordialmobs.server.entity.util;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public interface SnifferSkinHolder {

    int ac_getSkinType();

    void ac_setSkinType(int type);

    boolean ac_isRecolored();

    void ac_setRecolored(boolean recolored);

    @Nullable
    UUID ac_getOwnerUUID();

    void ac_setOwnerUUID(@Nullable UUID owner);

    default boolean ac_isTame() {
        return ac_getOwnerUUID() != null;
    }

    /**
     * 0 = wander, 1 = sit (lying on the ground with the digging pose), 2 = follow owner.
     * Mirrors DinosaurEntity's command cycle and reuses its "entity.alexscaves.all.command_N" messages.
     */
    int ac_getCommand();

    void ac_setCommand(int command);

    default boolean ac_isOrderedToSit() {
        return ac_getCommand() == 1;
    }

    /**
     * The Seething Stew's rage: for this many ticks the sniffer drops everything and headbutts
     * hostile mobs around it (see SnifferMixin#ac_rageStep). The countdown is server side; the
     * on/off state is synched so the client can pose the animal.
     */
    void ac_enrage(int ticks);

    boolean ac_isEnraged();

    /**
     * The angry posture, eased over ~5 ticks and interpolated for rendering: 0 = calm,
     * 1 = fully reared with the snout in the air. Client side reads this in SnifferModelMixin.
     */
    float ac_getAngryHeadAmount(float partialTick);
}
