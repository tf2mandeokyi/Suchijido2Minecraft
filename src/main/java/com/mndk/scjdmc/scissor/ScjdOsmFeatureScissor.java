package com.mndk.scjdmc.scissor;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.util.FeatureGeometryUtils;
import com.mndk.scjdmc.util.ScjdDirectoryParsedMap;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ScjdOsmFeatureScissor {

    public static ScjdDirectoryParsedMap<SimpleFeatureCollection> apply(
            ScjdDirectoryParsedMap<SimpleFeatureCollection> victim, Geometry boundary
    ) {

        ScjdDirectoryParsedMap<SimpleFeatureCollection> result = new ScjdDirectoryParsedMap<>(victim.getFileInformation());
        victim.entrySet().stream().filter(entry -> {
            LayerDataType type = entry.getKey();
            return type != LayerDataType.도로경계 && type != LayerDataType.도로중심선;
        }).forEach(entry -> {
            List<SimpleFeatureCollection> featureCollections = entry.getValue();
            if(boundary != null && entry.getKey().isCuttable()) {
                featureCollections = featureCollections.stream()
                        .map(featureCollection -> FeatureGeometryUtils.getFeatureCollectionGeometryIntersection(
                                featureCollection.getSchema(), featureCollection, boundary
                        )).collect(Collectors.toList());
            }
            result.set(entry.getKey(), featureCollections);
        });

        List<SimpleFeatureCollection>
                roadBoundaryCollections = victim.get(LayerDataType.도로경계),
                roadCenterlineCollections = victim.get(LayerDataType.도로중심선);

        if(roadBoundaryCollections != null) roadBoundaryCollections = roadBoundaryCollections.stream()
                .map(featureCollection -> FeatureGeometryUtils.getFeatureCollectionGeometryIntersection(
                        featureCollection.getSchema(), featureCollection, boundary
                )).toList();
        if(roadCenterlineCollections != null) roadCenterlineCollections = roadCenterlineCollections.stream()
                .map(featureCollection -> FeatureGeometryUtils.getFeatureCollectionGeometryIntersection(
                        featureCollection.getSchema(), featureCollection, boundary
                )).toList();

        List<Geometry> subtractGeometries = new ArrayList<>() {{
            addAll(FeatureGeometryUtils.extractGeometryAsList(result.get(LayerDataType.터널), f -> true));
            addAll(FeatureGeometryUtils.extractGeometryAsList(result.get(LayerDataType.입체교차부), f -> true));
            addAll(FeatureGeometryUtils.extractGeometryAsList(result.get(LayerDataType.교량),
                    f -> "road".equals(f.getAttribute("highway"))
            ));
        }};

        List<SimpleFeatureCollection> tempList = new ArrayList<>();
        if (!subtractGeometries.isEmpty() && roadBoundaryCollections != null) {
            AtomicInteger j = new AtomicInteger(0);
            for (SimpleFeatureCollection roadBoundary : roadBoundaryCollections) {
                tempList.add(FeatureGeometryUtils.subtractPolygonsToPolygonCollection(
                        LayerDataType.도로경계.getOsmFeatureType(), roadBoundary, subtractGeometries,
                        Constants.POLYGON_BUFFER_EPSILON,
                        i -> victim.getFileInformation().getNameOrIndex() + "-A0010000-" + j.get() + "-" + i
                ));
                j.addAndGet(1);
            }
            roadBoundaryCollections = tempList;
        }
        result.set(LayerDataType.도로경계, roadBoundaryCollections);

        if(roadCenterlineCollections != null) {
            subtractGeometries.addAll(FeatureGeometryUtils.extractGeometryAsList(roadBoundaryCollections, f -> true));

            tempList = new ArrayList<>();
            for (SimpleFeatureCollection roadCenterline : roadCenterlineCollections) {
                tempList.add(FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                        LayerDataType.도로중심선.getOsmFeatureType(), roadCenterline, subtractGeometries,
                        Constants.POLYGON_BUFFER_EPSILON
                ));
            }
            result.set(LayerDataType.도로중심선, tempList);
        }

        return result;

    }
}
