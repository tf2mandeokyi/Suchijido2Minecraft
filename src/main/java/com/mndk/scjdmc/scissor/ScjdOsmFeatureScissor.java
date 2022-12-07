package com.mndk.scjdmc.scissor;

import com.mndk.scjdmc.scjd.LayerDataType;
import com.mndk.scjdmc.util.FeatureGeometryUtils;
import com.mndk.scjdmc.util.ParsedOsmFeatureMap;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

public class ScjdOsmFeatureScissor {

    public static ParsedOsmFeatureMap apply(ParsedOsmFeatureMap victim) {

        ParsedOsmFeatureMap result = new ParsedOsmFeatureMap(victim.getName(), victim.getType());
        victim.entrySet().stream().filter(entry -> {
            LayerDataType type = entry.getKey();
            return type != LayerDataType.도로경계 && type != LayerDataType.도로중심선;
        }).forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        SimpleFeatureCollection
                roadBoundary = victim.get(LayerDataType.도로경계),
                roadCenterline = victim.get(LayerDataType.도로중심선);

        List<Geometry> subtractGeometries = new ArrayList<Geometry>() {{
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.터널)));
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.입체교차부)));
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.교량),
                    f -> "road".equals(f.getAttribute("highway"))
            ));
            addAll(FeatureGeometryUtils.extractGeometryAsList(victim.get(LayerDataType.안전지대),
                    f -> "yes".equals(f.getAttribute("crossing:island"))
            ));
        }};

        if (subtractGeometries.size() != 0) {
            result.put(LayerDataType.도로경계, FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                    LayerDataType.도로경계.getOsmFeatureType(), roadBoundary, subtractGeometries
            ));
            result.put(LayerDataType.도로중심선, FeatureGeometryUtils.getFeatureCollectionGeometryDifference(
                    LayerDataType.도로중심선.getOsmFeatureType(), roadCenterline, subtractGeometries
            ));
        }

        return result;

    }
}
