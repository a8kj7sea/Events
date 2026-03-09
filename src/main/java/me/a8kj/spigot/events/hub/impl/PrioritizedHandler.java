package me.a8kj.spigot.events.hub.impl;

import lombok.Value;
import me.a8kj.spigot.events.EventHandlerDelegate;
import me.a8kj.spigot.events.SpoolPriority;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

@Value
class PrioritizedHandler<T extends Event> implements Comparable<PrioritizedHandler<?>> {
    SpoolPriority priority;
    boolean ignoreCancelled;
    EventHandlerDelegate<T> delegate;

    public void execute(T event) {
        if (ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            return;
        }
        delegate.handle(event);
    }

    @Override
    public int compareTo(PrioritizedHandler<?> other) {
        return Integer.compare(this.priority.ordinal(), other.priority.ordinal());
    }
}