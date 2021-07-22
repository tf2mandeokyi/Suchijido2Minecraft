package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.triangulator.TerrainTriangulator;
import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.reader.VMapReader;
import com.mndk.kvm2m.db.VMapSQLManager;
import com.sk89q.worldedit.regions.FlatRegion;
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



    protected static final ExecutorService executorService = Executors.newCachedThreadPool();



    protected final File[] files;
    protected final World world;
    protected final FlatRegion worldEditRegion;
    protected final GeographicProjection projection;
    protected final VMapReader parser;
    protected final ICommandSender commandSender;
    protected final Map<String, String> options;



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



    protected VMapReaderResult getResults() throws Exception {
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

        return finalResult;
    }



    @Override
    public void run() {
        try {

            VMapReaderResult finalResult = this.getResults();


            commandSender.sendMessage(new TextComponentString("§dCalculating terrain..."));
            TriangleList triangleList = TerrainTriangulator.generateTerrain(finalResult);


            // CubeProviderServer cubeProviderServer = (CubeProviderServer) world.getChunkProvider();

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
                List<VMapLayer> layers = finalResult.getLayers();
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

        } catch(Exception e) {
            commandSender.sendMessage(new TextComponentString("§c" + e.getMessage()));
            e.printStackTrace();
        }
    }



    public static class DBFetcherTask extends VMapGenerationTask {


        private final String[] ids;

        private static final VMapReader EMPTY_READER = new VMapReader() {
            @Override protected Map.Entry<VMapGeometryPayload, VMapDataPayload> getResult() { return null; }
        };


        public DBFetcherTask(
                String[] ids,
                World world,
                FlatRegion worldEditRegion,
                GeographicProjection projection,
                ICommandSender commandSender,
                Map<String, String> options
        ) {
            super(new File[]{}, world, worldEditRegion, projection, EMPTY_READER, commandSender, options);
            this.ids = ids;
        }

        @Override
        protected VMapReaderResult getResults() throws Exception {

            VMapReaderResult finalResult = new VMapReaderResult();

            commandSender.sendMessage(new TextComponentString("§dFetching data..."));
            List<Callable<VMapReaderResult>> fileParsingTasks = new ArrayList<>();
            for (final String mapId : ids) {
                fileParsingTasks.add(() -> VMapSQLManager.getInstance().getVMapData(mapId, projection, options));
            }
            List<Future<VMapReaderResult>> parserResults = executorService.invokeAll(fileParsingTasks);
            for (Future<VMapReaderResult> result : parserResults) {
                finalResult.append(result.get());
            }

            return finalResult;
        }
    }
}
