package com.mndk.scjdmc.scissor;

import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.FeatureGeometryUtils;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScjdOsmFeatureScissor {

    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> apply(ScjdDirectoryParsedMap<SimpleFeatureCollection> victim) {

        ScjdDirectoryParsedMap<SimpleFeatureCollection> result = new ScjdDirectoryParsedMap<>(victim.getFileInformation());
        victim.entrySet().stream().filter(entry -> {
            LayerDataType type = entry.getKey();
            return type != LayerDataType.도로경계 && type != LayerDataType.도로중심선;
        }).forEach(entry -> result.set(entry.getKey(), entry.getValue()));

        List<SimpleFeatureCollection>
                roadBoundaryCollections = victim.get(LayerDataType.도로경계),
                roadCenterlineCollections = victim.get(LayerDataType.도로중심선);

        List<Geometry> subtractGeometries = new ArrayList<Geometry>() {{
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.터널), f -> true));
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.입체교차부), f -> true));
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.교량),
                    f -> "road".equals(f.getAttribute("highway"))
            ));
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.안전지대),
                    f -> "yes".equals(f.getAttribute("crossing:island"))
            ));
        }};

        if (subtractGeometries.size() != 0) {
            List<SimpleFeatureCollection> tempList = new ArrayList<>();
            AtomicInteger j = new AtomicInteger(0);
            for(SimpleFeatureCollection roadBoundary : roadBoundaryCollections) {
                tempList.add(FeatureGeometryUtils.subtractPolygonsToPolygonCollection(
                        LayerDataType.도로경계.getOsmFeatureType(), roadBoundary, subtractGeometries,
                        i -> victim.getFileInformation().getNameOrIndex() + "-A0010000-" + j.get() + "-" + i
                ));
                j.addAndGet(1);
            }
            result.set(LayerDataType.도로경계, tempList);

            tempList = new ArrayList<>();
            for(SimpleFeatureCollection roadCenterline : roadCenterlineCollections) {
                tempList.add(FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                        LayerDataType.도로중심선.getOsmFeatureType(), roadCenterline, subtractGeometries
                ));
            }
            result.set(LayerDataType.도로중심선, tempList);
        }

        return result;

    }
}
