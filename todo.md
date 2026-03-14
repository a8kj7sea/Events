###  Events Engine Optimization & Fixes

- [ ] **Priority System Alignment (Global Hooking)**
    - [ ] Refactor `DispatchingEventHub` to map `SpoolPriority` directly to Bukkit's `EventPriority`.
    - [ ] Register a unique `EventExecutor` for each used (Event + Priority) combination.
    - [ ] Ensure `ignoreCancelled` logic is executed within the correct Bukkit priority window.
    - [ ] Add `MONITOR` priority support to guarantee read-only access at the end of the event lifecycle.

- [ ] **Memory & Fat Jar Optimization**
    - [ ] Migrate from `maven-shade-plugin` to Bukkit/Paper `libraries` system in `plugin.yml`.
    - [ ] Implement `minimizeJar` in Maven configuration as a fallback for legacy Spigot versions.
    - [ ] Evaluate `byte-buddy-agent` removal to keep only the core bytecode generation modules.
    - [ ] Research ClassLoader isolation to prevent dependency conflicts when multiple plugins use different versions of the engine.

- [ ] **Performance Benchmarking**
    - [ ] Implement a `nanoTime` based profiler to compare `EventSpooler` vs Native `@EventHandler`.
    - [ ] Test memory footprint (Metaspace) during mass registration of 1000+ listeners.
    - [ ] Profile bytecode generation latency during the `autoRegister` phase.

- [ ] **Reliability & Safety**
    - [ ] Add automatic `HandlerList.unregisterAll(plugin)` on `shutdown()` to prevent memory leaks during reloads.
    - [ ] Implement a fallback mechanism to standard Reflection if ByteBuddy fails in restricted JVM environments.
    - [ ] Validate `RegistrationKey` hash collisions in high-frequency event environments.
