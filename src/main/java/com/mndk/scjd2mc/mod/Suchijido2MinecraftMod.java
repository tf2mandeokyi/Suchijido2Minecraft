package com.mndk.scjd2mc.mod;

import com.mndk.scjd2mc.core.scjd.reader.NgiMapReader;
import com.mndk.scjd2mc.core.scjd.reader.ShpZipMapReader;
import com.mndk.scjd2mc.core.db.MySQLManager;
import com.mndk.scjd2mc.mod.commands.GetDataFromDatabaseCmd;
import com.mndk.scjd2mc.mod.commands.DataGenerationCmd;
import net.minecraft.command.ICommand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

@Mod(
		modid = Suchijido2MinecraftMod.MODID,
		version = Suchijido2MinecraftMod.VERSION,
		acceptableRemoteVersions = "*",
		dependencies = "required-after:worldedit;required-after:terraplusplus"
)
public class Suchijido2MinecraftMod {

	public static final String MODID = "kvm2m";
	public static final String VERSION = "b1.0";

	private static final ICommand[] serverCommands = {
			new DataGenerationCmd("genngimap", "ngi", new NgiMapReader()),
			new DataGenerationCmd("genshpzipmap", "zip", new ShpZipMapReader()),
			new GetDataFromDatabaseCmd("genmapfromdb")
	};

	public static Logger logger;
	
	public static String kVecFileDirectory;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Suchijido2MinecraftMod.logger = event.getModLog();
		this.initializeMapDirectory(event);
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		
		this.registerCommands(event);
		try {
			MySQLManager.getInstance().connect(Scjd2McConfig.dburl, Scjd2McConfig.id, Scjd2McConfig.pw);
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

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
			boolean ignored = temp.mkdirs();
		}
	}
	
	public static void broadcastMessage(String message) {
		FMLCommonHandler.instance()
				.getMinecraftServerInstance()
				.getPlayerList()
				.sendMessage(new TextComponentString(message));
	}
	
}
