package com.mndk.scjdmc.ngiparser;

import com.mndk.scjdmc.ngiparser.nda.NdaDataColumn;
import com.mndk.scjdmc.ngiparser.ngi.NgiHeader;
import com.mndk.scjdmc.ngiparser.ngi.NgiLayer;
import com.mndk.scjdmc.ngiparser.ngi.NgiParserResult;
import com.mndk.scjdmc.ngiparser.ngi.element.*;
import com.mndk.scjdmc.ngiparser.ngi.gattr.*;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVector;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVectorList;
import com.mndk.scjdmc.ngiparser.util.DebuggableLineReader;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NgiParser {

	private DebuggableLineReader ngiReader, ndaReader;
	private NgiParserResult result;

	/**
	 * Parses ngi file, and if available, it also parses same-filename-having nda file.
	 * @param ngiFilePath The path of the .ngi file
	 * @param encoding The encoding type
	 * */
	public static NgiParserResult parse(String ngiFilePath, String encoding, boolean requireNdaFile) throws IOException {
		if(!FilenameUtils.getExtension(ngiFilePath).equals("ngi"))
			throw new FileNotFoundException(ngiFilePath + " not found!");

		String ndaFilePath = replaceLast(ngiFilePath, ".ngi", ".nda");
		if(!new File(ndaFilePath).exists()) {
			if(requireNdaFile) throw new FileNotFoundException(ndaFilePath + " not found!");
			ndaFilePath = null;
		}

		return parse(ngiFilePath, ndaFilePath, encoding);
	}

	/**
	 * Parses both ngi file and nda file, and then combines them.
	 * @param ngiFilePath The path of the .ngi file
	 * @param ndaFilePath The path of the .nda file
	 * @param encoding The encoding type
	 * */
	public static NgiParserResult parse(
			String ngiFilePath,
			String ndaFilePath,
			String encoding
	) throws IOException {
		return new NgiParser().parse(
				new DebuggableLineReader(
						new InputStreamReader(new FileInputStream(ngiFilePath), encoding),
						new File(ngiFilePath).getName()
				),

				ndaFilePath == null ? null : new DebuggableLineReader(
						new InputStreamReader(new FileInputStream(ndaFilePath), encoding),
						new File(ndaFilePath).getName()
				)
		);
	}

	/**
	 * Parses both ngi file and nda file, and then combines them.
	 * @param ngiStream The stream of the .ngi file
	 * @param ndaStream The stream of the .nda file
	 * @param encoding The encoding type
	 * */
	public static NgiParserResult parse(
			InputStream ngiStream,
			InputStream ndaStream,
			String encoding
	) throws IOException {
		return new NgiParser().parse(
				new DebuggableLineReader(
						new InputStreamReader(ngiStream, encoding),
						"ngi"
				),

				ndaStream == null ? null : new DebuggableLineReader(
						new InputStreamReader(ndaStream, encoding),
						"nda"
				)
		);
	}

	private NgiParserResult parse(DebuggableLineReader ngiReader, DebuggableLineReader ndaReader) throws IOException {
		this.ngiReader = ngiReader;
		this.ndaReader = ndaReader;
		result = new NgiParserResult(new HashMap<>(), new HashMap<>());

		String line;

		try {
			while (ngiReader.ready()) {
				line = ngiReader.readLine();
				if (line.equals("<LAYER_START>")) {
					readNgiLayer();
				}
			}
		} catch(Throwable t) {
			throw ngiReader.getException(t);
		}

		if(this.ndaReader != null) {
			try {
				while (ndaReader.ready()) {
					line = ndaReader.readLine();
					if (line.equals("<LAYER_START>")) {
						readNdaLayer();
					}
				}
			} catch(Throwable t) {
				throw ndaReader.getException(t);
			}
		}

		return result;
	}

	private void readNgiLayer() throws IOException {
		NgiLayer layer = new NgiLayer();
		int id = 0;
		layerLoop: while(ngiReader.ready()) {
			String line = ngiReader.readLine();
			switch(line) {
				case "$LAYER_ID":
					id = readIntegerVariable(ngiReader);
					break;
				case "$LAYER_NAME":
					layer.name = readStringVariable(ngiReader);
					break;
				case "<HEADER>":
					layer.header = this.readNgiHeader();
					break;
				case "<DATA>":
					this.readNgiData(layer);
					break;
				case "<LAYER_END>":
					break layerLoop;
				default:
					throw ngiReader.getException();
			}
		}
		result.addLayer(id, layer);
	}

	private void readNdaLayer() throws IOException {
		int layerId = 0;

		layerLoop: while(ndaReader.ready()) {
			String line = ndaReader.readLine();
			switch(line) {
				case "$LAYER_ID":
					layerId = readIntegerVariable(ndaReader);
					break;
				case "$LAYER_NAME":
					readStringVariable(ndaReader);
					// I mean, the name should be already declared in .ngi file,
					// so you wouldn't need to read this again.
					break;
				case "<HEADER>":
					this.readNdaHeader(layerId);
					break;
				case "<DATA>":
					this.readNdaData(layerId);
					break;
				case "<LAYER_END>":
					break layerLoop;
				default:
					throw ndaReader.getException();
			}
		}
	}

	private NgiHeader readNgiHeader() throws IOException {
		NgiHeader result = new NgiHeader();
		headerLoop: while(ngiReader.ready()) {
			String line = ngiReader.readLine();
			switch(line) {
				case "$VERSION":
					result.version = readIntegerVariable(ngiReader);
					break;
				case "$GEOMETRIC_METADATA":
					this.readNgiGeometricMetadata(result);
					break;
				case "$POINT_REPRESENT":
					result.symbolGAttrs = this.readNgiHeaderGAttrs(new NgiPointGAttribute());
					break;
				case "$LINE_REPRESENT":
					result.lineGAttrs = this.readNgiHeaderGAttrs(new NgiLineGAttribute());
					break;
				case "$REGION_REPRESENT":
					result.regionGAttrs = this.readNgiHeaderGAttrs(new NgiRegionGAttribute());
					break;
				case "$TEXT_REPRESENT":
					result.textGAttrs = this.readNgiHeaderGAttrs(new NgiTextGAttribute());
					break;
				case "<END>":
					break headerLoop;
			}
		}
		return result;
	}

	private <T extends NgiShapeGAttribute> Map<Integer, T> readNgiHeaderGAttrs(T newGAttrInstance) throws IOException {
		Map<Integer, T> attributes = new HashMap<>();
		while(ngiReader.ready()) {
			String line = ngiReader.readLine();
			if(line.equals("$END")) break;

			int firstSpace = line.indexOf(' ');
			int index = Integer.parseInt(line.substring(0, firstSpace));
			String attr = line.substring(firstSpace + 1);

			String[] parameters = attr.substring(attr.indexOf('('), attr.lastIndexOf(')')).split(",\\s*");

			newGAttrInstance.from(parameters);

			attributes.put(index, newGAttrInstance);
		}
		return attributes;
	}

	private void readNgiGeometricMetadata(NgiHeader header) throws IOException {
		while(ngiReader.ready()) {
			String line = ngiReader.readLine();
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

	private void readNdaHeader(int layerId) throws IOException {
		NdaDataColumn[] columnList = new NdaDataColumn[0];
		headerLoop: while(ndaReader.ready()) {
			String line = ndaReader.readLine();
			switch(line) {
				case "$VERSION":
					// Skip version declaration
					break;
				case "$ASPATIAL_FIELD_DEF":
					columnList = this.readNdaHeaderColumns();
					break;
				case "<END>":
					break headerLoop;
			}
		}
		result.getLayer(layerId).header.columns = columnList;
	}

	private NdaDataColumn[] readNdaHeaderColumns() throws IOException {
		List<NdaDataColumn> result = new ArrayList<>();
		while(ndaReader.ready()) {
			String line = ndaReader.readLine();
			if(line.equals("$END")) break;
			else if(line.startsWith("ATTRIB")) {
				String columnData = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
				result.add(new NdaDataColumn(parseDataParametersToObject(columnData)));
			}
		}
		return result.toArray(new NdaDataColumn[0]);
	}

	private void readNgiData(NgiLayer layer) throws IOException {
		Map<Integer, NgiRecord<?>> result = new HashMap<>();
		while(ngiReader.ready()) {
			String line = ngiReader.readLine();
			if(line.startsWith("$RECORD")) {
				int recordIndex = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
				NgiRecord<?> element = readNgiRecord(layer);
				result.put(recordIndex, element);
				this.result.addElement(recordIndex, element);
			}
			else if(line.equals("<END>")) break;
		}
		layer.data = result;
	}

	private void readNdaData(int layerId) throws IOException {
		while(ndaReader.ready()) {
			String line = ndaReader.readLine();
			if(line.startsWith("$RECORD")) {
				int recordIndex = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
				NgiRecord<?> element = result.getElement(recordIndex);
				if (element != null) { // If the record number exists in .ngi file
					String nextLine = ndaReader.readLine();
					element.rowData = parseDataParametersToObject(nextLine);
				}
			}
			else if(line.equals("<END>")) break;
		}
	}

	private NgiRecord<?> readNgiRecord(NgiLayer layer) throws IOException {

		String type = ngiReader.readLine();
		int dimensions = layer.header.dimensions;

		if(type.startsWith("TEXT")) {
			NgiText textElement = new NgiText(layer);
			textElement.text = type.substring(6, type.length() - 2);
			textElement.position = readVector(dimensions);
			textElement.gAttribute = (NgiTextGAttribute) getGAttribute(layer);

			return textElement;
		}

		else if(type.startsWith("MULTIPOLYGON")) {
			NgiMultiPolygon multiPolygonElement = new NgiMultiPolygon(layer);
			int polygons = Integer.parseInt(type.substring(13));
			multiPolygonElement.vertexData = readVectorMultiPolygon(polygons, dimensions);
			multiPolygonElement.gAttribute = (NgiRegionGAttribute) getGAttribute(layer);

			return multiPolygonElement;
		}

		else switch(type) {
			case "POINT":
				NgiPoint pointElement = new NgiPoint(layer);
				pointElement.position = readVector(dimensions);
				pointElement.gAttribute = (NgiPointGAttribute) getGAttribute(layer);

				return pointElement;

			case "LINESTRING":
				NgiLine lineElement = new NgiLine(layer);
				lineElement.lineData = readVectorLine(dimensions);
				lineElement.gAttribute = (NgiLineGAttribute) getGAttribute(layer);

				return lineElement;

			case "POLYGON":
				NgiPolygon polygonElement = new NgiPolygon(layer);
				polygonElement.vertexData = readVectorPolygon(dimensions);
				polygonElement.gAttribute = (NgiRegionGAttribute) getGAttribute(layer);

				return polygonElement;

			default:
				throw ngiReader.getException();
		}

	}

	private NgiShapeGAttribute getGAttribute(NgiLayer layer) throws IOException {
		String line = ngiReader.readLine();
		int attributeIndex = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));

		if(line.startsWith("REGIONGATTR")) {
			return layer.header.regionGAttrs.get(attributeIndex);
		}
		else if(line.startsWith("LINEGATTR")) {
			return layer.header.lineGAttrs.get(attributeIndex);
		}
		else if(line.startsWith("SYMBOLGATTR")) {
			return layer.header.symbolGAttrs.get(attributeIndex);
		}
		else if(line.startsWith("TEXTGATTR")) {
			return layer.header.textGAttrs.get(attributeIndex);
		}
		else {
			throw ngiReader.getException();
		}
	}

	private NgiVectorList[][] readVectorMultiPolygon(int polygons, int dimensions) throws IOException {
		NgiVectorList[][] result = new NgiVectorList[polygons][];
		for(int m = 0; m < polygons; ++m) {
			result[m] = readVectorPolygon(dimensions);
		}
		return result;
	}

	private NgiVectorList[] readVectorPolygon(int dimensions) throws IOException {

		String line = ngiReader.readLine();
		if(!line.startsWith("NUMPARTS")) {
			throw ngiReader.getException();
		}
		int numParts = Integer.parseInt(line.substring(9));

		NgiVectorList[] result = new NgiVectorList[numParts];

		for(int p = 0; p < numParts; ++p) {
			result[p] = readVectorLine(dimensions);
		}

		return result;
	}

	private NgiVectorList readVectorLine(int dimensions) throws IOException {

		int vertexCount = Integer.parseInt(ngiReader.readLine());
		NgiVector[] vertexListTemp = new NgiVector[vertexCount];

		for(int v = 0; v < vertexCount; ++v) {
			vertexListTemp[v] = readVector(dimensions);
		}
		return new NgiVectorList(vertexListTemp);
	}

	private NgiVector readVector(int dimensions) throws IOException {
		double[] vertexTemp = new double[dimensions];
		String[] vertexStringArr = ngiReader.readLine().split(" ");
		for(int d = 0; d < dimensions; ++d) {
			vertexTemp[d] = Double.parseDouble(vertexStringArr[d]);
		}
		return new NgiVector(vertexTemp);
	}

	private static int readIntegerVariable(DebuggableLineReader reader) throws IOException {
		String line = reader.readLine();
		int result = Integer.parseInt(line);
		line = reader.readLine();
		if(!line.equals("$END")) {
			throw reader.getException();
		}
		return result;
	}

	private static String readStringVariable(DebuggableLineReader reader) throws IOException {
		String result = reader.readLine();
		if(!result.startsWith("\"") || !result.endsWith("\"")) {
			throw reader.getException();
		}
		result = result.substring(1, result.length() - 1);
		String line = reader.readLine();
		if(!line.equals("$END")) {
			throw reader.getException();
		}
		return result;
	}

	private Object[] parseDataParametersToObject(String str) throws IOException {
		List<Object> result = new ArrayList<>();
		StringBuilder temp = new StringBuilder();
		boolean valueMode = false, stringMode = false;
		int n = str.length();

		for(int i = 0; i < n; ++i) {
			char c = str.charAt(i);
			if(!valueMode) {
				if(c == ' ') continue;
				else valueMode = true;
			}
			if(c == ',' && !stringMode) {
				result.add(checkType(temp.toString()));
				valueMode = stringMode = false;
				temp = new StringBuilder();
				continue;
			}
			if(c == '\\') {
				c = str.charAt(++i);
				if(c == '\\' || c == '\'' || c == '"') temp.append(c);
				else if(c == '\n') temp.append('\n');
				else if(c == '\r') temp.append('\r');
				else if(c == '\t') temp.append('\t');
				else if(c == '\b') temp.append('\b');
				else if(c == '\f') temp.append('\f');
				else throw ndaReader.getException("Invalid string escape \"" + c + "\"");
				continue;
			}
			if(c == '"') {
				stringMode = !stringMode;
			}

			temp.append(c);
		}
		result.add(checkType(temp.toString()));
		return result.toArray(new Object[0]);
	}

	private static Object checkType(String s) {
		if(s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		else if(s.equalsIgnoreCase("false") || s.equalsIgnoreCase("true")) {
			return Boolean.parseBoolean(s);
		}
		else if(s.matches("-?(\\d+)")) { // check integer
			return Integer.parseInt(s);
		}
		else if(s.matches("-?(\\d+\\.?|\\d*.\\d+)")) { // check float
			return Double.parseDouble(s);
		}
		else {
			return s;
		}
	}

	private static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
	}
}
