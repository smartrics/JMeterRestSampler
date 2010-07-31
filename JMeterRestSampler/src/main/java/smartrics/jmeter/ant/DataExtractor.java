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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.ResultCollectorHelper;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.visualizers.GraphVisualizer;
import org.apache.jmeter.visualizers.Visualizer;

public abstract class DataExtractor {

    private File jtlFile;

    public DataExtractor(File jtlFile) {
        super();
        if (jtlFile == null) {
            throw new IllegalArgumentException("jtl file is null");
        }
        this.jtlFile = jtlFile;
    }

    public File getJtlFile() {
        return jtlFile;
    }

    @SuppressWarnings({"serial" })
    public void handleResults() {
        String jtlFilePath = jtlFile.getAbsolutePath();
        try {
//            FileInputStream fis = new FileInputStream(getJtlFile());
			final Collection<SampleResult> results = new ArrayList<SampleResult>();
//			ResultCollector resultCollector = new ResultCollector(){
//			    public void sampleOccurred(SampleEvent event) {
//			        SampleResult result = event.getResult();
//			        results.add(result);
//			    }
//			};			
//            Visualizer visualizer = new GraphVisualizer();
//			ResultCollectorHelper resultCollectorHelper = new ResultCollectorHelper(resultCollector, visualizer);
//			SaveService.loadTestResults(fis, resultCollectorHelper);
//			resultCollector.loadExistingFile();

            ResultCollector rc = new ResultCollector();
            rc.setFilename(jtlFilePath);
            rc.setListener(new Visualizer() {
            	
 				public void add(SampleResult sample) {
					results.add(sample);
				}

				public boolean isStats() {
					return false;
				}
            	
            });
            rc.loadExistingFile();
			
            for (SampleResult r : results) {
                try {
                    handle(r);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("jtl file '" + jtlFilePath + "' contains data not processable", e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to load test results from " + jtlFilePath, e);
        }
    }

    public abstract void handle(SampleResult res);

}