package com.mndk.kmdi.core.math;

import com.sk89q.worldedit.Vector2D;

public class SplineMath {
	
	/*
	@Deprecated static double[][] spline(double x, Vector2D... v) {
		int n = v.length - 1;
		double[] b = new double[n], d = new double[n], h = new double[n];
		for(int i=0;i<n;i++) {
			h[i] = v[i+1].getX() - v[i].getX();
		}
		double[] c = new double[n+1], l = new double[n+1], mu = new double[n+1], z = new double[n+1];
		l[0] = 1; mu[0] = z[0] = 0;
		for(int i=1;i<n;i++) {
			double alpha = 3 * (v[i+1].getZ() - v[i].getZ()) / h[i] - 3 * (v[i].getZ() - v[i-1].getZ()) / h[i-1];
			l[i] = 2 * (v[i+1].getX() - v[i-1].getX()) - h[i-1] * mu[i-1];
			mu[i] = h[i] / l[i];
			z[i] = (alpha - h[i-1] * z[i-1]) / l[i];
		}
		l[n] = 1; z[n] = c[n] = 0;
		for(int j=n-1;j>=0;j--) {
			c[j] = z[j] - mu[j] * c[j+1];
			b[j] = (v[j+1].getZ() - v[j].getZ()) / h[j] - h[j] * (c[j+1] + 2 * c[j]) / 3;
			d[j] = (c[j+1] - c[j]) / (3 * h[j]);
		}
		double[][] output_set = new double[n][];
		for(int i=0;i<n;i++) {
			output_set[i] = new double[] {v[i].getZ(), b[i], c[i], d[i], v[i].getX()};
		}
		return output_set;
	}
	*/
	
	static double/*[]*/ getHeight(double x, Vector2D v0, Vector2D v1, Vector2D v2, Vector2D v3) {
		double dx2 = v2.getX() - v1.getX();
		double dz2 = v2.getZ() - v1.getZ();
		
		double l1 = 2 * (v2.getX() - v0.getX());
		double mu1 = dx2 / l1;
		double z1 = 3 * (dz2 / dx2 - (v1.getZ() - v0.getZ()) / (v1.getX() - v0.getX())) / l1;

		double alpha2 = 3 * (v3.getZ() - v2.getZ()) / (v3.getX() - v2.getX()) - 3 * dz2 / dx2;
		double l2 = 2 * (v3.getX() - v1.getX()) - dx2 * mu1;
		double z2 = (alpha2 - dx2 * z1) / l2;
		
		double c1 = z1 - mu1 * z2;
		double b1 = dz2 / dx2 - dx2 * (z2 + 2 * c1) / 3;
		double d1 = (z2 - c1) / (3 * dx2);
		
		double dx = x - v1.getX();
		return v1.getZ() + b1*dx + c1*dx + d1*dx;
		
		// return new double[] {v1.getZ(), b1, c1, d1, v1.getX()};
	}
	
	/*public static void main(String[] args) {
	System.out.println(Arrays.toString(spline(0,
			new Vector2D(0, 1),
			new Vector2D(1, 1),
			new Vector2D(2, 2),
			new Vector2D(3, 2))));
	}*/
}