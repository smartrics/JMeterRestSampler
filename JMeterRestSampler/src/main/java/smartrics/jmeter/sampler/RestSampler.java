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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler2;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Models a REST request for sampling purposes.
 */
public class RestSampler extends HTTPSampler2 {
    private static final long serialVersionUID = -5877623539165274730L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String REQUEST_BODY = "RestSampler.request_body";

    public static final String QUERY_STRING = "RestSampler.query_string";

    public static final String RESOURCE = "RestSampler.resource";

    public static final String BASE_HOST = "RestSampler.base_host";

    public static final String REQUEST_HEADERS = "RestSampler.request_headers";

    public RestSampler() {
    }

    public void setRequestBody(String data) {
        setProperty(REQUEST_BODY, data);
    }

    public void setRequestHeaders(String headers) {
        setProperty(REQUEST_HEADERS, headers);
    }

    public String getRequestBody() {
        return getPropertyAsString(REQUEST_BODY);
    }

    public String getRequestHeaders() {
        return getPropertyAsString(REQUEST_HEADERS);
    }

    public void setResource(String data) {
        setProperty(RESOURCE, data);
    }

    public String getResource() {
        return getPropertyAsString(RESOURCE);
    }

    public void setQueryString(String data) {
        setProperty(QUERY_STRING, data);
        getArguments().clear();
        parseArguments(data, EncoderCache.URL_ARGUMENT_ENCODING);
    }

    public String getQueryString() {
        return getPropertyAsString(QUERY_STRING);
    }

    public void setHostBaseUrl(final String data) {
        setProperty(BASE_HOST, data);
    }

    public String getHostBaseUrl() {
        return getPropertyAsString(BASE_HOST);
    }

    /**
     * Returns the full resource URI concatenating the base url and the resource
     * id. If either components are missing or invalid, it returns a canned
     * value of <code>"http://undefined.com"</code>
     */
    public URL getUrl() {
        String validHost = toValidUrl(getHostBaseUrl());
        URL u = toURL("http://undefined.com");
        if (validHost != null && getResource() != null) {
            String fullUrl = validHost + getResource();
            u = toURL(fullUrl);
        }
        return u;
    }

    public String toString() {
        return "Base host url: " + getHostBaseUrl() + ", resource: " + getResource() + ", Method: " + getMethod();
    }

    private String toValidUrl(String u) {
        try {
            URL url = new URL(u);
            String urlStr = url.toString();
            if (urlStr.endsWith("/")) {
                url = toURL(urlStr.substring(0, urlStr.length() - 1));
                urlStr = url.toString();
            }
            return urlStr;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private URL toURL(String u) {
        try {
            return new URL(u);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private void overrideHeaders(HttpMethodBase httpMethod) {
        String headers = getRequestHeaders();
        String[] header = headers.split(System.getProperty("line.separator"));
        for (String kvp : header) {
            int pos = kvp.indexOf(':');
            if (pos < 0)
                pos = kvp.indexOf('=');
            if (pos > 0) {
                String k = kvp.substring(0, pos).trim();
                String v = "";
                if (kvp.length() > pos + 1)
                    v = kvp.substring(pos + 1).trim();
                httpMethod.addRequestHeader(k, v);
            }
        }
    }

    /**
     * Method invoked by JMeter when a sample needs to happen. It's actually an
     * indirect call from the main sampler interface. it's resolved in the base
     * class.
     * 
     * This is a copy and paste from the HTTPSampler2 - quick and dirty hack as
     * that class is not very extensible. The reason to extend and slightly
     * modify is that I needed to get the body content from a text field in the
     * GUI rather than a file.
     */
    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {

        String urlStr = url.toString();

        log.debug("Start : sample " + urlStr);
        log.debug("method " + method);

        HttpMethodBase httpMethod = null;

        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());

        res.setSampleLabel(urlStr); // May be replaced later
        res.setHTTPMethod(method);
        res.setURL(url);
        res.sampleStart(); // Count the retries as well in the time
        HttpClient client = null;
        InputStream instream = null;
        try {
            httpMethod = createHttpMethod(method, urlStr);
            // Set any default request headers
            setDefaultRequestHeaders(httpMethod);
            // Setup connection
            client = setupConnection(url, httpMethod, res);
            // Handle the various methods
            if (httpMethod instanceof EntityEnclosingMethod) {
                String postBody = sendData((EntityEnclosingMethod) httpMethod);
                res.setResponseData(postBody.getBytes());
            }
            overrideHeaders(httpMethod);
            res.setRequestHeaders(getConnectionHeaders(httpMethod));

            int statusCode = -1;
            try {
                statusCode = client.executeMethod(httpMethod);
            } catch (RuntimeException e) {
                log.error("Exception when executing '" + httpMethod + "'", e);
                throw e;
            }

            // Request sent. Now get the response:
            instream = httpMethod.getResponseBodyAsStream();

            if (instream != null) {// will be null for HEAD

                Header responseHeader = httpMethod.getResponseHeader(HEADER_CONTENT_ENCODING);
                if (responseHeader != null && ENCODING_GZIP.equals(responseHeader.getValue())) {
                    instream = new GZIPInputStream(instream);
                }
                res.setResponseData(readResponse(res, instream, (int) httpMethod.getResponseContentLength()));
            }

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setSampleLabel(httpMethod.getURI().toString());
            // Pick up Actual path (after redirects)

            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(isSuccessCode(statusCode));

            res.setResponseMessage(httpMethod.getStatusText());

            String ct = null;
            org.apache.commons.httpclient.Header h = httpMethod.getResponseHeader(HEADER_CONTENT_TYPE);
            if (h != null)// Can be missing, e.g. on redirect
            {
                ct = h.getValue();
                res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
                res.setEncodingAndType(ct);
            }

            String responseHeaders = getResponseHeaders(httpMethod);
            res.setResponseHeaders(responseHeaders);
            if (res.isRedirect()) {
                final Header headerLocation = httpMethod.getResponseHeader(HEADER_LOCATION);
                if (headerLocation == null) { // HTTP protocol violation, but
                    // avoids NPE
                    throw new IllegalArgumentException("Missing location header");
                }
                res.setRedirectLocation(headerLocation.getValue());
            }

            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()) {
                res.setURL(new URL(httpMethod.getURI().toString()));
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpMethod, res.getURL(), getCookieManager());

            // Save cache information
            final CacheManager cacheManager = getCacheManager();
            if (cacheManager != null) {
                cacheManager.saveDetails(httpMethod, res);
            }

            // Follow redirects and download page resources if appropriate:
            res = resultProcessing(areFollowingRedirect, frameDepth, res);

            log.debug("End : sample");
            httpMethod.releaseConnection();
            return res;
        } catch (IllegalArgumentException e)// e.g. some kinds of invalid URL
        {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } catch (IOException e) {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } finally {
            JOrphanUtils.closeQuietly(instream);
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
    }

    private HttpMethodBase createHttpMethod(String method, String urlStr) {
        HttpMethodBase httpMethod;
        // May generate IllegalArgumentException
        if (method.equals(POST)) {
            httpMethod = new PostMethod(urlStr);
        } else if (method.equals(PUT)) {
            httpMethod = new PutMethod(urlStr);
        } else if (method.equals(HEAD)) {
            httpMethod = new HeadMethod(urlStr);
        } else if (method.equals(TRACE)) {
            httpMethod = new TraceMethod(urlStr);
        } else if (method.equals(OPTIONS)) {
            httpMethod = new OptionsMethod(urlStr);
        } else if (method.equals(DELETE)) {
            httpMethod = new DeleteMethod(urlStr);
        } else if (method.equals(GET)) {
            httpMethod = new GetMethod(urlStr);
        } else {
            log.error("Unexpected method (converted to GET): " + method);
            httpMethod = new GetMethod(urlStr);
        }
        return httpMethod;
    }

    /**
     * Set up the PUT/POST data.
     * 
     * <b>TODO</b>: should parse request headers and pass the Content-Type. For
     * now it will always assume text/xml
     */
    private String sendData(EntityEnclosingMethod method) throws IOException {
        method.setRequestEntity(new MyRequestEntity(getRequestBody()));
        return getRequestBody();
    }

}
