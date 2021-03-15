package com.mndk.kmdi.core.math_deprecated;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.ejml.simple.SimpleMatrix;

import com.mndk.kmdi.core.dxfmap.elem.polyline.DXFMapContour;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

@Deprecated
public class SplineMath {
	
	/** Elastic thickness */
	private static final double H_E = 30;
	/** Poisson's ratio */
	private static final double NU = 0.5;
	/** Young's Modulus */
	private static final double E = 3;
	
	/** Plate rigidity */
	private static final double D = E * H_E*H_E*H_E / (12 * (1 - NU*NU));
	
	// Matrix is hard :(
	public static double getHeight(Vector2D point, boolean debug, Vector... vectors) {
		int n = vectors.length;
		double epsilon = 1e-6;
		
		SimpleMatrix C = new SimpleMatrix(n+3, n+3);
		SimpleMatrix W = new SimpleMatrix(n+3, 1);
		
		for(int j=0;j<n;j++) {
			C.set(j, 0, 1);
			C.set(j, 1, vectors[j].getX());
			C.set(j, 2, vectors[j].getZ());
			
			C.set(n, j+3, 1);
			C.set(n+1, j+3, vectors[j].getX());
			C.set(n+2, j+3, vectors[j].getZ());
			
			double c_j = 0;
			for(int i=0;i<n;i++) {
				if(i != j) {
					double r2 = Math.pow((vectors[j].getX() - vectors[i].getX()), 2) + Math.pow((vectors[j].getZ() - vectors[i].getZ()), 2);
					C.set(j, i+3, r2 * Math.log(r2 + epsilon));
					c_j += r2;
				}
			}
			
			C.set(j, j+3, c_j / n);
			
			// TODO uh
			/*C.set(j, j+3, 16 * Math.PI * D / Math.pow(vectors[j].getY(), 0.5));
			double K_j = 1;
			C.set(j, j+3, 16 * Math.PI * D / K_j);*/
			
			W.set(j, 0, vectors[j].getY());
		}
		
		
		SimpleMatrix F = C.solve(W);
		
		if(debug) {
			System.out.println("Vectors: " + Arrays.toString(vectors));
			System.out.println("C: " + C);
			System.out.println("F: " + F);
			System.out.println("W: " + W);
			double sum = 0, sumx = 0, sumy = 0;
			for(int i=0;i<n;i++) {
				sum += F.get(i+3);
				sumx += vectors[i].getX() * F.get(i+3);
				sumy += vectors[i].getZ() * F.get(i+3);
			}
			System.out.println("sum of all Fs: " + sum);
			System.out.println("sum of all Fs * x: " + sumx);
			System.out.println("sum of all Fs * y: " + sumy);
		}
		
		double result = F.get(0, 0) + F.get(1, 0) * point.getX() + F.get(2, 0) * point.getZ();
		for(int i=0;i<n;i++) {
			double r2 = Math.pow((point.getX() - vectors[i].getX()), 2) + Math.pow((point.getZ() - vectors[i].getZ()), 2);
			result += F.get(i+3, 0) * r2 * Math.log(r2 + epsilon);
		}
		return result;
	}
	
	public static double getHeight(Vector2D point, Vector... vectors) {
		return getHeight(point, false, vectors);
	}
	
	public static void main(String[] args) throws IOException {
		int w = 400, h = 400;
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final Color TRANSPARENT = new Color(0, 0, 0, 0);
		DXFMapContour[] contours = new DXFMapContour[] {
			new DXFMapContour(new Vector2D[] {
					new Vector2D(0, 0),
					new Vector2D(0, h),
					new Vector2D(w, h),
					new Vector2D(w, 0),
					new Vector2D(0, 0)
			}, 50),
			new DXFMapContour(new Vector2D[] {
					new Vector2D(20, 20),
					new Vector2D(20, 350),
					new Vector2D(350, 350),
					new Vector2D(350, 20),
					new Vector2D(20, 20)
			}, 100),
			new DXFMapContour(new Vector2D[] {
					new Vector2D(50, 50),
					new Vector2D(50, 250),
					new Vector2D(250, 250),
					new Vector2D(250, 50),
					new Vector2D(50, 50)
			}, 150),
			new DXFMapContour(new Vector2D[] {
					new Vector2D(100, 100),
					new Vector2D(100, 200),
					new Vector2D(200, 200),
					new Vector2D(200, 100),
					new Vector2D(100, 100)
			}, 200)
		};
		List<DXFMapContour> list = Arrays.asList(contours);

		boolean makeImage = true;
		
		if(!makeImage) SplineContourMath.getPointHeightFromContourList(new Vector2D(150, 150), true, list);
		else {
			for(int y=0;y<h;y++) {
				for(int x=0;x<h;x++) {
					double value = SplineContourMath.getPointHeightFromContourList(new Vector2D(x, y), list);
					if(value != value) image.setRGB(x, y, TRANSPARENT.getRGB());
					else {
						int a = (int) Math.round(value);
						if(a > 255) a = 255;
						if(a < 0) a = 0;
						image.setRGB(x, y, new Color(a, a, a).getRGB());
						System.out.println(x + ", " + y + " => " + a);
					}
				}
			}
			
			/*for(DXFMapContour contour : contours) {
				for(Vector2D v : contour.getVertexList()) {
					if(v.getBlockX() >= 0 && v.getBlockX() < w && v.getBlockZ() >= 0 && v.getBlockZ() < h)
					image.setRGB(v.getBlockX(), v.getBlockZ(), new Color(255, 0, 0).getRGB());
				}
			}*/
			
			
			File file = new File("asdf.png");
			ImageIO.write(image, "png", file);
		}
	}
}