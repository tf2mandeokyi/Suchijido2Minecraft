package com.mndk.scjdmc.util;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScjdMapIndexUtils {

    private static final CoordinateReferenceSystem EPSG5185;
    private static final CoordinateReferenceSystem EPSG5186;
    private static final CoordinateReferenceSystem EPSG5187;
    private static final CoordinateReferenceSystem EPSG5188;

    private static final Pattern SCJD_INDEX_FILENAME_PATTERN = Pattern.compile("^\\(.{4}\\)수치지도_(\\d+)_");
    private static final Pattern CONTAINS_ALPHABET = Pattern.compile("[A-Za-z]");


    public static String getMapIndexFromFileName(String fileName) {
        Matcher matcher = SCJD_INDEX_FILENAME_PATTERN.matcher(fileName);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return fileName;
    }

    public static boolean validateIndexName(String index) {
        try {
            indexToPosition(index);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public static int[] indexToPosition(String index) {
        int scale = getTileScale(index);
        switch(scale) {
            case 50000: return get50000TilePos(index);
            case 25000: return get25000TilePos(index);
            case 10000: return get10000TilePos(index);
            case 5000: return get5000TilePos(index);
            case 1000: return get1000TilePos(index);
            default: throw new IllegalArgumentException("Unsupported tile scale: " + scale + " (index: " + index + ")");
        }
    }

    public static String positionToIndex(int[] pos, int scale) {
        switch(scale) {
            case 50000: return get50000TileIndex(pos);
            case 25000: return get25000TileIndex(pos);
            case 10000: return get10000TileIndex(pos);
            case 5000: return get5000TileIndex(pos);
            case 1000: return get1000TileIndex(pos);
            default: throw new IllegalArgumentException("Unsupported tile scale: " + scale);
        }
    }

    public static double[] positionToLongLat(int[] tilePosition, int scale, boolean center) {
        switch(scale) {
            case 50000: case 25000: case 10000: case 5000: case 1000: break;
            default: throw new IllegalArgumentException("Unsupported tile scale: " + scale);
        }
        double mapScale = 200000 / (double) scale;
        double offset = center ? 0.5 : 0;
        return new double[] { (tilePosition[0] + offset) / mapScale, (tilePosition[1] + offset) / mapScale };
    }

    public static int[] longLatToPosition(double[] longLat, int scale) {
        switch(scale) {
            case 50000: case 25000: case 10000: case 5000: case 1000: break;
            default: throw new IllegalArgumentException("Unsupported tile scale: " + scale);
        }
        double mapScale = 200000 / (double) scale;
        return new int[] { (int) Math.floor(longLat[0] * mapScale), (int) Math.floor(longLat[1] * mapScale) };
    }

    public static BoundingBox getBoudingBox(String index, boolean aBitLarger) {
        int scale = getTileScale(index);
        int[] pos = indexToPosition(index);
        double[] longLatMin, longLatMax;
        if(aBitLarger) {
            longLatMin = ScjdMapIndexUtils.positionToLongLat(new int[] { pos[0] - 1, pos[1] - 1 }, scale, true);
            longLatMax = ScjdMapIndexUtils.positionToLongLat(new int[] { pos[0] + 1, pos[1] + 1 }, scale, true);
        }
        else {
            longLatMin = ScjdMapIndexUtils.positionToLongLat(pos, scale, false);
            longLatMax = ScjdMapIndexUtils.positionToLongLat(new int[] { pos[0] + 1, pos[1] + 1 }, scale, false);
        }
        return new ReferencedEnvelope(longLatMin[0], longLatMax[0], longLatMin[1], longLatMax[1], null);
    }

    public static List<String> getContainingIndexes(BoundingBox bbox, int scale) {
        final int[] minPos = longLatToPosition(new double[] { bbox.getMinX(), bbox.getMinY() }, scale);
        final int[] maxPos = longLatToPosition(new double[] { bbox.getMaxX(), bbox.getMaxY() }, scale);
        List<String> indexes = new ArrayList<>();
        for(int x = minPos[0]; x <= maxPos[0]; x++) {
            for(int y = minPos[1]; y <= maxPos[1]; y++) {
                indexes.add(positionToIndex(new int[] { x, y }, scale));
            }
        }
        return indexes;
    }

    public static int getTileScale(String index) {
        int length = index.length();
        switch(length) {
            case 5: return 50000;
            case 6: return 25000;
            case 7: return 10000;
            case 8: if(!CONTAINS_ALPHABET.matcher(index).find()) return 5000;
            case 9: return 1000;
            default: throw new IllegalArgumentException("Unsupported index: " + index);
        }
    }


    // INDEX 50000
    private static int[] get50000TilePos(String index) {
        if(index.length() != 5) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        String latitudeString = index.substring(0, 2);
        char longitudeChar = index.charAt(2);

        int x, y = Integer.parseInt(latitudeString);

        if(longitudeChar >= '0' && longitudeChar <= '2') {
            x = 130 + (longitudeChar - '0');
        }
        else {
            x = 120 + (longitudeChar - '0');
        }

        return digitToPosition(new int[] { x, y }, Integer.parseInt(index.substring(3, 5)), 4, index);
    }
    private static String get50000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%02d", positionToDigit(newPos, 4));
        return String.format("%d%d", newPos[1], newPos[0] % 10) + temp;
    }


    // INDEX 25000
    private static int[] get25000TilePos(String index) {
        if(index.length() != 6) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get50000TilePos(index.substring(0, 5)),
                Integer.parseInt(index.substring(5, 6)), 2, index);
    }
    private static String get25000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%d", positionToDigit(newPos, 2));
        return get50000TileIndex(newPos) + temp;
    }


    // INDEX 10000
    private static int[] get10000TilePos(String index) {
        if(index.length() != 7) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get50000TilePos(index.substring(0, 5)),
                Integer.parseInt(index.substring(5, 7)), 5, index);
    }
    private static String get10000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%02d", positionToDigit(newPos, 5));
        return get50000TileIndex(newPos) + temp;
    }


    // INDEX 5000
    private static int[] get5000TilePos(String index) {
        if(index.length() != 8) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get50000TilePos(index.substring(0, 5)),
                Integer.parseInt(index.substring(5, 8)), 10, index);
    }
    private static String get5000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%03d", positionToDigit(newPos, 10));
        return get50000TileIndex(newPos) + temp;
    }


    // INDEX 1000
    private static int[] get1000TilePos(String index) {
        if (index.length() != 9) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get10000TilePos(index.substring(0, 7)),
                Integer.parseInt(index.substring(7, 9)), 10, index);
    }
    private static String get1000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%03d", positionToDigit(newPos, 10));
        return get10000TileIndex(newPos) + temp;
    }


    private static int[] digitToPosition(int[] parentPosition, int digit, int whLength, String indexReference) {
        digit--;
        if(digit < 0 || digit > whLength * whLength - 1) {
            throw new IllegalArgumentException("Illegal tile index: " + indexReference);
        }
        return new int[] {
                whLength * parentPosition[0] + digit % whLength,
                whLength * parentPosition[1] + (whLength - 1) - (digit / whLength)
        };
    }

    private static int positionToDigit(int[] newPosition, int whLength) {
        int x = newPosition[0] % whLength, y = newPosition[1] % whLength;
        newPosition[0] /= whLength; newPosition[1] /= whLength;
        return ((whLength - 1) - y) * whLength + x + 1;
    }

    public static CoordinateReferenceSystem getCoordinateReferenceSystemFromIndex(String index) {
        char number = index.charAt(2);
        switch(number) {
            case '5': return EPSG5185;
            case '6': case '7': return EPSG5186;
            case '8': case '9': return EPSG5187;
            case '0': case '1': return EPSG5188;
        }
        return null;
    }

    static {
        try {
            EPSG5185 = CRS.decode("epsg:5185");
            EPSG5186 = CRS.decode("epsg:5186");
            EPSG5187 = CRS.decode("epsg:5187");
            EPSG5188 = CRS.decode("epsg:5188");
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }
}
