package com.mndk.scjd2mc.mod.commands;

import com.mndk.scjd2mc.core.scjd.ScjdDataGenerationTask;
import com.mndk.scjd2mc.core.scjd.SuchijidoData;
import com.mndk.scjd2mc.core.scjd.reader.SuchijidoFileReader;
import com.mndk.scjd2mc.core.util.KeyRestrictedMap;
import com.mndk.scjd2mc.mod.Suchijido2MinecraftMod;
import com.sk89q.worldedit.regions.FlatRegion;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@ParametersAreNonnullByDefault
public class GetDataFromDatabaseCmd extends DataGenerationCmd {


    public GetDataFromDatabaseCmd(String name) {
        super(name, "", new SuchijidoFileReader() {
            @Override protected SuchijidoData getResult() { return null; }
        });
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        try {

            EntityPlayerMP player = commandSenderToPlayer(sender);
            World world = server.getEntityWorld();
            GeographicProjection projection = getWorldProjection(world);

            Map<String, String> options = new KeyRestrictedMap<>(ALLOWED_OPTIONS);

            boolean isEmpty = true;

            List<String> mapIdList = new ArrayList<>();

            for(String arg : args) {
                if(arg.startsWith("--")) {
                    String keyNvalue = arg.substring(2);
                    if(keyNvalue.contains("=")) {
                        String[] temp = keyNvalue.split("=", 2);
                        options.put(temp[0], temp[1]);
                    }
                    else options.put(keyNvalue, null);
                }
                else {
                    mapIdList.add(arg);

                    isEmpty = false;
                }
            }

            FlatRegion worldEditRegion = options.containsKey("generate-all") ? INFINITE_REGION : validateWorldEditRegion(world, player);

            if(isEmpty) throw new CommandException("No Files are given!");

            new Thread(new ScjdDataGenerationTask.DBFetcherTask(
                    mapIdList.toArray(new String[0]),
                    world,
                    worldEditRegion,
                    projection,
                    player,
                    options
            )).start();

        } catch(IllegalArgumentException e) {
            Suchijido2MinecraftMod.logger.error(e);
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
}
