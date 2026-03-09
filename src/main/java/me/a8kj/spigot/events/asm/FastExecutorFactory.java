package me.a8kj.spigot.events.asm;

import me.a8kj.spigot.events.EventHandlerDelegate;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;

public final class FastExecutorFactory {

    public static <T extends Event> EventExecutor create(EventHandlerDelegate<T> delegate) {
        try {
            return new ByteBuddy()
                    .subclass(EventExecutor.class)
                    .method(ElementMatchers.named("execute"))
                    .intercept(MethodCall.invoke(
                            EventHandlerDelegate.class.getMethod("handle", Event.class)
                    ).on(delegate).withArgument(1))
                    .make()
                    .load(delegate.getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            return (listener, event) -> {
                try {
                    if (event instanceof Event) {
                        ((EventHandlerDelegate<T>) delegate).handle((T) event);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            };
        }
    }
}