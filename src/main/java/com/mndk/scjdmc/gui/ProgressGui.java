package com.mndk.scjdmc.gui;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class ProgressGui extends JFrame {

    private BoundingBox boundingBox = null;
    private final Map<BoundingBox, Color> map;
    private boolean closed = false;

    public ProgressGui(int xSize, int ySize) {
        this.map = new HashMap<>();

        this.setSize(xSize, ySize);
        this.setTitle("Progress");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new Panel());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void setStatus(BoundingBox bbox, Color color) {
        if (closed) return;
        synchronized (map) {
            if(boundingBox == null) boundingBox = new ReferencedEnvelope(bbox);
            else boundingBox.include(bbox);
            map.put(bbox, color);
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
            Dimension dimension = ProgressGui.this.getSize();
            this.setSize(dimension);
            if (dimension.width <= 0 || dimension.height <= 0 || closed || boundingBox == null) {
                this.repaint();
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            synchronized (map) {

                double bigger = Math.max(boundingBox.getWidth(), boundingBox.getHeight());
                double xScale = dimension.width  / bigger;
                double yScale = dimension.height / bigger;

                for (Map.Entry<BoundingBox, Color> entry : map.entrySet()) {
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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1000, 1000);
        }
    }

}