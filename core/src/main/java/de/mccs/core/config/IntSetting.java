package de.mccs.core.config;

/** An int setting; returns the current value (it may change when the config is reloaded or synced). */
@FunctionalInterface
public interface IntSetting {
    int get();
}
