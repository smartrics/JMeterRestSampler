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

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jCharts.Chart;
import org.jCharts.encoders.PNGEncoder;

public class SaveGraphUtil {

    private static Log log = LogFactory.getLog(SaveGraphUtil.class);

    public synchronized static void saveGraph(String fileName, String fileIdx, Chart chart) {
        String extension = ".png";
        FileOutputStream fileOutputStream = null;
        if (fileName != null && !("".equals(fileName.trim()))) {
            fileName = fileName.trim();
            if (fileName.toLowerCase().endsWith(extension)) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            try {

                fileOutputStream = new FileOutputStream(fileName + fileIdx + extension);
                PNGEncoder.encode(chart, fileOutputStream);
                fileOutputStream.flush();
            } catch (Throwable throwable) {
                log.warn("Unable to save graph in " + fileName, throwable);
                throw new IllegalArgumentException("Unable to save graph in " + fileName, throwable);
            } finally {
                if (fileOutputStream != null)
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        log.warn("Unable to close " + fileName, e);
                    }
            }
        }
    }

    public synchronized static void saveGraph(String fileName, Chart chart) {
        saveGraph(fileName, "", chart);
    }

}
