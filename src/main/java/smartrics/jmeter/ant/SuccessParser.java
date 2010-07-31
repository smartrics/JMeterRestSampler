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

import org.apache.jmeter.samplers.SampleResult;

import smartrics.jmeter.sampler.JmxSampleResult;

/**
 * Parses the jtl file and accumulates successes and total sample results passed
 * (JmxSampleResults are skipped as they are not part of the actual test)
 */
public class SuccessParser extends DataExtractor {

    private int successes = 0;

    private int total = 0;

    public SuccessParser(File jtlFile) {
        super(jtlFile);
    }

    public void handle(SampleResult res) {
        if (!(res instanceof JmxSampleResult)) {
            total++;
            if (res.isSuccessful()) {
                successes++;
            }
        }
    }
    public int getSuccesses() {
        return successes;
    }

    public int getTotal() {
        return total;
    }
}
