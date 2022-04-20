package me.wesley1808.servercore.interfaces.activation_range;

import me.wesley1808.servercore.utils.ActivationRange;

public interface ActivationEntity {
    ActivationRange.ActivationType getActivationType();

    boolean isExcluded();

    int getActivatedTick();

    void setActivatedTick(int activatedTick);

    int getActivatedImmunityTick();

    void setActivatedImmunityTick(int activatedImmunityTick);

    void setInactive(boolean active);

    int getFullTickCount();

    void incFullTickCount();
}
