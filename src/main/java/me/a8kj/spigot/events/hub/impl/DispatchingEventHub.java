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
    private final Map<Class<? extends Event>, Map<EventPriority, List<PrioritizedHandler<? extends Event>>>> dispatchMap = new HashMap<>();

    @Override
    public <T extends Event> void register(@NonNull Class<T> eventClass, @NonNull SpoolPriority priority, boolean ignoreCancelled, @NonNull EventHandlerDelegate<T> handler) {
        EventPriority bukkitPriority = toBukkitPriority(priority);
        PrioritizedHandler<T> prioritizedHandler = new PrioritizedHandler<>(priority, ignoreCancelled, handler);

        boolean isFirstForPriority = storeHandler(eventClass, bukkitPriority, prioritizedHandler);

        if (isFirstForPriority) {
            registerWithBukkit(eventClass, bukkitPriority);
        }
    }

    private EventPriority toBukkitPriority(@NonNull SpoolPriority priority) {
        try {
            return EventPriority.valueOf(priority.name());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }

    private <T extends Event> boolean storeHandler(Class<T> eventClass, EventPriority bukkitPriority, PrioritizedHandler<T> handler) {
        Map<EventPriority, List<PrioritizedHandler<? extends Event>>> priorityMap =
                dispatchMap.computeIfAbsent(eventClass, k -> new EnumMap<>(EventPriority.class));

        boolean isFirstForPriority = !priorityMap.containsKey(bukkitPriority);

        List<PrioritizedHandler<? extends Event>> handlers =
                priorityMap.computeIfAbsent(bukkitPriority, k -> new ArrayList<>());

        handlers.add(handler);
        Collections.sort(handlers);

        return isFirstForPriority;
    }

    private <T extends Event> void registerWithBukkit(Class<T> eventClass, EventPriority bukkitPriority) {
        EventHandlerDelegate<T> dispatcher = event -> dispatch(eventClass, bukkitPriority, event);
        EventExecutor executor = FastExecutorFactory.create(dispatcher);
        Bukkit.getPluginManager().registerEvent(eventClass, this, bukkitPriority, executor, plugin);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void dispatch(Class<T> eventClass, EventPriority bukkitPriority, T event) {
        Map<EventPriority, List<PrioritizedHandler<? extends Event>>> priorityMap = dispatchMap.get(eventClass);
        if (priorityMap == null) return;

        List<PrioritizedHandler<? extends Event>> handlers = priorityMap.get(bukkitPriority);
        if (handlers == null) return;

        for (PrioritizedHandler<? extends Event> prioritized : handlers) {
            ((PrioritizedHandler<T>) prioritized).execute(event);
        }
    }

    @Override
    public void unregisterAll() {
        dispatchMap.clear();
        HandlerList.unregisterAll(this);
    }
}