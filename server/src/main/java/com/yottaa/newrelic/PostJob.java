package com.yottaa.newrelic;

import com.yottaa.api.YottaaHttpClientPublic;
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

    public void postJobMethod() {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        System.out.println("Invoked on " + dateFormat.format(System.currentTimeMillis()));

        ResourceBundle bundle = ResourceBundle.getBundle("yottaa");
        YottaaHttpClientPublic yottaaHttpClientPublic = new YottaaHttpClientPublic(bundle.getString("yottaaAPIKey"));

        JSONObject lastSampleMetrics = yottaaHttpClientPublic.getLastSample();

        // Http Metrics
        JSONObject httpMetrics = (JSONObject) lastSampleMetrics.get("http_metrics");
        JSONObject httpMetricsConnect = (JSONObject) httpMetrics.get("connect");

        // Issue Metrics
        JSONObject issueMetrics = (JSONObject) lastSampleMetrics.get("issue_metrics");

        //Webpage Metrics
        JSONObject webpageMetrics = (JSONObject) lastSampleMetrics.get("webpage_metrics");
        JSONObject webpageMetricsAssetCount = (JSONObject) webpageMetrics.get("asset_count");

        // Prepare JSON data that will be posted to New Relic
        JSONObject jsonData = new JSONObject();

        JSONObject agentData = new JSONObject();
        agentData.put("host", "host");
        agentData.put("pid", 0);
        agentData.put("version", "1.0.0");

        jsonData.put("agent", agentData);

        JSONArray components = new JSONArray();

        // Http Metrics
        JSONObject httpMetricsObj = new JSONObject();
        httpMetricsObj.put("guid", "com.yottaa.plugins.newrelic.yottaa");
        httpMetricsObj.put("duration", 60);
        httpMetricsObj.put("name", "Http Metrics");

        JSONObject httpMetricsData = new JSONObject();
        httpMetricsData.put("Component/Http Metrics/Connect[times]", Integer.parseInt(httpMetricsConnect.get("sum").toString()));
        httpMetricsData.put("Component/Http Metrics/Connect Average[times/sec]", Double.parseDouble(httpMetricsConnect.get("average").toString()));

        httpMetricsObj.put("metrics", httpMetricsData);

        components.add(httpMetricsObj);

        // Http Metrics
        JSONObject issueMetricsObj = new JSONObject();
        issueMetricsObj.put("guid", "com.yottaa.plugins.newrelic.yottaa");
        issueMetricsObj.put("duration", 60);
        issueMetricsObj.put("name", "Issue Metrics");

        JSONObject issueMetricsData = new JSONObject();
        issueMetricsData.put("Component/Issue Metrics/Critical Error Count[times]", Integer.parseInt(issueMetrics.get("critical_error_count").toString()));
        issueMetricsData.put("Component/Issue Metrics/Error Count[times]", Integer.parseInt(issueMetrics.get("error_count").toString()));
        issueMetricsData.put("Component/Issue Metrics/Info Count[times]", Integer.parseInt(issueMetrics.get("info_count").toString()));
        issueMetricsData.put("Component/Issue Metrics/Warning Count[times]", Integer.parseInt(issueMetrics.get("warning_count").toString()));

        issueMetricsObj.put("metrics", issueMetricsData);

        components.add(issueMetricsObj);

        // Webpage Metrics
        JSONObject webpageMetricsObj = new JSONObject();
        webpageMetricsObj.put("guid", "com.yottaa.plugins.newrelic.yottaa");
        webpageMetricsObj.put("duration", 60);
        webpageMetricsObj.put("name", "Webpage Metrics");

        JSONObject webpageMetricsData = new JSONObject();
        webpageMetricsData.put("Component/Webpage Metrics/Asset Count[items]", Integer.parseInt(webpageMetricsAssetCount.get("sum").toString()));
        webpageMetricsData.put("Component/Webpage Metrics/Asset Count Average[items/sec]", Double.parseDouble(webpageMetricsAssetCount.get("average").toString()));

        webpageMetricsObj.put("metrics", webpageMetricsData);

        components.add(webpageMetricsObj);

        jsonData.put("components", components);

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
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        } catch (URISyntaxException e) {
        } catch (ParseException e) {
        }

        return responseObj;
    }
}