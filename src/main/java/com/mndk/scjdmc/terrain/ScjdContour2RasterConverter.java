package com.mndk.scjdmc.terrain;

import com.mndk.scjdmc.cdtlib.Triangulation;
import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.column.ScjdElement;
import com.mndk.scjdmc.column.ScjdElevatedElement;
import com.mndk.scjdmc.util.FeatureGeometryUtils;
import com.mndk.scjdmc.util.math.Vector2DH;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScjdContour2RasterConverter {


    private static final Triangulation TRIANGULATION = new Triangulation();


    public static void triangulate(List<Vector2DH[]> vectors) {

    }


    public static List<Vector2DH[]> elevationObjectToVectors(
            SimpleFeatureCollection contourCollection, LayerDataType layerDataType
    ) {
        List<Vector2DH[]> result = new ArrayList<>(contourCollection.size());
        SimpleFeatureIterator contourIterator = contourCollection.features();

        while(contourIterator.hasNext()) {
            ScjdElement element = layerDataType.toElementObject(contourIterator.next());

            double height;
            if(element instanceof ScjdElevatedElement elevatedElement) {
                height = elevatedElement.getElevation();
            }
            else return Collections.emptyList();

            Geometry geometry = (Geometry) element.geometryObject;
            result.addAll(FeatureGeometryUtils.geometryToVector2DH(geometry, height));
        }
        contourIterator.close();

        return result;
    }
}
