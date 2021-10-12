package com.mndk.scjd2mc.core.util;

import javax.swing.*;
import java.awt.*;

public class ProgressGui extends JFrame {

    private final int xMin, yMax;
    private final int xSize, ySize;
    private final byte[][] progress;
    private final int pixelSize;

    public static final byte NO_DATA = 1, SUCCESS = 2, ERROR = 3;

    public ProgressGui(int xMin, int yMin, int xMax, int yMax, int pixelSize) {
        this.xMin = xMin;
        this.yMax = yMax;
        this.xSize = xMax - xMin + 1;
        this.ySize = yMax - yMin + 1;
        this.progress = new byte[this.ySize][this.xSize];
        this.pixelSize = pixelSize;
        this.setSize(this.xSize * pixelSize, this.ySize * pixelSize);
        this.setTitle("Progress");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(this.new Panel());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void setStatus(int x, int y, byte status) {
        this.progress[yMax - y][x - xMin] = status;
    }

    private class Panel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for(int y = 0; y < ySize; ++y) {
                for(int x = 0; x < xSize; ++x) {
                    byte prog = progress[y][x];
                    switch(prog) {
                        case NO_DATA: g.setColor(Color.ORANGE); break;
                        case SUCCESS: g.setColor(Color.GREEN); break;
                        case ERROR: g.setColor(Color.RED); break;
                        default: g.setColor(Color.WHITE); break;
                    }
                    g2d.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
                }
            }

            this.repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(xSize * pixelSize, ySize * pixelSize);
        }
    }

}