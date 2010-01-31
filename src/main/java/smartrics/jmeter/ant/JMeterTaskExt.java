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
import java.io.FilenameFilter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.soap.providers.com.Log;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class JMeterTaskExt extends JMeterTask {

    private String succecssThresholdPerc;

    public void setSuccecssThresholdPerc(String t) {
        this.succecssThresholdPerc = t;
    }

    public String getSuccecssThresholdPerc() {
        return this.succecssThresholdPerc;
    }

    private File chartsOutputDir;

    public File getChartsOutputDir() {
        return chartsOutputDir;
    }

    public void setChartsOutputDir(File chartsOutputDir) {
        this.chartsOutputDir = chartsOutputDir;
    }

    public int getSuccecssThresholdAsInt() {
        try {
            return Integer.parseInt(getSuccecssThresholdPerc());
        } catch (NumberFormatException e) {
            Log.msg(Log.WARNING, "Unable to parse succecssThreshold. Use default of 100%");
            return 100;
        }
    }

    public void execute() throws BuildException {
        try {
            super.execute();
            generateCharts();
        } catch (RuntimeException e) {
            throw new BuildException("Unexpected exception: " + e.getMessage());
        }
    }

    private void generateCharts() throws BuildException {
        if (chartsOutputDir != null) {
            AbstractList<File> resultFiles = getResultLogFiles();
            for (File result : resultFiles) {
                String name = result.getName();
                int pos = name.indexOf(".jtl");
                name = name.substring(0, pos);
                File jmxChartFile = new File(chartsOutputDir, name + "_jmxChart.png");
                File perfChartFile = new File(chartsOutputDir, name + "_perfChart.png");
                File fullResult = new File(getResultLogDir(), result.getName());
                ChartGenerator dataExtractor = new ChartGenerator(fullResult, jmxChartFile, perfChartFile);
                log("Generating charts with data extracted from " + fullResult.getAbsolutePath(), Project.MSG_VERBOSE);
                dataExtractor.generate();
                log("Charts generated in '" + jmxChartFile.getAbsolutePath() + "' and '" + perfChartFile.getAbsolutePath() + "'", Project.MSG_VERBOSE);
            }
        }
    }

    private ArrayList<File> getResultLogFiles() {
        ArrayList<File> resultLogFiles = new ArrayList<File>();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jtl");
            }
        };
        String[] files = getResultLogDir().list(filter);
        for (String s : files) {
            resultLogFiles.add(new File(s));
        }
        return resultLogFiles;
    }

    protected void checkForFailures() throws BuildException {
        JMeterUtils.setJMeterHome(getJmeterHome().getAbsolutePath());
        JMeterUtils.loadJMeterProperties(getJmeterProperties().getAbsolutePath());
        if (getFailureProperty() != null && getFailureProperty().trim().length() > 0) {
            AbstractList<File> resultFiles = getResultLogFiles();
            for (Iterator<File> i = resultFiles.iterator(); i.hasNext();) {
                File resultLogFile = new File(getResultLogDir(), ((File) i.next()).getName());
                log("Processing " + resultLogFile.getAbsolutePath());
                SuccessParser parser = new SuccessParser(resultLogFile);
                parser.handleResults();
                int totals = parser.getTotal();
                int successes = parser.getSuccesses();
                int failures = totals - successes;
                int succecssThresholdPerc = getSuccecssThresholdAsInt();
                double percSuccesses = 100 * successes / totals;
                log(String.format("Total samples: %s, successes: %s, failures: %s", totals, successes, failures), Project.MSG_VERBOSE);
                log(String.format("Success percent: $s, success threshold percent: %s", percSuccesses, succecssThresholdPerc));
                if (percSuccesses < succecssThresholdPerc) {
                    setFailure(getFailureProperty());
                }
            }
        }
    }

}
