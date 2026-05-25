package com.aelew.weavy;

import com.aelew.weavy.listener.MouseEventListener;
import com.aelew.weavy.transformer.LunarTransformer;
import com.aelew.weavy.util.Logger;
import net.weavemc.api.ModInitializer;
import net.weavemc.api.event.EventBus;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;

public final class WeavyMod implements ModInitializer {

    @Override
    public void preInit(@NotNull final Instrumentation instrumentation) {
        Logger.info("preInit");
        instrumentation.addTransformer(new LunarTransformer());
    }

    @Override
    public void init() {
        Logger.info("init");
        EventBus.subscribe(new MouseEventListener());
    }

}
