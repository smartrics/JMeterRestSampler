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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import smartrics.jmeter.sampler.JmxSampleResult;
import smartrics.jmeter.sampler.SaveGraphUtil;

/**
 * Visualises the results of sampling a JMX server for memory.
 *
 * Each server is represented by a graph and identified by it's uri. It captures
 * and display results from all JmxSampler set up to sample different servers.
 */
@SuppressWarnings("serial")
public class JmxVisualizer extends AbstractVisualizer implements SaveGraphListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private VerticalPanel graphsPanel;
    private Map<String, JmxGraphPanel> graphTable = Collections.synchronizedMap(new HashMap<String, JmxGraphPanel>());

    public JmxVisualizer() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        VerticalPanel groupPanel = new VerticalPanel();
        Container titlePanel = makeTitlePanel();
        groupPanel.add(titlePanel);
        // redo a filepanel for savegraph
        // - forces png
        // - files only
        // - add save button below
        // - this will listen to save file actions
        // - displays file chooser if file not chosen
        // - if file added by hand, must work
        SaveChartFilePanel saveGraph = new SaveChartFilePanel("Save Graph", "PNG [*.png]");
        saveGraph.addSaveGraphPressedListener(this);
        groupPanel.add(saveGraph);
        add(groupPanel, BorderLayout.NORTH);
        graphsPanel = new VerticalPanel();
        add(graphsPanel, BorderLayout.CENTER);
    }

    public String getStaticLabel() {
        return "JMX Memory Usage";
    }

    private JmxGraphPanel addGraph(String memType, String uri) {
        JmxGraphPanel graphPanel = new JmxGraphPanel();
        ChartWrapper graph = new ChartWrapper();
        graphsPanel.add(graphPanel);
        graphPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        graph.setTitle(memType + " usage for " + uri);
        graph.setXAxisTitle("elapsed time (seconds)");
        graph.setYAxisTitle("Kb");
        graph.setYAxisLabels(new String[] { memType, "Average" });
        Dimension size = new Dimension(800, 600);
        graph.setWidth(size.width);
        graph.setHeight(size.height);
        graphPanel.setGraph(graph);
        graphPanel.setMaximumSize(size);
        graphPanel.setPreferredSize(size);
        return graphPanel;
    }

    public void add(SampleResult res) {
        if (res instanceof JmxSampleResult) {
            log.warn("INVOKED: " + res);
            JmxSampleResult result = (JmxSampleResult) res;
            String uri = result.getJmxUri();
            JmxGraphPanel graphPanel = graphTable.get(uri);
            if (graphPanel == null) {
                graphPanel = addGraph(result.getMemType(), uri);
                graphTable.put(uri, graphPanel);
            }
            ChartWrapper graph = graphPanel.getGraph();
            graph.setXAxisScalingFactor(1000);
            graph.putRawData(result.getStartTime(), result.getValue());
            renderChart(uri, graphPanel);
        }
    }

    public String getLabelResource() {
        return "jmx.visualizer";
    }

    private synchronized void renderChart(String uri, JmxGraphPanel graphPanel) {
        graphPanel.invalidate();
        graphPanel.paintComponent();
        repaint();
    }

    public void saveGraphPressed(String filename) {
        Iterator<Entry<String, JmxGraphPanel>> it = graphTable.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            JmxGraphPanel v = it.next().getValue();
            String id = Integer.toString(i);
            if (graphTable.size() == 1) {
                id = "";
            }
            SaveGraphUtil.saveGraph(filename, id, v.getGraph().getChart());
            i++;
        }
    }

    public void clearData() {

    }

}
