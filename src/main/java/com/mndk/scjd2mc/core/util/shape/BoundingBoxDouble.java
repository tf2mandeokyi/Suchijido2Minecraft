package com.mndk.scjd2mc.core.util.shape;

import com.sk89q.worldedit.regions.FlatRegion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoundingBoxDouble {

    public final double xmin, zmin, xmax, zmax;

    public BoundingBoxDouble(FlatRegion region) {
        this.xmin = region.getMinimumPoint().getX(); this.zmin = region.getMinimumPoint().getZ();
        this.xmax = region.getMaximumPoint().getX(); this.zmax = region.getMaximumPoint().getZ();
    }

    public BoundingBoxDouble getIntersectionArea(BoundingBoxDouble other) {
        double xmin = Math.max(this.xmin, other.xmin), zmin = Math.max(this.zmin, other.zmin);
        double xmax = Math.min(this.xmax, other.xmax), zmax = Math.min(this.zmax, other.zmax);

        return new BoundingBoxDouble(xmin, zmin, xmax, zmax);
    }

    public BoundingBoxInteger toMaximumBoundingBoxInteger() {
        return new BoundingBoxInteger(
                (int) Math.floor(xmin), (int) Math.floor(zmin), (int) Math.ceil(xmax), (int) Math.ceil(zmax));
    }

    public boolean isValid() {
        return xmin <= xmax && zmin <= zmax;
    }
}
