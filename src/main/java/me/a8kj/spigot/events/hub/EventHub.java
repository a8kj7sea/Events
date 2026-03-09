package me.a8kj.spigot.events.hub;


import me.a8kj.spigot.events.EventHandlerDelegate;
import me.a8kj.spigot.events.SpoolPriority;
import org.bukkit.event.Event;

public interface EventHub {
    <T extends Event> void register(Class<T> eventClass, SpoolPriority priority, boolean ignoreCancelled, EventHandlerDelegate<T> handler);

    default <T extends Event> void register(Class<T> eventClass, SpoolPriority priority, EventHandlerDelegate<T> handler) {
        register(eventClass, priority, false, handler);
    }

    default <T extends Event> void register(Class<T> eventClass, EventHandlerDelegate<T> handler) {
        register(eventClass, SpoolPriority.NORMAL, false, handler);
    }

    void unregisterAll();
}