package com.mndk.scjdmc.util;

import org.opengis.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgressGui extends JFrame {

    private BoundingBox boundingBox = null;
    private final int xSize, ySize;
    private final List<Map.Entry<BoundingBox, Color>> list;
    private boolean closed = false;

    public ProgressGui(int xSize, int ySize) {
        this.list = new ArrayList<>();
        this.xSize = xSize;
        this.ySize = ySize;

        this.setSize(xSize, ySize);
        this.setTitle("Progress");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new Panel());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void addStatus(BoundingBox bbox, Color color) {
        if(!closed) {
            synchronized (list) {
                if(boundingBox == null) boundingBox = bbox;
                else boundingBox.include(bbox);
                list.add(new AbstractMap.SimpleEntry<>(bbox, color));
            }
        }
    }

    public void close() {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        this.closed = true;
    }

    private class Panel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(xSize > 0 && ySize > 0 && !closed && boundingBox != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                synchronized (list) {

                    double bigger = Math.max(boundingBox.getWidth(), boundingBox.getHeight());
                    double xScale = xSize / bigger;
                    double yScale = ySize / bigger;

                    for (Map.Entry<BoundingBox, Color> entry : list) {
                        BoundingBox bbox = entry.getKey();
                        g2d.setColor(entry.getValue());
                        g2d.fillRect(
                                (int) Math.round((bbox.getMinX() - boundingBox.getMinX()) * xScale),
                                (int) Math.round((boundingBox.getMaxY() - bbox.getMaxY()) * yScale),
                                (int) Math.round(bbox.getWidth() * xScale),
                                (int) Math.round(bbox.getHeight() * yScale)
                        );
                    }
                }
                this.repaint();
            }

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1000, 1000);
        }
    }

}