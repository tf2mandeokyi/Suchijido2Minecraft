package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.triangulator.TerrainTriangulator;
import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.reader.VMapReader;
import com.sk89q.worldedit.regions.FlatRegion;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class VMapGenerationTask implements Runnable {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final File[] files;
    private final World world;
    private final FlatRegion worldEditRegion;
    private final GeographicProjection projection;
    private final VMapReader parser;
    private final ICommandSender commandSender;
    private final Map<String, String> options;

    public VMapGenerationTask(
            File[] files,
            World world,
            FlatRegion worldEditRegion,
            GeographicProjection projection,
            VMapReader parser,
            ICommandSender commandSender,
            Map<String, String> options
    ) {
        this.files = files;
        this.world = world;
        this.worldEditRegion = worldEditRegion;
        this.projection = projection;
        this.parser = parser;
        this.commandSender = commandSender;
        this.options = options;
    }


    @Override
    public void run() {
        try {

            VMapReaderResult finalResult = new VMapReaderResult();

            commandSender.sendMessage(new TextComponentString("§dParsing files..."));
            List<Callable<VMapReaderResult>> fileParsingTasks = new ArrayList<>();
            for (final File file : files) {
                fileParsingTasks.add(() -> this.parser.parse(file, projection, options));
            }
            List<Future<VMapReaderResult>> parserResults = executorService.invokeAll(fileParsingTasks);
            for (Future<VMapReaderResult> result : parserResults) {
                finalResult.append(result.get());
            }



            commandSender.sendMessage(new TextComponentString("§dCalculating terrain..."));
            TriangleList triangleList = TerrainTriangulator.generateTerrain(finalResult);



            CubeProviderServer cubeProviderServer = (CubeProviderServer) world.getChunkProvider();

            if(!options.containsKey("no-terrain")) {
                commandSender.sendMessage(new TextComponentString("§dGenerating terrain..."));

                List<Callable<Object>> triangleGenerationTasks = new ArrayList<>();
                final boolean doCut = !options.containsKey("no-cutting"),
                        doFill = !options.containsKey("no-filling");

                for (Triangle triangle : triangleList) {
                    triangleGenerationTasks.add(Executors.callable(() -> {
                        triangle.rasterize(world, worldEditRegion, Blocks.GRASS.getDefaultState());
                        if (doCut) triangle.removeTerrainAbove(world, worldEditRegion);
                        if (doFill) triangle.fillBlocksBelow(world, worldEditRegion);
                    }));
                }
                executorService.invokeAll(triangleGenerationTasks);
            }



            List<Callable<Object>> layerGenerationTask = new ArrayList<>();
            if(!options.containsKey("terrain-only")) {
                List<VMapLayer> layers = finalResult.getElementLayers();
                if (layers.isEmpty()) return;

                commandSender.sendMessage(new TextComponentString("§dGenerating elements..."));
                for(VMapLayer layer : layers) {
                    VMapElementDataType type = layer.getType();
                    if(options.containsKey("layer-only")) {
                        if(!type.equals(VMapElementDataType.fromLayerName(options.get("layer-only")))) {
                            continue;
                        }
                    }
                    if(!options.containsKey("draw-contour")) {
                        if(type == VMapElementDataType.등고선 || type == VMapElementDataType.표고점) {
                            continue;
                        }
                    }
                    commandSender.sendMessage(new TextComponentString(
                            "§dGenerating layer \"" + type + "\"..."));
                    for(VMapElement element : layer) {
                        layerGenerationTask.add(Executors.callable(() ->
                                element.generateBlocks(this.worldEditRegion, this.world, triangleList)));
                    }
                    executorService.invokeAll(layerGenerationTask);
                    layerGenerationTask.clear();
                }
            }



            commandSender.sendMessage(new TextComponentString("§dDone!"));

        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
