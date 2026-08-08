package com.primordialmobs.server.misc;

/**
 * The one math helper the rider-seat mixins need: the same cosine walk-cycle term Citadel's
 * {@code AdvancedEntityModel.walk()} applies to a bone, so {@code positionRider} can bob the rider
 * in exact sync with the body (see GrottoceratopsEntityMixin / RelicheirusEntityMixin).
 */
public class PMMath {

    public static float walkValue(float limbSwing, float limbSwingAmount, float speed, float offset, float degree, boolean inverse) {
        return (float) ((Math.cos(limbSwing * speed + offset) * degree * limbSwingAmount) * (inverse ? -1 : 1));
    }
}
