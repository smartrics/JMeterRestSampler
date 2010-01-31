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

package smartrics.jmeter.sampler;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import smartrics.jmeter.sampler.jmx.MemUsageJmxClient;
import smartrics.jmeter.sampler.jmx.MemUsageJmxClient.MemoryData;

/**
 * This samples a JMX server for memory usage. At the moment only for Heap
 * memory of the JVM to which it points to. This sampler captures the URI for
 * the server, whether and where to save a graph file of the results and the
 * sample frequency.
 * 
 * It's a bit odd as a sampler because it should not be used to gather
 * performance measures on a JMX server.
 * 
 * It's main usage, in fact, is for reporting on memory usage, functional to
 * robustness builds.
 */
public class JmxSampler extends AbstractSampler {
    public static String HEAP_MEM = "heap";
    public static String NON_HEAP_MEM = "non heap";
    private static final long serialVersionUID = -5877623539165274730L;
    private static Map<String, Long> lastSampleTs = new HashMap<String, Long>();

    public static final String JMX_URI = "JmxSampler.jmx_uri";

    public static final String JMX_MEM_TYPE = "JmxSampler.jmx_mem_type";

    public static final String JMX_SAMPLE_FREQUENCY = "JmxSampler.jmx_sample_frequency";

    public static final String JMX_LAST_SAMPLE_TS = "JmxSampler.jmx_last_sample_ts";

    public JmxSampler() {
    }

    public int getSampleFrequency() {
        return getPropertyAsInt(JMX_SAMPLE_FREQUENCY);
    }

    public void setSampleFrequency(int data) {
        setProperty(JMX_SAMPLE_FREQUENCY, Integer.toString(data));
    }

    public void setLastSampleTs(String uri, long data) {
        lastSampleTs.put(uri, data);
    }

    public long getLastSampleTs(String uri) {
        Long ts = lastSampleTs.get(uri);
        if (ts == null)
            return 0;
        return ts.longValue();
    }

    public void setJmxUri(String data) {
        setProperty(JMX_URI, data);
    }

    public String getJmxUri() {
        return getPropertyAsString(JMX_URI);
    }

    public void setJmxMemType(String data) {
        setProperty(JMX_MEM_TYPE, data);
    }

    public String getJmxMemType() {
        return getPropertyAsString(JMX_MEM_TYPE);
    }

    public String toString() {
        return "Jmx uri: " + getJmxUri() + ", mem: " + getJmxMemType();
    }

    public SampleResult sample(Entry e) {
        return sample();
    }

    /**
     * 
     * @return a standard sample result of type JmxSampleResult.
     */
    public SampleResult sample() {
        long startdate = System.currentTimeMillis();
        long lastSampleTs = getLastSampleTs(getJmxUri());
        if ((startdate - lastSampleTs) < getSampleFrequency() * 1000) {
            // sampling more frequently than the set frequency
            return null;
        }
        setLastSampleTs(getJmxUri(), startdate);
        JmxSampleResult newRes = new JmxSampleResult();
        MemUsageJmxClient c = new MemUsageJmxClient();
        c.setUrl(getJmxUri());
        MemoryData d = c.getData();
        newRes.setSampleLabel("jmx");
        long mem = byte2Kbyte(d.getUsedNonHeap());
        if (HEAP_MEM.equals(getJmxMemType())) {
            // transform in Kb by default (mem is returned in bytes)
            mem = byte2Kbyte(d.getUsedHeap());
        }
        newRes.setMemType(getJmxMemType());
        newRes.setValue(mem);
        newRes.setJmxUri(getJmxUri());
        newRes.setSamplerData(Long.toString(mem));
        long enddate = System.currentTimeMillis();
        newRes.setStampAndTime(enddate, enddate - startdate);
        // obviously it's always successful as we're capturing data for report
        // purposes
        newRes.setSuccessful(true);
        return newRes;
    }

    private long byte2Kbyte(long n) {
        return n / 1024;
    }

}
