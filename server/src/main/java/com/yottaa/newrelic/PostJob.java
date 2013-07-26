package com.yottaa.newrelic;

import com.yottaa.api.YottaaHttpClientPublic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 */
public class PostJob {

    /**
     *
     */
    protected static Log logger = LogFactory.getLog(PostJob.class);

    /**
     *
     */
    public void postJobMethod() {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        logger.info("Posting Yottaa metrics to New Relic @ " + dateFormat.format(System.currentTimeMillis()));

        ResourceBundle bundle = ResourceBundle.getBundle("yottaa");
        YottaaHttpClientPublic yottaaHttpClientPublic = new YottaaHttpClientPublic(bundle.getString("yottaaAPIKey"));

        JSONObject lastSampleMetrics = yottaaHttpClientPublic.getLastSample();

        // Http Metrics
        JSONObject httpMetrics = (JSONObject) lastSampleMetrics.get("http_metrics");
        JSONObject httpMetricsFirstByte = (JSONObject) httpMetrics.get("first_byte");
        JSONObject httpMetricsWait = (JSONObject) httpMetrics.get("wait");
        JSONObject httpMetricsDNS = (JSONObject) httpMetrics.get("dns");
        JSONObject httpMetricsConnect = (JSONObject) httpMetrics.get("connect");

        // Issue Metrics
        JSONObject issueMetrics = (JSONObject) lastSampleMetrics.get("issue_metrics");

        //Webpage Metrics
        JSONObject webpageMetrics = (JSONObject) lastSampleMetrics.get("webpage_metrics");
        JSONObject webpageMetricsTimeToRender = (JSONObject) webpageMetrics.get("time_to_render");
        JSONObject webpageMetricsTimeToDisplay = (JSONObject) webpageMetrics.get("time_to_display");
        JSONObject webpageMetricsTimeToInteract = (JSONObject) webpageMetrics.get("time_to_interact");

        // Prepare JSON data that will be posted to New Relic
        JSONObject jsonData = new JSONObject();

        JSONObject agentData = new JSONObject();
        agentData.put("host", "apps.yottaa.com");
        agentData.put("pid", 0);
        agentData.put("version", "1.0.0");

        jsonData.put("agent", agentData);

        JSONArray components = new JSONArray();

        // Http Metrics
        JSONObject yottaaMetricsObj = new JSONObject();
        yottaaMetricsObj.put("guid", "com.yottaa.Yottaa");
        yottaaMetricsObj.put("duration", 60);
        yottaaMetricsObj.put("name", "Yottaa Metrics");

        JSONObject yottaaMetricsData = new JSONObject();
        yottaaMetricsData.put("Component/Http Metrics/Time To First Byte[sec]", Double.parseDouble(httpMetricsFirstByte.get("average").toString()));
        yottaaMetricsData.put("Component/Http Metrics/Waiting Time[sec]", Double.parseDouble(httpMetricsWait.get("average").toString()));
        yottaaMetricsData.put("Component/Http Metrics/DNS Time[sec]", Double.parseDouble(httpMetricsDNS.get("average").toString()));
        yottaaMetricsData.put("Component/Http Metrics/Connection Time[sec]", Double.parseDouble(httpMetricsConnect.get("average").toString()));

        yottaaMetricsData.put("Component/Issue Metrics/Critical Error Count[times]", Integer.parseInt(issueMetrics.get("critical_error_count").toString()));
        yottaaMetricsData.put("Component/Issue Metrics/Error Count[times]", Integer.parseInt(issueMetrics.get("error_count").toString()));
        yottaaMetricsData.put("Component/Issue Metrics/Info Count[times]", Integer.parseInt(issueMetrics.get("info_count").toString()));
        yottaaMetricsData.put("Component/Issue Metrics/Warning Count[times]", Integer.parseInt(issueMetrics.get("warning_count").toString()));

        yottaaMetricsData.put("Component/Webpage Metrics/Time To Render[sec]", Double.parseDouble(webpageMetricsTimeToRender.get("average").toString()));
        yottaaMetricsData.put("Component/Webpage Metrics/Time To Display[sec]", Double.parseDouble(webpageMetricsTimeToDisplay.get("average").toString()));
        yottaaMetricsData.put("Component/Webpage Metrics/Time To Interact[sec]", Double.parseDouble(webpageMetricsTimeToInteract.get("average").toString()));

        yottaaMetricsObj.put("metrics", yottaaMetricsData);

        components.add(yottaaMetricsObj);

        jsonData.put("components", components);

        logger.debug("Posted Yottaa Metrics :" + jsonData);

        this.newrelicPost(null, bundle.getString("newrelicLicenseKey"), jsonData);

    }

    /**
     * @param params
     * @param apiKey
     * @param postedJSON
     * @return
     */
    protected Object newrelicPost(JSONObject params, String apiKey, Object postedJSON) {
        Object responseObj = null;

        try {
            URIBuilder builder = new URIBuilder("https://platform-api.newrelic.com");
            builder.setPath("/platform/v1/metrics");

            if (params != null) {
                Iterator it = params.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    builder.setParameter(entry.getKey().toString(), entry.getValue().toString());
                }
            }

            URI uri = builder.build();

            HttpPost httpMethod = new HttpPost(uri);

            httpMethod.setHeader("X-License-Key", apiKey);
            httpMethod.removeHeaders("accept");
            httpMethod.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(postedJSON.toString(), HTTP.UTF_8);
            entity.setContentType("application/json");
            httpMethod.setEntity(entity);

            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpMethod);
            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                String responseStr = EntityUtils.toString(responseEntity);
                JSONParser parser = new JSONParser();
                responseObj = parser.parse(responseStr);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to post Yottaa metrics to New Relic",e);
        } catch (ClientProtocolException e) {
            logger.error("Failed to post Yottaa metrics to New Relic",e);
        } catch (IOException e) {
            logger.error("Failed to post Yottaa metrics to New Relic",e);
        } catch (URISyntaxException e) {
            logger.error("Failed to post Yottaa metrics to New Relic",e);
        } catch (ParseException e) {
            logger.error("Failed to post Yottaa metrics to New Relic",e);
        }

        return responseObj;
    }
}