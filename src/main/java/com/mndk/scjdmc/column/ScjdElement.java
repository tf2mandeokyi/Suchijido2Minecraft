package com.mndk.scjdmc.column;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.util.ReflectionUtil;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.lang.reflect.Field;

public class ScjdElement {

    public final Object geometryObject;

    public ScjdElement(SimpleFeature feature) {
        try {
            this.geometryObject = feature.getDefaultGeometry();

            for (Field f : ReflectionUtil.getAllFields(this.getClass(), ScjdElement.class)) {
                Column column = f.getAnnotation(Column.class);
                if(column == null) continue;

                if (!"".equals(column.key())) {
                    Object attr = feature.getAttribute(column.key());
                    if(attr != null) f.set(this, attr);
                }
                if (!"".equals(column.name())) {
                    Object attr = feature.getAttribute(column.name());
                    if(attr != null) f.set(this, attr);
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
                if (column != null && !"".equals(column.osmName())) {
                    builder.set(column.osmName(), f.get(this));
                }
            }
            return builder.buildFeature(id);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends ScjdElement> SimpleFeatureType getSimpleFeatureType(
            Class<T> clazz, String layerName, ColumnStoredType columnStoredType
    ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(layerName);
        builder.add(Constants.GEOMETRY_PROPERTY_NAME, Geometry.class);
        for(Field f : ReflectionUtil.getAllFields(clazz, ScjdElement.class)) {
            Column column = f.getAnnotation(Column.class);
            if(column == null) continue;

            if(columnStoredType == ColumnStoredType.KEY && !"".equals(column.key())) {
                builder.add(column.key(), f.getDeclaringClass());
            }
            if(columnStoredType == ColumnStoredType.NAME && !"".equals(column.name())) {
                builder.add(column.name(), f.getDeclaringClass());
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
            if(column != null && !"".equals(column.osmName())) {
                builder.add(column.osmName(), f.getDeclaringClass());
            }
        }
        builder.setDefaultGeometry(Constants.GEOMETRY_PROPERTY_NAME);
        return builder.buildFeatureType();
    }


}
