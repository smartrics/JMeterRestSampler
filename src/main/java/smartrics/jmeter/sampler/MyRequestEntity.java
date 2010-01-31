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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.httpclient.methods.RequestEntity;

/**
 * Holds the body content of a PUT/POST as requested by the apache HttpClient.
 */
public class MyRequestEntity implements RequestEntity {

    private String data;
    private String contentType;

    public MyRequestEntity(String data) {
        this("text/xml", data);
    }

    public MyRequestEntity(String type, String data) {
        this.data = data;
        this.contentType = type;
    }

    public boolean isRepeatable() {
        return true;
    }

    public void writeRequest(OutputStream out) throws IOException {
        PrintWriter printer = new PrintWriter(out);
        printer.print(data);
        printer.flush();
    }

    public long getContentLength() {
        // so we don't generate chunked encoding
        return data.getBytes().length;
    }

    public String getContentType() {
        return contentType;
    }
}
