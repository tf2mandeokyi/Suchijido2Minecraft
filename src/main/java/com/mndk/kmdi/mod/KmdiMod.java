package com.mndk.kmdi.mod;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.mndk.kmdi.mod.commands.GenerateDXFMapCommand;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = KmdiMod.MODID, version = KmdiMod.VERSION, acceptableRemoteVersions = "*")
public class KmdiMod {

    public static final String MODID = "kmdi";
    public static final String VERSION = "1.0";

    public static Logger logger;
    
    public static String dxfFileDirectory;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KmdiMod.logger = event.getModLog();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new GenerateDXFMapCommand());
        dxfFileDirectory = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath() + "/dxf_files/";
        File temp = new File(dxfFileDirectory);
        if(!temp.isDirectory()) {
        	temp.mkdirs();
        }
    }

}
