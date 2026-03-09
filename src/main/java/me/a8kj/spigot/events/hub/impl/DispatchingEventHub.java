package me.a8kj.spigot.events.hub.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.a8kj.spigot.events.EventHandlerDelegate;
import me.a8kj.spigot.events.SpoolPriority;
import me.a8kj.spigot.events.asm.FastExecutorFactory;
import me.a8kj.spigot.events.hub.EventHub;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.util.*;

@RequiredArgsConstructor
public final class DispatchingEventHub implements EventHub, Listener {

    private final @NonNull Plugin plugin;
    private final Map<Class<? extends Event>, List<PrioritizedHandler<? extends Event>>> dispatchMap = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void register(@NonNull Class<T> eventClass, @NonNull SpoolPriority priority, boolean ignoreCancelled, @NonNull EventHandlerDelegate<T> handler) {
        boolean isFirst = !dispatchMap.containsKey(eventClass);

        List<PrioritizedHandler<? extends Event>> handlers = dispatchMap.computeIfAbsent(eventClass, k -> new ArrayList<>());
        handlers.add(new PrioritizedHandler<>(priority, ignoreCancelled, handler));
        Collections.sort(handlers);

        if (isFirst) {
            EventHandlerDelegate<T> dispatcher = event -> dispatch(eventClass, event);
            EventExecutor executor = FastExecutorFactory.create(dispatcher);
            Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.NORMAL, executor, plugin);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void dispatch(Class<T> eventClass, T event) {
        List<PrioritizedHandler<? extends Event>> handlers = dispatchMap.get(eventClass);
        if (handlers != null) {
            for (PrioritizedHandler<? extends Event> prioritized : handlers) {
                ((PrioritizedHandler<T>) prioritized).execute(event);
            }
        }
    }

    @Override
    public void unregisterAll() {
        dispatchMap.clear();
        HandlerList.unregisterAll(this);
    }
}