package com.mndk.kvm2m.mod.commands;

import com.mndk.kvm2m.core.util.KeyRestrictedMap;
import com.mndk.kvm2m.core.vmap.parser.VMapParser;
import com.mndk.kvm2m.core.vmap.VMapParserException;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.VMapToMinecraft;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.mojang.authlib.GameProfile;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.forge.ForgeWorld;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VMapGenCmd<T extends VMapParser> extends CommandBase {

	

	static final int MAX_AXIS = 30000000;

	static final FlatRegion INFINITE_REGION =
			new CuboidRegion(new Vector(-MAX_AXIS, 0, -MAX_AXIS), new Vector(MAX_AXIS, 0, MAX_AXIS));

	static final Set<String> ALLOWED_OPTIONS = new HashSet<>(Arrays.asList(
			// Generate all of the map data regardless of worldedit selection
			"generate-all",
			// Number of elements to be generated in each tick. Without this option, it will generate one layer per tick.
			"element-per-tick",
			// Terrain won't be generated with this option.
			"no-terrain",
			// Terrain won't be cut with this option.
			"no-cutting",
			// Terrain won't be filled with tis option.
			"no-filling",
			// Map data objects won't be generated with this opiton.
			"terrain-only",
			// Building shells will be generated with this option.
			"gen-building-shells",
			// Contours will be generated with this option.
			"draw-contour",
			// Accepts only one layer. (The input is the layer name. e.g. A0010000)
			"layer-only"
	));
	
	
	
	private final String name, extension;
	private final T parser;
	
	
	
	public VMapGenCmd(String name, String extension, T parser) {
		this.name = name;
		this.extension = extension;
		this.parser = parser;
	}
	
	
	
	@Override
	public String getName() {
		return this.name;
	}


	
	@Override
	public String getUsage(ICommandSender sender) {
		return this.getName() + " <id>";
	}
	
	
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if(!(sender instanceof EntityPlayerMP)) return false;
		GameProfile profile = ((EntityPlayerMP) sender).getGameProfile();
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(profile);
	}
	
	
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		try {
			
			EntityPlayerMP player = commandSenderToPlayer(sender);
			World world = server.getEntityWorld();
			GeographicProjection projection = getWorldProjection(world);
			
			Map<String, String> options = new KeyRestrictedMap<>(ALLOWED_OPTIONS);
			
			for(String fileName : args) {
				if(fileName.startsWith("--")) {
					String keyNvalue = fileName.substring(2);
					if(keyNvalue.contains("=")) {
						String[] temp = keyNvalue.split("=", 2);
						options.put(temp[0], temp[1]);
					}
					else options.put(keyNvalue, null);
				}
			}
			
			FlatRegion worldEditRegion = options.containsKey("generate-all") ? INFINITE_REGION : validateWorldEditRegion(world, player);
			
			KVectorMap2MinecraftMod.broadcastMessage("§dParsing files...");
			
			VMapParserResult result = new VMapParserResult();
			boolean isEmpty = true;
			
			for(String fileName : args) {
				if(!fileName.startsWith("--")) {
					File file = new File(KVectorMap2MinecraftMod.kVecFileDirectory + "/" + fileName);
					if(file.isDirectory()) {
						File[] files = file.listFiles((dir, name) -> name.endsWith(this.extension));

						for(File child : files) {
							VMapParserResult parserResult = this.parser.parse(child, projection, options);
							result.append(parserResult);
						}
					}
					else {
						if(!file.isFile()) {
							throw new CommandException("File \"" + fileName + "\" does not exist!");
						}
						if(!FilenameUtils.isExtension(fileName, this.extension)) {
							throw new CommandException("Invalid extension!");
						}
						VMapParserResult parserResult = this.parser.parse(file, projection, options);
						result.append(parserResult);
					}
					
					isEmpty = false;
				}
			}
			
			if(isEmpty) throw new CommandException("No Files are given!");
			
			VMapToMinecraft.generateTasks(world, worldEditRegion, result, options);
			
		}
		catch(FileNotFoundException e) {
			throw new CommandException("File does not exist!");
		}
		catch(VMapParserException | IllegalArgumentException e) {
			KVectorMap2MinecraftMod.logger.error(e);
			throw new CommandException(e.getMessage());
		}
		catch(CommandException e) {
			throw e;
		}
		catch(Throwable t) {
			t.printStackTrace();
			throw new CommandException("An unexpected error occured while parsing vector map.");
		}
	}
	
	
	
	private static EntityPlayerMP commandSenderToPlayer(ICommandSender sender) throws CommandException {
		
		if(!(sender instanceof EntityPlayer)) {
			throw new CommandException("Only players can run this command.");
		}
		
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("This command is only available in the multiplayer server.");
		}
		
		return (EntityPlayerMP) sender;
	}
	
	
	
	private static GeographicProjection getWorldProjection(World world) throws CommandException {
		
		IChunkProvider chunkProvider = world.getChunkProvider();
		if(!(chunkProvider instanceof CubeProviderServer)) {
			throw new CommandException("You must be in a cubic chunks world to generate .dxf map.");
		}

		ICubeGenerator cubeGenerator = ((CubeProviderServer) chunkProvider).getCubeGenerator();
		if (!(cubeGenerator instanceof EarthGenerator)) {
			throw new CommandException("You must be in a terra 1 to 1 world to generate .dxf map.");
		}
		
		EarthGenerator generator = (EarthGenerator) cubeGenerator;
		
		return generator.projection;
	}
	
	
	
	private static FlatRegion validateWorldEditRegion(World world, EntityPlayerMP player) throws CommandException {
		
		ForgeWorld weWorld = ForgeWorldEdit.inst.getWorld(world);
		LocalSession session = ForgeWorldEdit.inst.getSession(player);
		Region worldEditRegion;
		try {
			worldEditRegion = session.getSelection(weWorld);
			if(!(worldEditRegion instanceof FlatRegion)) {
				throw new CommandException("Worldedit region should be either cuboid, cylinder, or polygon.");
			}
		} catch(IncompleteRegionException exception) {
			// No region is selected, or is incomplete.
			throw new CommandException("Please select the worldedit region first.");
		}
		return (FlatRegion) worldEditRegion;
		
	}
	
	
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		
		File file = new File(KVectorMap2MinecraftMod.kVecFileDirectory);
		String extension = this.extension;
		
		final String final_extension = extension.startsWith(".") ? extension : "." + extension;
		
		File[] files = file.listFiles();
		if(files == null) return new ArrayList<>();
		
		return Stream.of(files).filter(f -> f.isFile() && f.getName().endsWith(final_extension))
				.map(File::getName).collect(Collectors.toList());
	}
	
	
}
