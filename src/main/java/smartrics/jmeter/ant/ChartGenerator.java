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
package smartrics.jmeter.ant;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import smartrics.jmeter.sampler.JmxSampleResult;
import smartrics.jmeter.sampler.SaveGraphUtil;
import smartrics.jmeter.sampler.gui.ChartWrapper;

public class ChartGenerator extends DataExtractor {
    private Map<String, ChartWrapper> jmxCharts = new HashMap<String, ChartWrapper>();
    private Map<String, ChartWrapper> perfCharts = new HashMap<String, ChartWrapper>();
    private File jmxGraphFile;
    private File perfGraphFile;

    public ChartGenerator(File jtlFile, File jmxGraphFile, File perfGraphFile) {
        super(jtlFile);
        this.jmxGraphFile = jmxGraphFile;
        this.perfGraphFile = perfGraphFile;
    }

    public void handle(SampleResult res) {
        if (res instanceof JmxSampleResult)
            handle((JmxSampleResult) res);
        else if (res instanceof HTTPSampleResult)
            handle((HTTPSampleResult) res);
        else {
            throw new IllegalArgumentException("Unable to handle SampleResult of type " + res.getClass().getName());
        }
    }

    public void generate() {
        if (jmxGraphFile == null && perfGraphFile == null) {
            return;
        }
        handleResults();
        produceGraphFiles(jmxCharts, jmxGraphFile);
        produceGraphFiles(perfCharts, perfGraphFile);
    }

    private void produceGraphFiles(Map<String, ChartWrapper> charts, File graphFile) {
        if (graphFile == null) {
            return;
        }
        Set<String> keys = charts.keySet();
        int cnt = 0;
        for (String key : keys) {
            ChartWrapper chart = charts.get(key);
            chart.drawData();
            String gfname = graphFile.getAbsolutePath();
            if (keys.size() > 1) {
                gfname = addCounterToFilename(cnt, gfname);
                cnt++;
            }
            SaveGraphUtil.saveGraph(gfname, chart.getChart());
        }
    }

    private String addCounterToFilename(int cnt, String fname) {
        int extPos = fname.lastIndexOf('.');
        String pre = fname.substring(0, extPos);
        String post = fname.substring(extPos);
        return pre + "_" + cnt + post;
    }

    public void handle(JmxSampleResult res) {
        ChartWrapper jmxChart = jmxCharts.get(res.getJmxUri());
        if (jmxChart == null) {
            String[] label = new String[] { res.getMemType(), "Average" };
            String yaTitle = "Kb";
            String xaTitle = "time";
            jmxChart = createChartWrapper(res.getJmxUri(), label, yaTitle, xaTitle);
            jmxCharts.put(res.getJmxUri(), jmxChart);
        }
        jmxChart.putRawData(res.getTimeStamp(), Long.parseLong(res.getSamplerData()));
    }

    private ChartWrapper createChartWrapper(String title, String[] label, String yaTitle, String xaTitle) {
        ChartWrapper chart = new ChartWrapper();
        chart.setHeight(600);
        chart.setWidth(800);
        chart.setTitle(title);
        chart.setYAxisLabels(label);
        chart.setYAxisTitle(yaTitle);
        chart.setXAxisTitle(xaTitle);
        return chart;
    }

    public void handle(HTTPSampleResult res) {
        URL url = res.getURL();
        String uri = "host";
        // TODO: find out why the URL is not saved in the HTTPSampleResult
        if (url != null)
            uri = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        ChartWrapper timeChart = perfCharts.get(uri);
        if (timeChart == null) {
            String[] label = new String[] { "Time", "Average" };
            String yaTitle = "ms";
            String xaTitle = "time";
            timeChart = createChartWrapper(uri, label, yaTitle, xaTitle);
            perfCharts.put(uri, timeChart);
        }
        timeChart.putRawData(res.getTimeStamp(), res.getEndTime() - res.getStartTime());
    }

    public static void main(String[] args) {
        JMeterUtils.setJMeterHome("/opt/java/jakarta-jmeter");
        JMeterUtils.loadJMeterProperties("/opt/java/jakarta-jmeter/bin/jmeter.properties");
        File f = new File("/home/fabrizio/Desktop/twitter.jtl");
        File jmxFile = new File("/home/fabrizio/Desktop/jmxGraph.png");
        File perfFile = new File("/home/fabrizio/Desktop/timeGraph.png");
        new ChartGenerator(f, jmxFile, perfFile).generate();
    }

}
