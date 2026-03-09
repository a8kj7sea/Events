

[![](https://jitpack.io/v/a8kj7sea/Events.svg)](https://jitpack.io/#a8kj7sea/Events)
## | Events <sub><sup> High-Performance SpigotAPI Event System </sub></sup>



A high-performance event bus architecture designed for **Java 21** environments. This system utilizes dynamic bytecode generation to minimize dispatch latency and provides an automated registration mechanism for scalable development.

<p align="center">
  <a href="https://github.com/a8kj7sea/Events/blob/main/asset/proof.mp4?raw=true">
    <img src="https://github.com/user-attachments/assets/014fcbeb-8eed-4c7e-a10a-4321d9a2107b" width="100%" alt="Click to watch the performance test">
  </a>
  <br>
</p>



---

### Technical Specifications

* **Bytecode Optimization:** Implements **ByteBuddy** to generate `EventExecutor` classes at runtime. This eliminates the performance overhead associated with Java Reflection.
* **Automated Discovery:** Utilizes the **Java Service Provider Interface (SPI)** via **Google AutoService** to detect and register event listeners automatically.
* **Modern Java Integration:** Fully compatible with Java 21, supporting advanced class-loading strategies and prepared for virtual thread environments.
* **Logic Controls:** Built-in support for event priorities (`SpoolPriority`) and conditional execution based on event cancellation status (`ignoreCancelled`).

---

### Implementation [Guide](https://github.com/a8kj7sea/EventsTest)

#### 1. Listener Definition

Implement the `EventSpool` interface and annotate the class with `@AutoService(EventSpool.class)`. Use the `@Subscribe` annotation for specific event handlers.

```java
@AutoService(EventSpool.class)
public class WorldProtection implements EventSpool {

    @Override
    public void bind(EventHub hub) {
        // Manual registration logic (optional)
    }

    @Subscribe(priority = SpoolPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
}

```

#### 2. System Initialization

Initialize the `EventHub` and trigger the automated registration process within your application's entry point.

```java
public void onEnable() {
    EventHub hub = new DispatchingEventHub(this);
    EventSpooler.init(hub);
    
    // Automatically discovers and registers all EventSpool implementations
    EventSpooler.getInstance().autoRegister();
}

```

---

### Required Build Configuration

To ensure the automated discovery system functions correctly, your `pom.xml` must include the **Annotation Processor** for `AutoService` and the **ServicesResourceTransformer** for the Shade plugin.

#### Maven Compiler Configuration

The following configuration ensures that `META-INF/services` files are generated during the compilation phase:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.google.auto.service</groupId>
                <artifactId>auto-service</artifactId>
                <version>1.1.1</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>

```

#### Maven Shade Configuration

When relocating packages, you must use the `ServicesResourceTransformer` to update the SPI configuration files to match the new Shaded paths:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
    <configuration>
        <transformers>
            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
        </transformers>
        <relocations>
            <relocation>
                <pattern>me.a8kj.spigot.events.hub</pattern>
                <shadedPattern>me.a8kj.spigot.events.internal.hub</shadedPattern>
            </relocation>
            <relocation>
                <pattern>me.a8kj.spigot.events.spool</pattern>
                <shadedPattern>me.a8kj.spigot.events.internal.spool</shadedPattern>
            </relocation>
            <relocation>
                <pattern>me.a8kj.spigot.events.asm</pattern>
                <shadedPattern>me.a8kj.spigot.events.internal.asm</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
</plugin>

```

### Practical Example: Cross-Server Event Synchronization

This example demonstrates the power of integrating the event system with technologies like **Redis** to synchronize events across multiple Minecraft servers (e.g., a Lobby cluster) while maintaining high performance and type-safe data handling.

#### 1. Event Definition

Define a network-transferable event just like any standard Java event:

```java
public class GlobalMessageEvent extends Event implements Serializable {
    private final String serverOrigin;
    private final String sender;
    private final String content;

    public GlobalMessageEvent(String serverOrigin, String sender, String content) {
        this.serverOrigin = serverOrigin;
        this.sender = sender;
        this.content = content;
    }
    
    // Standard Bukkit HandlerList implementation...
}

```

#### 2. Building the Event Bridge

Using the system's automated registration, you can create a bridge that links Redis to your local event bus in just a few lines of code:

```java
@AutoService(EventSpool.class)
public class RedisBridge implements EventSpool {

    @Override
    public void bind(EventHub hub) {
        // Listen to Redis messages and convert them to internal events immediately
        RedisProvider.listen("network_channel", data -> {
            GlobalMessageEvent event = Serializer.deserialize(data);
            hub.dispatch(event); // Dispatch the event into the local server bus
        });
    }

    @Subscribe
    public void onGlobalMessage(GlobalMessageEvent event) {
        // This logic executes on ALL servers whenever the event is dispatched
        System.out.println("[" + event.getServerOrigin() + "] " + event.getSender() + ": " + event.getContent());
    }
}

```

#### Architectural Advantages:

* **Full Abstraction:** Developers write code as if handling local events, while the system manages complex network synchronization behind the scenes.
* **Superior Performance:** When an event is received via Redis, the listener is invoked using **Generated Bytecode**, ensuring the lowest possible dispatch latency.
* **Seamless Scalability:** Thanks to `@AutoService` and `ServiceLoader`, network features are added to the project simply by creating the class-no modification to the core engine's source code is required.

