package com.mndk.scjdmc.scjd;

import com.mndk.scjdmc.util.ReflectionUtil;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Geometry;

import java.lang.reflect.Field;

public class ScjdDefaultElement {

    public final Object geometryObject;

    public ScjdDefaultElement(SimpleFeature feature) {
        try {
            this.geometryObject = feature.getDefaultGeometry();
            for (Field f : ReflectionUtil.getAllFields(this.getClass(), ScjdDefaultElement.class)) {
                Column column = f.getAnnotation(Column.class);
                if (column != null && !"".equals(column.name())) {
                    f.set(this, feature.getAttribute(column.name()));
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
            for (Field f : ReflectionUtil.getAllFields(this.getClass(), ScjdDefaultElement.class)) {
                Column column = f.getAnnotation(Column.class);
                if (column != null && !"".equals(column.jsonName())) {
                    builder.set(column.jsonName(), f.get(this));
                }
            }
            return builder.buildFeature(id);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends ScjdDefaultElement> SimpleFeatureType getSimpleFeatureType(Class<T> clazz, String layerName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(layerName);
        builder.add("geometry", Geometry.class);
        for(Field f : ReflectionUtil.getAllFields(clazz, ScjdDefaultElement.class)) {
            Column column = f.getAnnotation(Column.class);
            if(column != null && !"".equals(column.name())) {
                builder.add(column.name(), f.getDeclaringClass());
            }
        }
        builder.setDefaultGeometry("geometry");
        return builder.buildFeatureType();
    }

    public static <T extends ScjdDefaultElement> SimpleFeatureType getOsmSimpleFeatureType(Class<T> clazz, String layerName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(layerName);
        builder.add("geometry", Geometry.class);
        for(Field f : ReflectionUtil.getAllFields(clazz, ScjdDefaultElement.class)) {
            Column column = f.getAnnotation(Column.class);
            if(column != null && !"".equals(column.jsonName())) {
                builder.add(column.jsonName(), f.getDeclaringClass());
            }
        }
        builder.setDefaultGeometry("geometry");
        return builder.buildFeatureType();
    }

}
