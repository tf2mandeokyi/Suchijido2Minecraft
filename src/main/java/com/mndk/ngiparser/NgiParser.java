package com.mndk.ngiparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mndk.ngiparser.ngi.NgiHeader;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParseException;
import com.mndk.ngiparser.ngi.element.NgiElement;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;

public class NgiParser {

    private BufferedReader reader;

    public List<NgiLayer> parse(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        this.reader = reader;
        return parse();
    }

    public List<NgiLayer> parse(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        this.reader = reader;
        return parse();
    }

    private List<NgiLayer> parse() throws IOException {
        List<NgiLayer> layers = new ArrayList<>();
        while(reader.ready()) {
            String line = reader.readLine();
            if(line.equals("<LAYER_START>")) {
                layers.add(readLayer());
            }
        }
        return layers;
    }

    private NgiLayer readLayer() throws IOException {
        NgiLayer layer = new NgiLayer();
        layerLoop: while(reader.ready()) {
            String line = reader.readLine();
            switch(line) {
                case "$LAYER_ID":
                    layer.id = this.readIntegerVariable();
                    break;
                case "$LAYER_NAME":
                    layer.name = this.readStringVariable();
                    break;
                case "<HEADER>":
                    layer.header = this.readHeader();
                    break;
                case "<DATA>":
                	this.readData(layer);
                    break;
                case "<LAYER_END>":
                    break layerLoop;
                default:
                    throw new NgiParseException();
            }
        }
        return layer;
    }

    private void readData(NgiLayer layer) throws IOException {
    	Map<Integer, NgiElement<?>> result = new HashMap<>();
    	while(reader.ready()) {
            String line = reader.readLine();
            if(line.startsWith("$RECORD")) {
            	int index = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
            	NgiElement<?> element = readRecord(layer);
            	result.put(index, element);
            }
            else if(line.equals("<END>")) break;
        }
    	layer.data = result;
	}


	private NgiElement<?> readRecord(NgiLayer layer) throws IOException {
    	
        String type = reader.readLine(), line;
        int dimensions = layer.header.dimensions;
        
        if(type.startsWith("TEXT")) {
        	// TODO implement this
        	return null;
        }
        
        int vertexCount, attributeIndex;
        
        switch(type) {
        	case "POINT":
        		NgiPointElement pointElement = new NgiPointElement();
        		pointElement.position = new double[dimensions];

            	String[] pointStringArr = reader.readLine().split(" ");
        		for(int d = 0; d < dimensions; ++d) {
        			pointElement.position[d] = Double.parseDouble(pointStringArr[d]);
            	}
                
                line = reader.readLine();
                if(!line.startsWith("SYMBOLGATTR")) throw new NgiParseException();
                attributeIndex = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
                pointElement.attribute = layer.header.pointAttributes.get(attributeIndex);
        		
        		return pointElement;
        		
        	case "LINESTRING":
        		NgiLineElement lineElement = new NgiLineElement();
                vertexCount = Integer.parseInt(reader.readLine());
                lineElement.lineData = new double[vertexCount][];
                
                for(int v = 0; v < vertexCount; ++v) {
                	lineElement.lineData[v] = new double[dimensions];
                	String[] vertexStringArr = reader.readLine().split(" ");
                	
                	for(int d = 0; d < dimensions; ++d) {
                		lineElement.lineData[v][d] = Double.parseDouble(vertexStringArr[d]);
                	}
                }
                
                line = reader.readLine();
                if(!line.startsWith("LINEGATTR")) throw new NgiParseException();
                attributeIndex = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
                lineElement.attribute = layer.header.lineAttributes.get(attributeIndex);
                
        		return lineElement;
        		
        	case "POLYGON":
        		NgiPolygonElement polygonElement = new NgiPolygonElement();
        		
        		line = reader.readLine();
        		if(!line.startsWith("NUMPARTS")) throw new NgiParseException();
                int numParts = Integer.parseInt(line.substring(9));
                polygonElement.vertexData = new double[numParts][][];
                
                for(int p = 0; p < numParts; ++p) {
                	vertexCount = Integer.parseInt(reader.readLine());
                	polygonElement.vertexData[p] = new double[vertexCount][];
                	
                	for(int v = 0; v < vertexCount; ++v) {
                		polygonElement.vertexData[p][v] = new double[dimensions];
                    	String[] vertexStringArr = reader.readLine().split(" ");
                    	
                    	for(int d = 0; d < dimensions; ++d) {
                    		polygonElement.vertexData[p][v][d] = Double.parseDouble(vertexStringArr[d]);
                    	}
                    }
                }
                
                line = reader.readLine();
                if(!line.startsWith("REGIONGATTR")) throw new NgiParseException();
                attributeIndex = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
                polygonElement.attribute = layer.header.polygonAttributes.get(attributeIndex);
                
        		return polygonElement;
        		
        	default:
        		throw new NgiParseException();
        }
        
    }

	private NgiHeader readHeader() throws IOException {
        NgiHeader result = new NgiHeader();
        headerLoop: while(reader.ready()) {
            String line = reader.readLine();
            switch(line) {
                case "$VERSION":
                    result.version = this.readIntegerVariable();
                    break;
                case "$GEOMETRIC_METADATA":
                    this.readGeometricMetadata(result);
                    break;
                case "$POINT_REPRESENT":
                    result.pointAttributes = this.readPointAttributes();
                    break;
                case "$LINE_REPRESENT":
                    result.lineAttributes = this.readLineAttributes();
                    break;
                case "$REGION_REPRESENT":
                    result.polygonAttributes = this.readPolygonAttributes();
                    break;
                case "$TEXT_REPRESENT":
                	// I'm not going to implement this, since it's not necessary for the generator.
                	// But for the future (I guess), I'll just leave a TODO here.
                    break;
                case "<END>":
                    break headerLoop;
            }
        }
        return result;
    }

    private Map<Integer, NgiPointElement.Attr> readPointAttributes() throws IOException {
        Map<Integer, NgiPointElement.Attr> attributes = new HashMap<>();
        while(reader.ready()) {
            String line = reader.readLine();
            if(line.equals("$END")) break;
            
            int firstSpace = line.indexOf(' ');
            int index = Integer.parseInt(line.substring(0, firstSpace));
            String attr = line.substring(firstSpace + 1);
            
            String[] parameters = attr.substring(attr.indexOf('('), attr.lastIndexOf(')')).split(",");
            
            NgiPointElement.Attr attribute = new NgiPointElement.Attr();
            attribute.type = parameters[0];
            attribute.unknownFloat = Double.parseDouble(parameters[1].replace(" ", ""));
            attribute.color = Integer.parseInt(parameters[2].replace(" ", ""));
            
            attributes.put(index, attribute);
        }
        return attributes;
    }

    private Map<Integer, NgiLineElement.Attr> readLineAttributes() throws IOException {
        Map<Integer, NgiLineElement.Attr> attributes = new HashMap<>();
        while(reader.ready()) {
            String line = reader.readLine();
            if(line.equals("$END")) break;
            
            int firstSpace = line.indexOf(' ');
            int index = Integer.parseInt(line.substring(0, firstSpace));
            String attr = line.substring(firstSpace + 1);
            
            String[] parameters = attr.substring(attr.indexOf('('), attr.lastIndexOf(')')).split(",");
            
            NgiLineElement.Attr attribute = new NgiLineElement.Attr();
            attribute.type = parameters[0];
            attribute.thickness = Integer.parseInt(parameters[1].replace(" ", ""));
            attribute.color = Integer.parseInt(parameters[2].replace(" ", ""));
            
            attributes.put(index, attribute);
        }
        return attributes;
    }

    private Map<Integer, NgiPolygonElement.Attr> readPolygonAttributes() throws IOException {
        Map<Integer, NgiPolygonElement.Attr> attributes = new HashMap<>();
        while(reader.ready()) {
            String line = reader.readLine();
            if(line.equals("$END")) break;
            
            int firstSpace = line.indexOf(' ');
            int index = Integer.parseInt(line.substring(0, firstSpace));
            String attr = line.substring(firstSpace + 1);
            
            String[] parameters = attr.substring(attr.indexOf('('), attr.lastIndexOf(')')).split(",");
            
            NgiPolygonElement.Attr attribute = new NgiPolygonElement.Attr();
            attribute.lineType = parameters[0];
            attribute.thickness = Integer.parseInt(parameters[1].replace(" ", ""));
            attribute.lineColor = Integer.parseInt(parameters[2].replace(" ", ""));
            attribute.fillType = parameters[3].replace(" ", "");
            attribute.color1 = Integer.parseInt(parameters[4].replace(" ", ""));
            attribute.color2 = Integer.parseInt(parameters[5].replace(" ", ""));
            
            attributes.put(index, attribute);
        }
        return attributes;
    }

    private void readGeometricMetadata(NgiHeader header) throws IOException {
        while(reader.ready()) {
            String line = reader.readLine();
            if(line.startsWith("MASK(") && line.endsWith(")")) {
                // TODO figure out how to deal with this
            	// or not, I mean why would this be necessary smh
            }
            else if(line.startsWith("DIM(") && line.endsWith(")")) {
                header.dimensions = Integer.parseInt(line.substring(4, line.length() - 1));
            }
            else if(line.startsWith("BOUND(") && line.endsWith(")")) {
                String[] args = line.substring(6, line.length() - 1).split(",");
                double[] min = new double[header.dimensions], max = new double[header.dimensions];
                for(int i=0;i<header.dimensions;i++) {
                    min[i] = Double.parseDouble(args[i]);
                    max[i] = Double.parseDouble(args[header.dimensions + i]);
                }
                header.bound = new NgiHeader.Boundary(min, max);
            }
            if(line.equals("$END")) break;
        }
    }

    private int readIntegerVariable() throws IOException {
        String line = reader.readLine();
        int result = Integer.parseInt(line);
        line = reader.readLine();
        if(!line.equals("$END")) throw new NgiParseException();
        return result;
    }

    private String readStringVariable() throws IOException {
        String result = reader.readLine();
        if(!result.startsWith("\"") || !result.endsWith("\""))
            throw new NgiParseException();
        result = result.substring(1, result.length() - 1);
        String line = reader.readLine();
        if(!line.equals("$END")) throw new NgiParseException();
        return result;
    }
    
    public static void main(String[] args) throws IOException {
		File file = new File("(B010)수치지도_376081986_2018_00000816502746.ngi");
		List<NgiLayer> layers = new NgiParser().parse(file);
		System.out.println(layers.size());
		for(NgiLayer layer : layers) {
			if(layer.name.startsWith("B001")) continue;
			System.out.println(layer.name);
			for(Map.Entry<Integer, NgiElement<?>> entry : layer.data.entrySet()) {
				NgiElement<?> value = entry.getValue();
				if(value instanceof NgiPolygonElement) {
					System.out.println(entry.getKey() + ": " + Arrays.deepToString(((NgiPolygonElement) value).vertexData));
					System.out.println(" " + ((NgiPolygonElement.Attr) value.attribute).thickness);
				}
			}
		}
	}

}
