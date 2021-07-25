package com.mndk.scjd2mc.mod;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Suchijido2MinecraftMod.MODID)
public class Scjd2McConfig {

    @Config.Name("DB URL")
    public static String dburl = "";

    @Config.Name("DB ID")
    public static String id = "";

    @Config.Name("DB PW")
    public static String pw = "";



    public static void save() {
        ConfigManager.sync(Suchijido2MinecraftMod.MODID, Config.Type.INSTANCE);
    }



    @Mod.EventBusSubscriber(modid = Suchijido2MinecraftMod.MODID)
    public static class ConfigEventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Suchijido2MinecraftMod.MODID)) {
                Scjd2McConfig.save();
            }
        }

    }

}
