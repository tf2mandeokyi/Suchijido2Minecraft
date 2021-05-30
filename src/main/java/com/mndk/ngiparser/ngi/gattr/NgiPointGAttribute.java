package com.mndk.ngiparser.ngi.gattr;

public class NgiPointGAttribute implements NgiShapeGAttribute {
    public String type;
    public double unknownFloat;
    public int color;

    public void from(String[] args) {
        this.type = args[0];
        this.unknownFloat = Double.parseDouble(args[1]);
        this.color = Integer.parseInt(args[2]);
    }
}
