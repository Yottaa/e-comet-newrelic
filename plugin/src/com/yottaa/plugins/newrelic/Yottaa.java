package com.yottaa.plugins.newrelic;

import com.newrelic.metrics.publish.Agent;
import com.yottaa.api.YottaaHttpClientPublic;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 */
public class Yottaa extends Agent {

    private YottaaHttpClientPublic yottaaHttpClientPublic;

    private String name = "Default";

    public Yottaa(String name) {
        super(YottaaConstants.YOTTAA_PLUGIN_ID, YottaaConstants.YOTTAA_PLUGIN_VERSION);

        try {
            ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream("config/newrelic.properties"));
            this.yottaaHttpClientPublic = new YottaaHttpClientPublic(bundle.getString("yottaaAPIKey"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.name = name;
    }

    @Override
    public String getComponentHumanLabel() {
        return name;
    }

    @Override
    public void pollCycle() {
        JSONObject lastSampleMetrics = this.yottaaHttpClientPublic.getLastSample();

        if (this.name.equals("Yottaa Metrics")) {

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


            reportMetric("Http Metrics/Time To First Byte", "sec", Double.parseDouble(httpMetricsFirstByte.get("average").toString()));
            reportMetric("Http Metrics/Waiting Time", "sec", Double.parseDouble(httpMetricsWait.get("average").toString()));
            reportMetric("Http Metrics/DNS Time", "sec", Double.parseDouble(httpMetricsDNS.get("average").toString()));
            reportMetric("Http Metrics/Connection Time", "sec", Double.parseDouble(httpMetricsConnect.get("average").toString()));

            reportMetric("Issue Metrics/Critical Error Count", "times", Integer.parseInt(issueMetrics.get("critical_error_count").toString()));
            reportMetric("Issue Metrics/Error Count", "times", Integer.parseInt(issueMetrics.get("error_count").toString()));
            reportMetric("Issue Metrics/Info Count", "times", Integer.parseInt(issueMetrics.get("info_count").toString()));
            reportMetric("Issue Metrics/Warning Count", "times", Integer.parseInt(issueMetrics.get("warning_count").toString()));

            reportMetric("Webpage Metrics/Time To Render", "sec", Double.parseDouble(webpageMetricsTimeToRender.get("average").toString()));
            reportMetric("Webpage Metrics/Time To Display", "sec", Double.parseDouble(webpageMetricsTimeToDisplay.get("average").toString()));
            reportMetric("Webpage Metrics/Time To Interact", "sec", Double.parseDouble(webpageMetricsTimeToInteract.get("average").toString()));

        }
    }


}

