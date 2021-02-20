package com.mndk.kmdi.core.math.laplace;

import org.ejml.simple.SimpleMatrix;

import java.util.*;

/* Modified from https://bitbucket.org/nicooplusplus/discretelaplaceexample */
public class GridXZ {
    private enum Remove {INSIDE, OUTSIDE}
    private enum NodeStatus {IGNORED, SETTLED, TO_COMPUTE};
    private static class Node {
        double x, y, z;
        int X, Z;
        Node left, right, up, down;
        NodeStatus nodeStatus;
        public Node(int X, int Z) {
            this.X = X; this.Z = Z;
            this.nodeStatus = NodeStatus.IGNORED;
            this.x = 0; this.y = 0; this.z = 0;
        }
    }
    private final int nbCellX;
    private final int nbCellZ;
    private int nbrTO_COMPUTE;
    private final List<Node> nodes;
    private final Map<AbstractMap.SimpleEntry<Integer, Integer>, Integer> XZMappingToA;

    SimpleMatrix b, U, A;

    public GridXZ(int nbCellX, int nbCellZ, double x1, double z1, double x2, double z2) {
        this.nbCellX = nbCellX; this.nbCellZ = nbCellZ;
        this.nbrTO_COMPUTE = 0;
        double dx = Math.abs((x2 - x1) / ((double) nbCellX));
        double dz = Math.abs((z2 - z1) / ((double) nbCellZ));
        this.nodes = new ArrayList<>();
        this.XZMappingToA = new HashMap<>();
        if(dz <0.001 || dx <0.001) System.out.println("dx or dz <0.001");
        for(int Z=0; Z<nbCellZ; Z++)for(int X=0; X<nbCellX; X++){
            Node temp = new Node(X,Z);
            temp.x=X* dx;
            temp.z=Z* dz;
            nodes.add(temp);
        }
    }

    public void generateLinks() {
        for(int Z=0; Z<nbCellZ; Z++)for(int X=0; X<nbCellX; X++){
            if(nodes.get(X*nbCellZ+Z).nodeStatus!= NodeStatus.IGNORED){
                if(X>0 && nodes.get((X-1)*nbCellZ+Z).nodeStatus!= NodeStatus.IGNORED){
                    nodes.get(X*nbCellZ+Z).left = nodes.get((X-1)*nbCellZ+Z);
                    nodes.get((X-1)*nbCellZ+Z).right = nodes.get(X*nbCellZ+Z);
                }
                if(Z>0 && nodes.get(X*nbCellZ+(Z-1)).nodeStatus!= NodeStatus.IGNORED){
                    nodes.get(X*nbCellZ+Z).up = nodes.get(X*nbCellZ+(Z-1));
                    nodes.get(X*nbCellZ+(Z-1)).down = nodes.get(X*nbCellZ+Z);
                }
            }
        }
    }

    public void addingContour(int X1, int Z1, int X2, int Z2, Remove InOut, double height) {
        List<Node> contour = new ArrayList<>();
        contour.add(new Node(X1,Z1));
        contour.get(0).y = height;
        contour.add(new Node(X1,Z2));
        contour.get(1).y = height;
        contour.add(new Node(X2,Z2));
        contour.get(2).y = height;
        contour.add(new Node(X2,Z1));
        contour.get(3).y = height;
        contour.add(new Node(X1,Z1)); // to make a loop
        contour.get(4).y = height;

        // sets the nodes status
        for(int i=1 ; i<contour.size() ; i++){
            /*distance*/ double D0 = Math.sqrt(Math.pow(contour.get(i-1).X - contour.get(i).X,2)+Math.pow(contour.get(i-1).Z - contour.get(i).Z,2));

            for(int Z=0 ; Z<nbCellZ ; Z++)for(int X=0 ; X<nbCellX ; X++){
                /*distance*/   double D1 = Math.sqrt(Math.pow(contour.get(i-1).X - nodes.get(X*nbCellZ+Z).X,2)+Math.pow(contour.get(i-1).Z - nodes.get(X*nbCellZ+Z).Z,2));
                /*distance*/   double D2 = Math.sqrt(Math.pow(contour.get(i).X - nodes.get(X*nbCellZ+Z).X,2)+Math.pow(contour.get(i).Z - nodes.get(X*nbCellZ+Z).Z,2));

                // check if the node is on the contour :
                if(D1+D2<D0+0.0001f){
                    /*set status*/ nodes.get(X*nbCellZ+Z).nodeStatus= NodeStatus.SETTLED;
                    /*set height*/ nodes.get(X*nbCellZ+Z).y=height;
                }else{
                    // so the node isn't on the contour, check if is inside :
                    boolean inside=false;

                    for (int j = 0, k = contour.size()-1; j < contour.size(); k = j++) {
                        if ( ((contour.get(j).Z>nodes.get(X*nbCellZ+Z).Z) != (contour.get(k).Z>nodes.get(X*nbCellZ+Z).Z)) && (nodes.get(X*nbCellZ+Z).X < (contour.get(k).X-contour.get(j).X) * (contour.get(j).Z) / (contour.get(k).Z-contour.get(j).Z) + contour.get(j).X) )
                            inside = !inside;
                    }
                    if(inside && nodes.get(X*nbCellZ+Z).nodeStatus!= NodeStatus.SETTLED){
                        nodes.get(X*nbCellZ+Z).nodeStatus= (InOut==Remove.OUTSIDE)? NodeStatus.TO_COMPUTE : NodeStatus.IGNORED;
                    }
                }
            }
        }
    }

    public void solve() {
        for(int Z=0 ; Z<nbCellZ ; Z++)for(int X=0 ; X<nbCellX ; X++){
            if(nodes.get(X*nbCellZ+Z).nodeStatus==NodeStatus.TO_COMPUTE){
                XZMappingToA.put(new AbstractMap.SimpleEntry<>(X,Z), nbrTO_COMPUTE);
                nbrTO_COMPUTE++;
            }
        }
        
        // we set the dimensions of vec and matrix
        b = new SimpleMatrix(nbrTO_COMPUTE, 1);
        // b.set_size(nbrTO_COMPUTE);
        // b.fill(0.0f);
        U = new SimpleMatrix(nbrTO_COMPUTE, 1);
        // U.set_size(nbrTO_COMPUTE);
        // U.fill(0.0f);
        A = new SimpleMatrix(nbrTO_COMPUTE, nbrTO_COMPUTE);
        // A.set_size( nbrTO_COMPUTE , nbrTO_COMPUTE);
        // A.fill(0.0f);

        int I=0; /*index on node TO_COMPUTE*/
        for(int Z=0 ; Z<nbCellZ ; Z++)for(int X=0 ; X<nbCellX ; X++){
            if(nodes.get(X*nbCellZ+Z).nodeStatus!=NodeStatus.TO_COMPUTE) continue;
            A.set(I, XZMappingToA.get(new AbstractMap.SimpleEntry<>(X,Z)), 4.0);
            // up neibourer
            if(nodes.get(X*nbCellZ+Z).up != null){
                switch(nodes.get(X*nbCellZ+Z).up.nodeStatus){
                    case SETTLED:
                        b.getDDRM().add(I, 0, nodes.get(X*nbCellZ+Z).up.y);
                        break;

                    case TO_COMPUTE:
                        if(Z<nbrTO_COMPUTE) A.set(I, XZMappingToA.get(new AbstractMap.SimpleEntry<>(X, Z-1)),-1.0);
                        break;

                    case IGNORED:
                        break;
                }
            } // end of up
            // down neibourer
            if(nodes.get(X*nbCellZ+Z).down != null){
                switch(nodes.get(X*nbCellZ+Z).down.nodeStatus){
                    case SETTLED:
                        b.getDDRM().add(I, 0, nodes.get(X*nbCellZ+Z).down.y);
                        break;

                    case TO_COMPUTE:
                        if(Z>0) A.set(I, XZMappingToA.get(new AbstractMap.SimpleEntry<>(X, Z+1)), -1.0);
                        break;

                    case IGNORED:
                        break;
                }
            } // end of down
            // left neibourer
            if(nodes.get(X*nbCellZ+Z).left != null){
                switch(nodes.get(X*nbCellZ+Z).left.nodeStatus){
                    case SETTLED:
                        b.getDDRM().add(I, 0, nodes.get(X*nbCellZ+Z).left.y);
                        break;

                    case TO_COMPUTE:
                        if(X>0) A.set(I, XZMappingToA.get(new AbstractMap.SimpleEntry<>(X-1, Z)), 1.0f);
                        break;

                    case IGNORED:
                        break;
                }
            } // end of left
            // right neibourer
            if(nodes.get(X*nbCellZ+Z).right != null){
                switch(nodes.get(X*nbCellZ+Z).right.nodeStatus){
                    case SETTLED:
                        b.getDDRM().add(I, 0, nodes.get(X*nbCellZ+Z).right.y);
                        break;

                    case TO_COMPUTE:
                        if(X<nbrTO_COMPUTE) A.set(I, XZMappingToA.get(new AbstractMap.SimpleEntry<>(X+1, Z)), -1.0);
                        break;

                    case IGNORED:
                        break;
                }
            } // end of right
            I++;
        } // end of for
        U = A.solve(b);

        // send results to nodes
        I=0;
        for(int Z=0 ; Z<nbCellZ ; Z++)for(int X=0 ; X<nbCellX ; X++){
            if(nodes.get(X*nbCellZ+Z).nodeStatus == NodeStatus.TO_COMPUTE){
                nodes.get(X*nbCellZ+Z).y = U.get(I, 0);
                nodes.get(X*nbCellZ+Z).nodeStatus = NodeStatus.SETTLED;
                I++;
            }
        }
    }

    /*public void consoleOutput() {
        std::cout<<std::endl;

        std::cout<<"nbrTO_COMPUTE:"<<nbrTO_COMPUTE<<std::endl;
        for (int i=0 ; i<nodes.size() ; i++){
            if(nodes.get(i).nodeStatus== NodeStatus.IGNORED) continue;
            std::cout << "me("<<nodes.get(i).X<<","<<nodes.get(i).Z<<") status("<<nodes.get(i).nodeStatus<<") 3D("<<nodes.get(i).x<<","<<nodes.get(i).y<<","<<nodes.get(i).z;
            if(nodes.get(i).left != null) std::cout <<") left("<<nodes.get(i).left.X<<","<<nodes.get(i).left.Z;
            if(nodes.get(i).right != null) std::cout <<") right("<<nodes.get(i).right.X<<","<<nodes.get(i).right.Z;
            if(nodes.get(i).up != null) std::cout <<") up("<<nodes.get(i).up.X<<","<<nodes.get(i).up.Z;
            if(nodes.get(i).down != null) std::cout <<") down("<<nodes.get(i).down.X<<","<<nodes.get(i).down.Z;
            std::cout<<")"<<std::endl;
        }

        std::cout << A << std::endl;
        std::cout << b << std::endl;
        std::cout << U << std::endl;
        for(int Z=0 ; Z<nbCellZ ; Z++){
            for(int X=0 ; X<nbCellX ; X++){
                std::cout<<"("<<X<<","<<Z<<")="<<nodes.get(X*nbCellZ+Z).y<<"|";
            }
            std::cout<<std::endl;
        }
    }*/
    
    public void consoleOutput() {
    	System.out.println("nbrTO_COMPUTE: " + this.nbrTO_COMPUTE);
    	for(int i=0;i<nodes.size();i++) {
    		if(nodes.get(i).nodeStatus == NodeStatus.IGNORED) continue;
    		System.out.println("me(" + nodes.get(i).X + ", " + nodes.get(i).z + ") status(" + nodes.get(i).nodeStatus + ") 3D(" + nodes.get(i).x + "," + nodes.get(i).y + "," + nodes.get(i).z);
            if(nodes.get(i).left != null) System.out.print(") left(" + nodes.get(i).left.X + "," + nodes.get(i).left.Z);
            if(nodes.get(i).right != null) System.out.print(") right(" + nodes.get(i).right.X + "," + nodes.get(i).right.Z);
            if(nodes.get(i).up != null) System.out.print(") up(" + nodes.get(i).up.X + "," + nodes.get(i).up.Z);
            if(nodes.get(i).down != null) System.out.print(") down(" + nodes.get(i).down.X + "," + nodes.get(i).down.Z);
            System.out.println(")");
    	}
    	System.out.println(this.A);
    	System.out.println(this.b);
    	System.out.println(this.U);
    	for(int Z=0 ; Z<nbCellZ ; Z++){
            for(int X=0 ; X<nbCellX ; X++){
                System.out.print("(" + X + "," + Z + ")=" + nodes.get(X*nbCellZ+Z).y + "|");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        GridXZ grid = new GridXZ(/*deltas*/5,5,/*3D space coordinates*/ -1.0f,-1.0f,1.0f,1.0f);
        grid.addingContour(/*grid coordinates*/ 0, 0, 4, 4, /*points to remove*/ Remove.OUTSIDE, /*height of the contour*/ -0.2f);
        grid.generateLinks();
        grid.solve();
        grid.consoleOutput();
    }
}
