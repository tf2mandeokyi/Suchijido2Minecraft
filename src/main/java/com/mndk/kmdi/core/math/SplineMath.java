package com.mndk.kmdi.core.math;

import org.ejml.simple.SimpleMatrix;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

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
	public static double getHeight(Vector2D point, Vector... vectors) {
		int n = vectors.length;
		double epsilon = 1e-1;
		
		SimpleMatrix C = new SimpleMatrix(n, n+3);
		SimpleMatrix W = new SimpleMatrix(n, 1);
		
		for(int j=0;j<n;j++) {
			C.set(j, 0, 1);
			C.set(j, 1, vectors[j].getX());
			C.set(j, 2, vectors[j].getZ());
			for(int i=0;i<n;i++) {
				if(i != j) {
					double r2 = Math.pow((vectors[j].getX() - vectors[i].getX()), 2) + Math.pow((vectors[j].getZ() - vectors[i].getZ()), 2);
					C.set(j, i+3, r2 * Math.log(r2 + epsilon));
				} /*else {
					// TODO uh
					// C.set(j, i+3, 16 * Math.PI * D / Math.pow(vectors[j].getY(), 0.5));
					double K_j = 1;
					C.set(j, i+3, 16 * Math.PI * D / K_j);
				}*/
			}
			
			W.set(j, 0, vectors[j].getY());
		}
		
		SimpleMatrix F = C.pseudoInverse().mult(W);
		// For some reason, the value sigma_sum(i=1; n; f.get(i+2,0)), which should return 0, is not returning it,
		// while both sigma_sum(i=1; n; f.get(i+2,0) * vectors[i-1].x) and sigma_sum(i=1; n; f.get(i+2,0) * vectors[i-1].z) are returning 0 as intended
		// maybe because I didn't put that "c_j * F_j" thing?
		
		double result = F.get(0, 0) + F.get(1, 0) * point.getX() + F.get(2, 0) * point.getZ();
		for(int i=0;i<n;i++) {
			double r2 = Math.pow((point.getX() - vectors[i].getX()), 2) + Math.pow((point.getZ() - vectors[i].getZ()), 2);
			result += F.get(i+3, 0) * r2 * Math.log(r2 + epsilon);
		}
		return result;
	}
}