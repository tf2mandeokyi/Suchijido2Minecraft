package com.mndk.scjdmc.util;

import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProgressGui extends JFrame {

    private int xMin = Integer.MAX_VALUE, yMin = Integer.MAX_VALUE, xMax = Integer.MIN_VALUE, yMax = Integer.MIN_VALUE;
    private int xSize = -1, ySize = -1;
    private final Map<IntPos, Byte> posMap;
    private final int pixelSizeX, pixelSizeY;
    private boolean triggerPaint = true;

    public static final byte NO_DATA = 1, SUCCESS = 2, ERROR = 3;

    public ProgressGui(int pixelSizeX, int pixelSizeY) {
        this.posMap = new HashMap<>();
        this.pixelSizeX = pixelSizeX;
        this.pixelSizeY = pixelSizeY;

        this.setSize(1000, 1000);
        this.setTitle("Progress");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(this.new Panel());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public ProgressGui(int pixelSize) {
        this(pixelSize, pixelSize);
    }

    public void addStatus(int x, int y, byte status) {
        if(x > xMax) xMax = x;
        if(x < xMin) xMin = x;
        if(y > yMax) yMax = y;
        if(y < yMin) yMin = y;
        synchronized (posMap) {
            this.posMap.put(new IntPos(x, y), status);
        }
        this.xSize = xMax - xMin + 1;
        this.ySize = yMax - yMin + 1;
        this.triggerPaint = true;
    }

    private class Panel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            if(xSize > 0 && ySize > 0) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                synchronized (posMap) {
                    for (Map.Entry<IntPos, Byte> entry : posMap.entrySet()) {
                        IntPos pos = entry.getKey();
                        switch (entry.getValue()) {
                            case NO_DATA:
                                g.setColor(Color.ORANGE); break;
                            case SUCCESS:
                                g.setColor(Color.GREEN); break;
                            case ERROR:
                                g.setColor(Color.RED); break;
                            default:
                                g.setColor(Color.WHITE); break;
                        }
                        g2d.fillRect((pos.x - xMin) * pixelSizeX, (yMax - pos.y) * pixelSizeY, pixelSizeX, pixelSizeY);
                    }
                }
                triggerPaint = false;
                this.repaint();
            }

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1000, 1000);
        }
    }

    @RequiredArgsConstructor
    private static class IntPos {
        final int x, y;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntPos intPos = (IntPos) o;
            return x == intPos.x && y == intPos.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

}