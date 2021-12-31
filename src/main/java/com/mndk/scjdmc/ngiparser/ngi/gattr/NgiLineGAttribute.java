package com.mndk.scjdmc.ngiparser.ngi.gattr;

public class NgiLineGAttribute implements NgiShapeGAttribute {
    public String type;
    public int thickness;
    public int color;

    public void from(String[] args) {
        this.type = args[0];
        this.thickness = Integer.parseInt(args[1]);
        this.color = Integer.parseInt(args[2]);
    }
}
