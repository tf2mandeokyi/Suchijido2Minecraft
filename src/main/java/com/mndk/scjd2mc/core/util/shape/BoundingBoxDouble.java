package com.mndk.scjd2mc.core.util.shape;

import com.sk89q.worldedit.regions.FlatRegion;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor @ToString
public class BoundingBoxDouble {

    public static final BoundingBoxDouble ILLEGAL_INFINITE = new BoundingBoxDouble(
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    public final double xmin, zmin, xmax, zmax;

    public BoundingBoxDouble(FlatRegion region) {
        this.xmin = region.getMinimumPoint().getX(); this.zmin = region.getMinimumPoint().getZ();
        this.xmax = region.getMaximumPoint().getX(); this.zmax = region.getMaximumPoint().getZ();
    }

    public BoundingBoxDouble and(BoundingBoxDouble other) {
        double xmin = Math.max(this.xmin, other.xmin), zmin = Math.max(this.zmin, other.zmin);
        double xmax = Math.min(this.xmax, other.xmax), zmax = Math.min(this.zmax, other.zmax);

        return new BoundingBoxDouble(xmin, zmin, xmax, zmax);
    }

    public BoundingBoxDouble or(BoundingBoxDouble other) {
        double xmin = Math.min(this.xmin, other.xmin), zmin = Math.min(this.zmin, other.zmin);
        double xmax = Math.max(this.xmax, other.xmax), zmax = Math.max(this.zmax, other.zmax);

        return new BoundingBoxDouble(xmin, zmin, xmax, zmax);
    }

    public BoundingBoxInteger toMaximumBoundingBoxInteger() {
        return new BoundingBoxInteger(
                (int) Math.floor(xmin), (int) Math.floor(zmin), (int) Math.ceil(xmax), (int) Math.ceil(zmax));
    }

    public boolean isValid() {
        return xmin <= xmax && zmin <= zmax;
    }

    public Map<String, Object> toSerializableMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("xmin", xmin);
        result.put("xmax", xmax);
        result.put("zmin", zmin);
        result.put("zmax", zmax);
        return result;
    }
}
