package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.projection.Korea2010BeltProjection;
import com.mndk.scjd2mc.core.projection.Projections;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScjdIndexManager {
    private static final Pattern generalMapIdPattern = Pattern.compile("^\\(.{4}\\)수치지도_(\\d+)_");
    private static final Pattern containsAlphabet = Pattern.compile("[A-Za-z]");

    public static String getMapIndexFromFileName(String fileName) {
        Matcher matcher = generalMapIdPattern.matcher(fileName);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return fileName;
    }

    public static int[] getTilePosition(String index) {
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

    public static String getTileIndex(int[] pos, int scale) {
        switch(scale) {
            case 50000: return get50000TileIndex(pos);
            case 25000: return get25000TileIndex(pos);
            case 10000: return get10000TileIndex(pos);
            case 5000: return get5000TileIndex(pos);
            case 1000: return get1000TileIndex(pos);
            default: throw new IllegalArgumentException("Unsupported tile scale: " + scale);
        }
    }

    public static int getTileScale(String index) {
        int length = index.length();
        switch(length) {
            case 5: return 50000;
            case 6: return 25000;
            case 7: return 10000;
            case 8:
                if(containsAlphabet.matcher(index).find()) {
                    return 2500;
                }
                return 5000;
            case 9: return 1000;
            case 10: return 500;
            default:
                throw new IllegalArgumentException("Unsupported index: " + index);
        }
    }


    public static int[] get50000TilePos(String index) {
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
    public static String get50000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%02d", positionToDigit(newPos, 4));
        return String.format("%d%d", newPos[1], newPos[0] % 10) + temp;
    }


    public static int[] get25000TilePos(String index) {
        if(index.length() != 6) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get50000TilePos(index.substring(0, 5)),
                Integer.parseInt(index.substring(5, 6)), 2, index);
    }
    public static String get25000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%d", positionToDigit(newPos, 2));
        return get50000TileIndex(newPos) + temp;
    }


    public static int[] get10000TilePos(String index) {
        if(index.length() != 7) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get50000TilePos(index.substring(0, 5)),
                Integer.parseInt(index.substring(5, 7)), 5, index);
    }
    public static String get10000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%02d", positionToDigit(newPos, 5));
        return get50000TileIndex(newPos) + temp;
    }


    public static int[] get5000TilePos(String index) {
        if(index.length() != 8) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get50000TilePos(index.substring(0, 5)),
                Integer.parseInt(index.substring(5, 8)), 10, index);
    }
    public static String get5000TileIndex(int[] pos) {
        int[] newPos = pos.clone();
        String temp = String.format("%03d", positionToDigit(newPos, 10));
        return get50000TileIndex(newPos) + temp;
    }


    public static int[] get1000TilePos(String index) {
        if (index.length() != 9) {
            throw new IllegalArgumentException("Illegal tile index: " + index);
        }
        return digitToPosition(get10000TilePos(index.substring(0, 7)),
                Integer.parseInt(index.substring(7, 9)), 10, index);
    }
    public static String get1000TileIndex(int[] pos) {
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

    public static int positionToDigit(int[] newPosition, int whLength) {
        int x = newPosition[0] % whLength, y = newPosition[1] % whLength;
        newPosition[0] /= whLength; newPosition[1] /= whLength;
        return ((whLength - 1) - y) * whLength + x + 1;
    }

    public static Korea2010BeltProjection getProjectionFromMapName(String fileName) {

        fileName = getMapIndexFromFileName(fileName);
        char number = fileName.charAt(2);

        if(number == '5') {
            return Projections.KOREA2010_WEST;
        } else if(number == '6' || number == '7') {
            return Projections.KOREA2010_CENTRAL;
        } else if(number == '8' || number == '9') {
            return Projections.KOREA2010_EAST;
        } else if(number == '0' || number == '1'){
            return Projections.KOREA2010_EASTSEA;
        }
        return null;
    }
}
