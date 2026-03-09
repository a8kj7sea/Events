package me.a8kj.spigot.events.spool;

import lombok.Getter;
import lombok.NonNull;
import me.a8kj.spigot.events.Subscribe;
import me.a8kj.spigot.events.hub.EventHub;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

public final class EventSpooler {

    @Getter
    private static EventSpooler instance;
    private final EventHub eventHub;

    private EventSpooler(@NonNull EventHub eventHub) {
        this.eventHub = eventHub;
    }

    public static void init(@NonNull EventHub eventHub) {
        if (instance == null) {
            instance = new EventSpooler(eventHub);
        }
    }

    public void autoRegister() {
        ServiceLoader.load(EventSpool.class, EventSpool.class.getClassLoader()).forEach(this::spool);
    }

    @SuppressWarnings("unchecked")
    public void spool(@NonNull Object object) {
        if (object instanceof EventSpool) {
            ((EventSpool) object).bind(eventHub);
        }

        for (Method method : object.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Subscribe.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> param = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(param)) continue;

            Class<? extends Event> eventClass = (Class<? extends Event>) param;
            Subscribe sub = method.getAnnotation(Subscribe.class);

            method.setAccessible(true);
            eventHub.register(eventClass, sub.priority(), sub.ignoreCancelled(), event -> {
                try {
                    method.invoke(object, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void shutdown() {
        if (eventHub != null) {
            eventHub.unregisterAll();
        }
        instance = null;
    }
}