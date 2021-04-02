package com.mndk.kvm2m.core.vectormap.elem;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.VMapBlockSelector;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapPolyline;
import com.mndk.ngiparser.ngi.element.NgiElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.sk89q.worldedit.regions.FlatRegion;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public abstract class VMapElement {
	
	
	protected final VMapElementLayer parent;
	protected final IBlockState blockState;
	protected final int y;
	
	
	public VMapElement(VMapElementLayer parent, IBlockState blockState, int y) {
		this.parent = parent;
		this.blockState = blockState;
		this.y = y;
	}
	
	
	public VMapElementLayer getParent() { return parent; }
	
	
	protected static Vector2DH projectGrs80CoordToBteCoord(Grs80Projection projection, double x, double y) {
		double[] geoCoordinate = projection.toGeo(x, y), bteCoordinate;
		try {
			bteCoordinate = Projections.BTE.fromGeo(geoCoordinate[0], geoCoordinate[1]);
		} catch(OutOfProjectionBoundsException exception) {
			throw new RuntimeException(exception); // wcpgw lmao
		}
		return new Vector2DH(bteCoordinate[0] * Projections.BTE.metersPerUnit(), -bteCoordinate[1] * Projections.BTE.metersPerUnit());
	}
	
	
	public abstract void generateBlocks(FlatRegion region, World world, TriangleList triangles);
	
	
	public static VMapElement fromNgiElement(VMapElementLayer layer, NgiElement<?> ngiElement, Grs80Projection projection) {
		
		String layerName = ngiElement.parent.name;
		VMapElementType type = VMapElementType.getTypeFromLayerName(layerName);
		IBlockState state = VMapBlockSelector.getBlockState(ngiElement, type);
		int y = VMapBlockSelector.getAdditionalHeight(ngiElement, type);
		
		if(ngiElement instanceof NgiPolygonElement) {
			return VMapElement.fromNgiPolygon(layer, (NgiPolygonElement) ngiElement, type, state, y, projection);
		}
		else if(ngiElement instanceof NgiLineElement) {
			return VMapElement.fromNgiLine(layer, (NgiLineElement) ngiElement, type, state, y, projection);
		}
		else if(ngiElement instanceof NgiPointElement) {
			return VMapElement.fromNgiPoint(layer, (NgiPointElement) ngiElement, type, state, y, projection);
		}
		return null;
	}
	
	
	public static VMapPolyline fromNgiPolygon(
			VMapElementLayer layer, 
			NgiPolygonElement polygon, 
			VMapElementType type, 
			IBlockState state, 
			int y, 
			Grs80Projection projection
	) {
		if(type == VMapElementType.도곽선) {
			return new VMapPolyline(layer, polygon, state, y, projection);
		}
		else if(type == VMapElementType.건물) {
			return new VMapBuilding(layer, polygon, state, y, projection);
		}
		else {
			return new VMapPolyline(layer, polygon, state, y, projection);
		}
	}
	
	
	
	public static VMapPolyline fromNgiLine(
			VMapElementLayer layer, 
			NgiLineElement line, 
			VMapElementType type, 
			IBlockState state, 
			int y, 
			Grs80Projection projection
	) {
		
		if(type == VMapElementType.등고선) {
			return new VMapContour(layer, line, y, projection);
		}
		else if(type == VMapElementType.도곽선) {
			return new VMapPolyline(layer, line, state, y, projection);
		}
		else {
			return new VMapPolyline(layer, line, state, y, projection);
		}
	}
	
	
	
	public static VMapPoint fromNgiPoint(
			VMapElementLayer layer, 
			NgiPointElement point, 
			VMapElementType type, 
			IBlockState state, 
			int y, 
			Grs80Projection projection
	) {
		if(type == VMapElementType.표고점) {
			return new VMapElevationPoint(layer, point, y, projection);
		}
		else {
			return new VMapPoint(layer, point, state, y, projection);
		}
	}
}
