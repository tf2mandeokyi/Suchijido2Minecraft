package com.mndk.kvm2m.mod;

import com.mndk.kvm2m.core.vmap.parser.NgiMapParser;
import com.mndk.kvm2m.core.vmap.parser.ShpZipMapParser;
import com.mndk.kvm2m.mod.commands.VMapGenCmd;
import net.minecraft.command.ICommand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
		modid = KVectorMap2MinecraftMod.MODID, 
		version = KVectorMap2MinecraftMod.VERSION, 
		acceptableRemoteVersions = "*",
		dependencies = "required-after:worldedit;required-after:terraplusplus"
)
public class KVectorMap2MinecraftMod {

	public static final String MODID = "kvm2m";
	public static final String VERSION = "b1.0";

	private static final ICommand[] serverCommands = {
			new VMapGenCmd("genngimap", "ngi", new NgiMapParser()),
			new VMapGenCmd("genshpzipmap", "zip", new ShpZipMapParser())
	};

	public static Logger logger;
	
	public static String kVecFileDirectory;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		KVectorMap2MinecraftMod.logger = event.getModLog();
		this.initializeMapDirectory(event);
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		
		this.registerCommands(event);
		
	}
	
	private void registerCommands(FMLServerStartingEvent event) {
		for(ICommand command : serverCommands) {
			event.registerServerCommand(command);
		}
	}
	
	private void initializeMapDirectory(FMLPreInitializationEvent event) {
		kVecFileDirectory = event.getModConfigurationDirectory().getParentFile().getAbsolutePath() + "/kvecmap_files/";
		File temp = new File(kVecFileDirectory);
		if(!temp.isDirectory()) {
			temp.mkdirs();
		}
	}
	
	public static void broadcastMessage(String message) {
		FMLCommonHandler.instance()
				.getMinecraftServerInstance()
				.getPlayerList()
				.sendMessage(new TextComponentString(message));
	}
	
}
