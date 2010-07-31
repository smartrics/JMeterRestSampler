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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.jCharts.Chart;
import org.jCharts.axisChart.AxisChart;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelPosition;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelRenderer;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.DataAxisProperties;
import org.jCharts.properties.LabelAxisProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.properties.LineChartProperties;
import org.jCharts.properties.PropertyException;
import org.jCharts.types.ChartType;

/**
 * Wraps a jChart graph on a simpler interface. The graph is represented as an
 * area chart for the data in column 1 and a line chart for data in column 2.
 */
public class ChartWrapper {

    private static final long serialVersionUID = 8263438417891736098L;
    private Map<Long, Long> rawData = new TreeMap<Long, Long>();

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final static ChartType[] chartTypes = new ChartType[] { ChartType.AREA, ChartType.LINE, ChartType.BAR, ChartType.POINT };
    private final static Color[] colours = new Color[] { Color.ORANGE, Color.BLUE, Color.RED, Color.GREEN };
    private final static int poolsLength = chartTypes.length;

    protected String title, xAxisTitle, yAxisTitle;
    protected String[] yAxisLabels;
    protected int width, height;
    protected Chart chart;
    protected BufferedImage image;
    private int xAxisScalingFactor = 1;

    /**
     *
     */
    public ChartWrapper() {
        super();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void putRawData(Long key, Long val) {
        if (rawData == null)
            rawData = new HashMap<Long, Long>();
        rawData.put(key, val);
    }

    public void setXAxisScalingFactor(int xAsf) {
        this.xAxisScalingFactor = xAsf;
    }

    public Chart getChart() {
        return chart;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setXAxisTitle(String title) {
        this.xAxisTitle = title;
    }

    public void setYAxisTitle(String title) {
        this.yAxisTitle = title;
    }

     public void setYAxisLabels(String[] label) {
        this.yAxisLabels = label;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    public void drawData() {
        if (image == null) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D graphics = image.createGraphics();
        drawData(graphics);
    }

    public void drawData(Graphics g) {
        if (getWidth() == 0) {
            log.warn("Width is 0 - nothing to draw");
        }
        if (getHeight() == 0) {
            log.warn("Height is 0 - nothing to draw");
        }
        if (rawData == null) {
            log.warn("Nothing to draw");
            return;
        }
        if (title == null)
            title = "Graph";
        if (this.xAxisTitle == null)
            xAxisTitle = "X Axis Title";
        if (this.yAxisTitle == null)
            yAxisTitle = "Y Axis Title";
        render(g);
    }

    private void render(Graphics g) {
        try {
            String[] xAxisLabels = formattedKeys();
            DataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, title);
            LineChartProperties lineChartProperties = createLineChartProperties();
            double data[][] = createDatasetWithAverage();
            for (int i = 0; i < data.length; i++) {
                String[] legendLabelsData = { yAxisLabels[i] };
                Paint[] paints = new Paint[] { colours[i % poolsLength] };
                double[][] extracted = extract(i, data);
                AxisChartDataSet axisChartDataSet = new AxisChartDataSet(extracted, legendLabelsData, paints, chartTypes[i % poolsLength], lineChartProperties);
                dataSeries.addIAxisPlotDataSet(axisChartDataSet);
            }
            chart = createAxisChart(dataSeries);
            chart.setGraphics2D((Graphics2D) g);
            chart.render();

        } catch (ChartDataException e) {
            log.warn("Chart Data Exception", e);
        } catch (PropertyException e) {
            log.warn("Property Exception", e);
        }
    }

    private Chart createAxisChart(DataSeries dataSeries) {
        DataAxisProperties yaxis = new DataAxisProperties();
        yaxis.setShowGridLines(1);

        LabelAxisProperties xaxis = new LabelAxisProperties();
        xaxis.setShowGridLines(1);

        AxisProperties axisProperties = new AxisProperties(xaxis, yaxis);
        axisProperties.setXAxisLabelsAreVertical(true);

        LegendProperties legendProperties = new LegendProperties();
        ChartProperties chartProperties = new ChartProperties();
        return new AxisChart(dataSeries, chartProperties, axisProperties, legendProperties, width, height);
    }

    private LineChartProperties createLineChartProperties() {
        ValueLabelRenderer valueLabelRenderer = new ValueLabelRenderer(false, false, true, 0);
        valueLabelRenderer.setValueLabelPosition(ValueLabelPosition.AT_TOP);
        valueLabelRenderer.useVerticalLabels(true);

        Ellipse2D.Double dot = new Ellipse2D.Double(0.0D, 0.0D, 2D, 2D);
        Shape[] shapes = { dot };

        Stroke[] strokes = { LineChartProperties.DEFAULT_LINE_STROKE };

        LineChartProperties lineChartProperties = new LineChartProperties(strokes, shapes);
        lineChartProperties.addPostRenderEventListener(valueLabelRenderer);
        return lineChartProperties;
    }

    private double[][] extract(int index, double[][] _data) {
        double[][] d = new double[1][_data[0].length];
        for (int i = 0; i < _data[index].length; i++) {
            d[0][i] = _data[index][i];
        }
        return d;
    }

    protected String[] formattedKeys() {
        List<String> fk = new ArrayList<String>();
        Long k0 = -1L;
        for (Long k : rawData.keySet()) {
            if (k0 == -1)
                k0 = k;
            // transform to double to the first decimal digit.
            long etMs = k.longValue() - k0.longValue();
            long etSec = etMs / xAxisScalingFactor;
            String et = new Long(etSec).toString();
            fk.add(et);
        }
        return fk.toArray(new String[fk.size()]);
    }

    private double[][] createDatasetWithAverage() {
        List<Long> data = getDataValues();
        double[][] dataset = new double[2][data.size()];
        double avg = 0;
        for (int idx = 0; idx < data.size(); idx++) {
            dataset[0][idx] = data.get(idx);
            avg += dataset[0][idx];
            double val = avg / (idx + 1);
            dataset[1][idx] = val;
        }
        return dataset;
    }

    private List<Long> getDataValues() {
        List<Long> data = new ArrayList<Long>();
        for (Long l : rawData.keySet()) {
            data.add(rawData.get(l));
        }
        return data;
    }
}
