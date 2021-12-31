package com.mndk.scjdmc.ngiparser.ngi.gattr;

public class NgiRegionGAttribute implements NgiShapeGAttribute {
    public String lineType;
    public int thickness;
    public int lineColor;
    public String fillType;
    public int color1, color2; // TODO figure out what 2 of these color variables represent

    public void from(String[] args) {
        this.lineType = args[0];
        this.thickness = Integer.parseInt(args[1]);
        this.lineColor = Integer.parseInt(args[2]);
        this.fillType = args[3];
        this.color1 = Integer.parseInt(args[4]);
        this.color2 = Integer.parseInt(args[5]);
    }
}
