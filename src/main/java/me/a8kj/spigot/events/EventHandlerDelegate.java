package me.a8kj.spigot.events;

import org.bukkit.event.Event;

@FunctionalInterface
public interface EventHandlerDelegate<T extends Event> {
    void handle(T event);
}