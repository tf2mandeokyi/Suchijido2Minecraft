package com.mndk.kvm2m.mod;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = KVectorMap2MinecraftMod.MODID)
public class KVM2MConfig {

    @Config.Name("DB URL")
    public static String dburl = "";

    @Config.Name("DB ID")
    public static String id = "";

    @Config.Name("DB PW")
    public static String pw = "";



    public static void save() {
        ConfigManager.sync(KVectorMap2MinecraftMod.MODID, Config.Type.INSTANCE);
    }



    @Mod.EventBusSubscriber(modid = KVectorMap2MinecraftMod.MODID)
    public static class ConfigEventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(KVectorMap2MinecraftMod.MODID)) {
                KVM2MConfig.save();
            }
        }

    }

}
