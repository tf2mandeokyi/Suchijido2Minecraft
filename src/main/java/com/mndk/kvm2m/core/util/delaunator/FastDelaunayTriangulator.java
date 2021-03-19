package com.mndk.kvm2m.core.util.delaunator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.kabeja.parser.ParseException;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.util.shape.Triangle;
import com.mndk.kvm2m.core.util.shape.TriangleList;
import com.mndk.kvm2m.core.vectormap.DxfMapParser;
import com.mndk.kvm2m.core.vectormap.VectorMapParserResult;
import com.mndk.kvm2m.core.vectormap.elem.poly.VectorMapContour;



/**
 * An incredibly fast Delaunay triangulation calculator for 2d points, from <a href="https://github.com/mapbox/delaunator">https://github.com/mapbox/delaunator</a>
 * */
public class FastDelaunayTriangulator {
	
	
	
	private static final double EPSILON = Math.pow(2, -52);
	private static final int[] EDGE_STACK = new int[512];
	
	
	
	/*
	 *  Arrays that will store the triangulation graph
	 */
	private Vector2DH[] coords;
	private int[] triangles;
	private int trianglesLen;
	private int[] halfedges;
	
	private Vector2DH center;
	
	
	/* 
	 * Temporary arrays for tracking the edges of the advancing convex hull
	 */
	private int hashSize;
	private int[] hull;
	private int hullStart;
	private int[] hullPrev; // edge to previous edge
	private int[] hullNext; // edge to next edge
	private int[] hullTri; // edge to adjacent triangle
	private int[] hullHash; // angular edge hash
	
	
	/*
	 * Temporary arrays for sorting points
	 */
	private int[] ids;
	private double[] dists;
	
	
	
	public static FastDelaunayTriangulator from(List<Vector2DH> points) {
		return new FastDelaunayTriangulator(points.toArray(new Vector2DH[0]));
	}
	
	
	
	private FastDelaunayTriangulator(Vector2DH[] coords) {
		int n = coords.length;
		
		this.coords = coords;
		
		int maxTriangles = Math.max(2 * n - 5, 0);
		this.triangles = new int[maxTriangles * 3];
		this.halfedges = new int[maxTriangles * 3];
		
		this.hashSize = (int) Math.ceil(Math.sqrt(n));
        this.hullPrev = new int[n];
        this.hullNext = new int[n];
        this.hullTri = new int[n];
        this.hullHash = new int[this.hashSize];
        Arrays.fill(this.hullHash, -1);
        
        this.ids = new int[n];
        this.dists = new double[n];

        this.update();
	}
	
	
	
	private void update() {
		
		int n = coords.length;
		
		
		//
		// Populate an array of point indices; calculate input data bbox
		//
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MIN_VALUE;
		double maxY = -Double.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            double x = this.coords[i].x;
            double y = this.coords[i].z;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            this.ids[i] = i;
        }
        Vector2DH c = new Vector2DH((minX + maxX) / 2, (minY + maxY) / 2);

        double minDist = Double.MAX_VALUE;
        int i0 = -1, i1 = -1, i2 = -1;
        
        
        //
        // Pick a seed point close to the center
        //
        for (int i = 0; i < n; i++) {
            double d = dist(c, coords[i]);
            if (d < minDist) {
                i0 = i;
                minDist = d;
            }
        }
        Vector2DH i0v = coords[i0];

        minDist = Double.MAX_VALUE;
        
        
        //
        // find the point closest to the seed
        //
        for (int i = 0; i < n; i++) {
            if (i == i0) continue;
            double d = dist(i0v, coords[i]);
            if (d < minDist && d > 0) {
                i1 = i;
                minDist = d;
            }
        }
        Vector2DH i1v = coords[i1];
        
        double minRadius = Double.MAX_VALUE;

        
        //
        // find the third point which forms the smallest circumcircle with the first two
        //
        for (int i = 0; i < n; i++) {
            if (i == i0 || i == i1) continue;
            double r = circumradius(i0v, i1v, coords[i]);
            if (r < minRadius) {
                i2 = i;
                minRadius = r;
            }
        }
        Vector2DH i2v = coords[i2];

        if (minRadius == Double.MAX_VALUE) {
            // order collinear points by dx (or dy if all x are identical)
            // and return the list as a hull
            for (int i = 0; i < n; i++) {
                this.dists[i] = coords[i].x - coords[0].x != 0 ? coords[i].x - coords[0].x : coords[i].z - coords[0].z;
            }
            quicksort(this.ids, this.dists, 0, n - 1);
            int[] hull = new int[n];
            int i = 0, j = 0;
            double d0 = Double.MIN_VALUE;
            for (; i < n; i++) {
                int id = this.ids[i];
                if (this.dists[id] > d0) {
                    hull[j++] = id;
                    d0 = this.dists[id];
                }
            }
            this.hull = Arrays.copyOfRange(hull, 0, j);
            this.triangles = new int[0];
            this.halfedges = new int[0];
            return;
        }

        
        //
        // swap the order of the seed points for counter-clockwise orientation
        //
        if (orient(i0v, i1v, i2v)) {
            int i = i1;
            double x = i1v.x;
            double y = i1v.z;
            i1 = i2;
            i1v.x = i2v.x;
            i1v.z = i2v.z;
            i2 = i;
            i2v.x = x;
            i2v.z = y;
        }
        
        this.center = circumcenter(i0v, i1v, i2v);

        for (int i = 0; i < n; i++) {
            this.dists[i] = dist(coords[i], center);
        }
        
        
        //
        // sort the points by distance from the seed triangle circumcenter
        //
        quicksort(this.ids, this.dists, 0, n - 1);

        
        //
        // set up the seed triangle as the starting hull
        //
        this.hullStart = i0;
        int hullSize = 3;

        hullNext[i0] = hullPrev[i2] = i1;
        hullNext[i1] = hullPrev[i0] = i2;
        hullNext[i2] = hullPrev[i1] = i0;

        hullTri[i0] = 0;
        hullTri[i1] = 1;
        hullTri[i2] = 2;

        Arrays.fill(hullHash, -1);
        hullHash[this.hashKey(i0v)] = i0;
        hullHash[this.hashKey(i1v)] = i1;
        hullHash[this.hashKey(i2v)] = i2;

        this.trianglesLen = 0;
        this.addTriangle(i0, i1, i2, -1, -1, -1);

        
        double xp = 0, yp = 0;
        for (int k = 0; k < this.ids.length; k++) {
            int i = this.ids[k];
            Vector2DH v = coords[i];

            // skip near-duplicate points
            if (k > 0 && Math.abs(v.x - xp) <= EPSILON && Math.abs(v.z - yp) <= EPSILON) continue;
            xp = v.x;
            yp = v.z;

            // skip seed triangle points
            if (i == i0 || i == i1 || i == i2) continue;

            // find a visible edge on the convex hull using edge hash
            int start = 0;
            for (int j = 0, key = this.hashKey(v); j < this.hashSize; j++) {
                start = hullHash[(key + j) % this.hashSize];
                if (start != -1 && start != hullNext[start]) break;
            }

            start = hullPrev[start];
            int e = start, q;
            while (true) {
            	q = hullNext[e];
            	if(!orient(v, coords[e], coords[q])) {
	                e = q;
	                if (e == start) {
	                    e = -1;
	                    break;
	                }
            	}
            	else break;
            }
            if (e == -1) continue; // likely a near-duplicate point; skip it

            // add the first triangle from the point
            int t = this.addTriangle(e, i, hullNext[e], -1, -1, hullTri[e]);

            // recursively flip triangles from the point until they satisfy the Delaunay condition
            hullTri[i] = this.legalize(t + 2);
            hullTri[e] = t; // keep track of boundary triangles on the hull
            hullSize++;

            // walk forward through the hull, adding more triangles and flipping recursively
            int next = hullNext[e];
            while (true) {
            	q = hullNext[next];
            	if(orient(v, coords[next], coords[q])) {
	                t = this.addTriangle(next, i, q, hullTri[i], -1, hullTri[next]);
	                hullTri[i] = this.legalize(t + 2);
	                hullNext[next] = next; // mark as removed
	                hullSize--;
	                next = q;
            	}
            	else break;
            }

            // walk backward from the other side, adding more triangles and flipping
            if (e == start) {
                while (true) {
                	q = hullPrev[e];
                	if(orient(v, coords[q], coords[e])) {
	                    t = this.addTriangle(q, i, e, -1, hullTri[e], hullTri[q]);
	                    this.legalize(t + 2);
	                    hullTri[q] = t;
	                    hullNext[e] = e; // mark as removed
	                    hullSize--;
	                    e = q;
                	}
                	else break;
                }
            }

            // update the hull indices
            this.hullStart = hullPrev[i] = e;
            hullNext[e] = hullPrev[next] = i;
            hullNext[i] = next;

            // save the two new edges in the hash table
            hullHash[this.hashKey(v)] = i;
            hullHash[this.hashKey(coords[e])] = e;
        }

        this.hull = new int[hullSize];
        for (int i = 0, e = this.hullStart; i < hullSize; i++) {
            this.hull[i] = e;
            e = hullNext[e];
        }

        // trim typed triangle mesh arrays
        this.triangles = Arrays.copyOfRange(this.triangles, 0, this.trianglesLen);
        this.halfedges = Arrays.copyOfRange(this.halfedges, 0, this.trianglesLen);
	}
	
	
	
	private int hashKey(Vector2DH v) {
	    return ((int) Math.floor(pseudoAngle(v.x - this.center.x, v.z - this.center.z) * this.hashSize)) % this.hashSize;
	}
	
	
	
	private int legalize(int a) {

        int i = 0;
        int ar = 0;

        // recursion eliminated with a fixed-size stack
        while (true) {
            int b = halfedges[a];

            /* if the pair of triangles doesn't satisfy the Delaunay condition
             * (p1 is inside the circumcircle of [p0, pl, pr]), flip them,
             * then do the same check/flip recursively for the new pair of triangles
             *
             *           pl                    pl
             *          /||\                  /  \
             *       al/ || \bl            al/    \a
             *        /  ||  \              /      \
             *       /  a||b  \    flip    /___ar___\
             *     p0\   ||   /p1   =>   p0\---bl---/p1
             *        \  ||  /              \      /
             *       ar\ || /br             b\    /br
             *          \||/                  \  /
             *           pr                    pr
             */
            int a0 = a - a % 3;
            ar = a0 + (a + 2) % 3;

            if (b == -1) { // convex hull edge
                if (i == 0) break;
                a = EDGE_STACK[--i];
                continue;
            }

            int b0 = b - b % 3;
            int al = a0 + (a + 1) % 3;
            int bl = b0 + (b + 2) % 3;

            int p0 = triangles[ar];
            int pr = triangles[a];
            int pl = triangles[al];
            int p1 = triangles[bl];

            boolean illegal = inCircle(coords[p0], coords[pr], coords[pl], coords[p1]);

            if (illegal) {
                triangles[a] = p1;
                triangles[b] = p0;

                int hbl = halfedges[bl];

                // edge swapped on the other side of the hull (rare); fix the halfedge reference
                if (hbl == -1) {
                    int e = this.hullStart;
                    do {
                        if (this.hullTri[e] == bl) {
                            this.hullTri[e] = a;
                            break;
                        }
                        e = this.hullPrev[e];
                    } while (e != this.hullStart);
                }
                this.link(a, hbl);
                this.link(b, halfedges[ar]);
                this.link(ar, bl);

                int br = b0 + (b + 1) % 3;

                // don't worry about hitting the cap: it can only happen on extremely degenerate input
                if (i < EDGE_STACK.length) {
                    EDGE_STACK[i++] = br;
                }
            } else {
                if (i == 0) break;
                a = EDGE_STACK[--i];
            }
        }

        return ar;
    }
	
	
	
	private void link(int a, int b) {
        this.halfedges[a] = b;
        if (b != -1) this.halfedges[b] = a;
    }
	
	
	
	// add a new triangle given vertex indices and adjacent half-edge ids
	private int addTriangle(int i0, int i1, int i2, int a, int b, int c) {
        int t = this.trianglesLen;

        this.triangles[t] = i0;
        this.triangles[t + 1] = i1;
        this.triangles[t + 2] = i2;

        this.link(t, a);
        this.link(t + 1, b);
        this.link(t + 2, c);

        this.trianglesLen += 3;

        return t;
    }
	
	
	
	public TriangleList getTriangleList() {
		TriangleList result = new TriangleList();
		for(int i = 0; i < this.triangles.length; i += 3) {
			result.add(new Triangle(this.coords[this.triangles[i]], this.coords[this.triangles[i + 1]], this.coords[this.triangles[i + 2]]));
		}
		return result;
	}
	
	
	
	// monotonically increases with real angle, but doesn't need expensive trigonometry
	private static double pseudoAngle(double x, double y) {
	    double p = x / (Math.abs(x) + Math.abs(y));
	    return (y > 0 ? 3 - p : 1 + p) / 4; // [0..1]
	}
	
	
	
	private static double dist(Vector2DH a, Vector2DH b) {
	    double dx = a.x - b.x;
	    double dy = a.z - b.z;
	    return dx * dx + dy * dy;
	}
	
	
	
	// return 2d orientation sign if we're confident in it through J. Shewchuk's error bound check
	private static double orientIfSure(Vector2DH p, Vector2DH r, Vector2DH q) {
		double l = (r.z - p.z) * (q.x - p.x);
		double r0 = (r.x - p.x) * (q.z - p.z);
	    return Math.abs(l - r0) >= 3.3306690738754716e-16 * Math.abs(l + r0) ? l - r0 : 0;
	}
	
	
	
	// a more robust orientation test that's stable in a given triangle (to fix robustness issues)
	private static boolean orient(Vector2DH r, Vector2DH q, Vector2DH p) {
		double temp;
		
		temp = orientIfSure(p, r, q);
		if(temp < 0) return true;
		if(temp > 0) return false;
		
		temp = orientIfSure(r, q, p);
		if(temp < 0) return true;
		if(temp > 0) return false;
		
		temp = orientIfSure(q, p, r);
		if(temp < 0) return true;
		return false;
	}

	
	
	private static boolean inCircle(Vector2DH a, Vector2DH b, Vector2DH c, Vector2DH p) {
		double dx = a.x - p.x;
		double dy = a.z - p.z;
		double ex = b.x - p.x;
		double ey = b.z - p.z;
	    double fx = c.x - p.x;
	    double fy = c.z - p.z;

	    double ap = dx * dx + dy * dy;
	    double bp = ex * ex + ey * ey;
	    double cp = fx * fx + fy * fy;

	    return dx * (ey * cp - bp * fy) -
	           dy * (ex * cp - bp * fx) +
	           ap * (ex * fy - ey * fx) < 0;
	}

	
	
	private static double circumradius(Vector2DH a, Vector2DH b, Vector2DH c) {
		double dx = b.x - a.x;
		double dy = b.z - a.z;
	    double ex = c.x - a.x;
	    double ey = c.z - a.z;

	    double bl = dx * dx + dy * dy;
	    double cl = ex * ex + ey * ey;
	    double d = 0.5 / (dx * ey - dy * ex);

	    double x = (ey * bl - dy * cl) * d;
	    double y = (dx * cl - ex * bl) * d;

	    return x * x + y * y;
	}
	
	

	private static Vector2DH circumcenter(Vector2DH a, Vector2DH b, Vector2DH c) {
		double dx = b.x - a.x;
		double dy = b.z - a.z;
		double ex = c.x - a.x;
		double ey = c.z - a.z;

		double bl = dx * dx + dy * dy;
		double cl = ex * ex + ey * ey;
		double d = 0.5 / (dx * ey - dy * ex);

		double x = a.x + (ey * bl - dy * cl) * d;
		double y = a.z + (dx * cl - ex * bl) * d;

	    return new Vector2DH(x, 0, y);
	}
	
	
	
	private static void quicksort(int[] ids, double[] dists, int left, int right) {
	    if (right - left <= 20) {
	        for (int i = left + 1; i <= right; i++) {
	            int temp = ids[i];
	            double tempDist = dists[temp];
	            int j = i - 1;
	            while (j >= left && dists[ids[j]] > tempDist) ids[j + 1] = ids[j--];
	            ids[j + 1] = temp;
	        }
	    } else {
	        int median = (left + right) >> 1;
	        int i = left + 1;
	        int j = right;
	        swap(ids, median, i);
	        if (dists[ids[left]] > dists[ids[right]]) swap(ids, left, right);
	        if (dists[ids[i]] > dists[ids[right]]) swap(ids, i, right);
	        if (dists[ids[left]] > dists[ids[i]]) swap(ids, left, i);

	        int temp = ids[i];
	        double tempDist = dists[temp];
	        while (true) {
	            do i++; while (dists[ids[i]] < tempDist);
	            do j--; while (dists[ids[j]] > tempDist);
	            if (j < i) break;
	            swap(ids, i, j);
	        }
	        ids[left + 1] = ids[j];
	        ids[j] = temp;

	        if (right - i + 1 >= j - left) {
	            quicksort(ids, dists, i, right);
	            quicksort(ids, dists, left, j - 1);
	        } else {
	            quicksort(ids, dists, left, j - 1);
	            quicksort(ids, dists, i, right);
	        }
	    }
	}

	
	
	private static void swap(int[] arr, int i, int j) {
	    int tmp = arr[i];
	    arr[i] = arr[j];
	    arr[j] = tmp;
	}

	
	
	public static void main_(String[] args) throws FileNotFoundException, ParseException {
    	VectorMapParserResult result = DxfMapParser.parse(new File("37612030.dxf"));
    	List<Vector2DH> vertexes = result.getElevationPoints();
    	System.out.println(vertexes.size());
    	List<Triangle> triangles = FastDelaunayTriangulator.from(vertexes).getTriangleList();
    	System.out.println(triangles.size());
	}
	
	public static void main(String[] args) throws IOException {
    	int w = 400, h = 400;
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		final int ERROR_COLOR = new Color(255, 0, 0).getRGB();
		VectorMapContour[] contours = new VectorMapContour[] {
			new VectorMapContour(new Vector2DH[] {
					new Vector2DH(0, 0),
					new Vector2DH(0, h),
					new Vector2DH(w, h),
					new Vector2DH(w, 0)
			}, 50),
			new VectorMapContour(new Vector2DH[] {
					new Vector2DH(20, 20),
					new Vector2DH(20, 350),
					new Vector2DH(350, 350),
					new Vector2DH(350, 20)
			}, 100),
			new VectorMapContour(new Vector2DH[] {
					new Vector2DH(50, 50),
					new Vector2DH(50, 250),
					new Vector2DH(250, 250),
					new Vector2DH(250, 50)
			}, 150),
			new VectorMapContour(new Vector2DH[] {
					new Vector2DH(100, 100),
					new Vector2DH(100, 200),
					new Vector2DH(200, 200),
					new Vector2DH(200, 100)
			}, 200)
		};
		List<Vector2DH> vertexList = new ArrayList<>();
		for(VectorMapContour contour : contours) {
			for(Vector2DH vertex : contour.getVertexList()) {
				vertexList.add(vertex.withHeight(contour.getElevation()));
			}
		}
		
		FastDelaunayTriangulator v = FastDelaunayTriangulator.from(vertexList);
		System.out.println(Arrays.toString(v.coords));
		System.out.println(Arrays.toString(v.triangles));
		System.out.println(Arrays.toString(v.halfedges));
		System.out.println(v.coords.length);
		TriangleList triangleList = v.getTriangleList();
		
		for(int y=0;y<h;y++) {
			for(int x=0;x<h;x++) {
				double value = triangleList.interpolateHeight(new Vector2DH(x, y));
				if(value != value) image.setRGB(x, y, ERROR_COLOR);
				else {
					int a = (int) Math.round(value);
					if(a > 255) a = 255;
					if(a < 0) a = 0;
					image.setRGB(x, y, new Color(a, a, a).getRGB());
				}
			}
		}
		
		/*Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setColor(Color.GREEN);
		for(Triangle triangle : triangleList) {
			g2d.drawLine((int) triangle.v1.x, (int) triangle.v1.z, (int) triangle.v2.x, (int) triangle.v2.z);
			g2d.drawLine((int) triangle.v2.x, (int) triangle.v2.z, (int) triangle.v3.x, (int) triangle.v3.z);
			g2d.drawLine((int) triangle.v3.x, (int) triangle.v3.z, (int) triangle.v1.x, (int) triangle.v1.z);
		}*/
		
		
		File file = new File("test.png");
		ImageIO.write(image, "png", file);
	}
	
	
}
