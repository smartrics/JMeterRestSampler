/*  Copyright 2009 Fabrizio Cannizzo
 *
 *  This file is part of JMeterRestSampler.
 *
 *  JMeterRestSampler (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  BSD License
 *
 *  You should have received a copy of the BSD License
 *  along with JMeterRestSampler.  If not, see <http://opensource.org/licenses/bsd-license.php>.
 *
 *  If you want to contact the author please see http://smartrics.blogspot.com
 */
package smartrics.jmeter.sampler.gui;

import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Wraps a jChart graph on a Swing panel to ease embedding in the JmxVisualiser.
 * The graph is represented as an area chart for the data in column 1 and a line
 * chart for data in column 2.
 */
public class JmxGraphPanel extends JPanel {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = -4967670058219091920L;
    private ChartWrapper graph;

    /**
     *
     */
    public JmxGraphPanel() {
        super();
    }

    /**
     * @param layout
     */
    public JmxGraphPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * @param layout
     * @param isDoubleBuffered
     */
    public JmxGraphPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public ChartWrapper getGraph() {
        return graph;
    }

    public void setGraph(ChartWrapper graph) {
        this.graph = graph;
    }

    public void paintComponent() {
        try {
            graph.drawData();
        } catch (RuntimeException e) {
            log.info(String.format("Exception when drowing graph '%s'", graph.getTitle()), e);
        }
    }

    public void paintComponent(Graphics g) {
        try {
            graph.drawData(g);
        } catch (RuntimeException e) {
            // ignore this time around
            log.info(String.format("Exception when drowing graph '%s'", graph.getTitle()), e);
        }
    }

}
