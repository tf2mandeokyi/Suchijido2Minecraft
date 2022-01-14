package com.mndk.scjdmc.scjd;

import com.mndk.scjdmc.util.Constants;
import com.mndk.scjdmc.util.ReflectionUtil;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Geometry;

import java.lang.reflect.Field;

public class ScjdElement {

    public final Object geometryObject;

    public ScjdElement(SimpleFeature feature) {
        try {
            this.geometryObject = feature.getDefaultGeometry();
            for (Field f : ReflectionUtil.getAllFields(this.getClass(), ScjdElement.class)) {
                Column column = f.getAnnotation(Column.class);
                if (column != null && !"".equals(column.shpColumnName())) {
                    f.set(this, feature.getAttribute(column.shpColumnName()));
                }
            }
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public final SimpleFeature toOsmStyleFeature(SimpleFeatureType featureType, String id) {
        try {
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
            builder.add(this.geometryObject);
            for (Field f : ReflectionUtil.getAllFields(this.getClass(), ScjdElement.class)) {
                Column column = f.getAnnotation(Column.class);
                if (column != null && !"".equals(column.osmKeyName())) {
                    builder.set(column.osmKeyName(), f.get(this));
                }
            }
            return builder.buildFeature(id);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends ScjdElement> SimpleFeatureType getSimpleFeatureType(Class<T> clazz, String layerName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(layerName);
        builder.add(Constants.GEOMETRY_PROPERTY_NAME, Geometry.class);
        for(Field f : ReflectionUtil.getAllFields(clazz, ScjdElement.class)) {
            Column column = f.getAnnotation(Column.class);
            if(column != null && !"".equals(column.shpColumnName())) {
                builder.add(column.shpColumnName(), f.getDeclaringClass());
            }
        }
        builder.setDefaultGeometry(Constants.GEOMETRY_PROPERTY_NAME);
        return builder.buildFeatureType();
    }

    public static <T extends ScjdElement> SimpleFeatureType getOsmSimpleFeatureType(Class<T> clazz, String layerName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(layerName);
        builder.add(Constants.GEOMETRY_PROPERTY_NAME, Geometry.class);
        for(Field f : ReflectionUtil.getAllFields(clazz, ScjdElement.class)) {
            Column column = f.getAnnotation(Column.class);
            if(column != null && !"".equals(column.osmKeyName())) {
                builder.add(column.osmKeyName(), f.getDeclaringClass());
            }
        }
        builder.setDefaultGeometry(Constants.GEOMETRY_PROPERTY_NAME);
        return builder.buildFeatureType();
    }

}
