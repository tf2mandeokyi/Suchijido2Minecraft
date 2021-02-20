package com.mndk.kmdi.core.we;

import java.util.function.Consumer;

import com.sk89q.worldedit.Vector2D;

public class LineGenerator {

    public static void generateFlatLine(Vector2D v1, Vector2D v2,
    		Consumer<Vector2D> consumer) {

        double dx = v2.getX() - v1.getX(), dz = v2.getZ() - v1.getZ();
        double dxa = Math.abs(dx), dza = Math.abs(dz);
        double xs, xe, zs, ze, x, z;

        if(dx == 0 && dz == 0) {
        	consumer.accept(new Vector2D(v1.getX(), v1.getZ()));
            return;
        }

        if(dx > 0) { xs = v1.getX(); xe = v2.getX(); }
        else /*dx < 0*/ { xs = v2.getX(); xe = v1.getX(); }
        if(dz > 0) { zs = v1.getZ(); ze = v2.getZ(); }
        else /*dz < 0*/ { zs = v2.getZ(); ze = v1.getZ(); }
        
        if(dxa == 0) {
        	x = v1.getX();
            for(z = zs; z <= ze; z++) {
            	consumer.accept(new Vector2D(x, z));
            }
        } else if(dza == 0) {
        	z = v1.getZ();
            for(x = xs; x <= xe; x++) {
            	consumer.accept(new Vector2D(x, z));
            }
        } else if(dxa > dza) {
            for(x = xs; x <= xe; x++) {
            	z = v1.getZ() + dz * (x - v1.getX()) / dx;
            	consumer.accept(new Vector2D(x, z));
            }
        } else {
            for(z = zs; z <= ze; z++) {
                x = v1.getX() + dx * (z - v1.getZ()) / dz;
            	consumer.accept(new Vector2D(x, z));
            }
        }
    }

}
