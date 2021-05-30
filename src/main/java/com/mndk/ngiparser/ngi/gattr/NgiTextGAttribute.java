package com.mndk.ngiparser.ngi.gattr;

public class NgiTextGAttribute implements NgiShapeGAttribute {
    public String fontName;
    public double fontSize;
    public int color;
    public double unknownFloat;
    public int unknownInt;

    public void from(String[] args) {
        this.fontName = args[0];
        this.fontSize = Double.parseDouble(args[1]);
        this.color = Integer.parseInt(args[2]);
        this.unknownFloat = Double.parseDouble(args[2]);
        this.unknownInt = Integer.parseInt(args[2]);
    }
}
