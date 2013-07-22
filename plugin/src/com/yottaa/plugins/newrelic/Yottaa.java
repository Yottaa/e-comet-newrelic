package com.yottaa.plugins.newrelic;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.processors.EpochCounter;
import com.newrelic.metrics.publish.processors.Processor;
import com.yottaa.api.YottaaHttpClientPublic;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

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

        if (this.name.equals("Http Metrics")) {
            // Http Metrics
            JSONObject httpMetrics = (JSONObject) lastSampleMetrics.get("http_metrics");
            JSONObject httpMetricsConnect = (JSONObject) httpMetrics.get("connect");
            reportMetric("Http Metrics/Connect Average", "times/sec", Double.parseDouble(httpMetricsConnect.get("average").toString()));
            reportMetric("Http Metrics/Connect", "times", Integer.parseInt(httpMetricsConnect.get("sum").toString()));
        }

        if (this.name.equals("Issue Metrics")) {
            // Issue Metrics
            JSONObject issueMetrics = (JSONObject) lastSampleMetrics.get("issue_metrics");
            reportMetric("Issue Metrics/Critical Error Count", "times", Integer.parseInt(issueMetrics.get("critical_error_count").toString()));
            reportMetric("Issue Metrics/Error Count", "times", Integer.parseInt(issueMetrics.get("error_count").toString()));
            reportMetric("Issue Metrics/Info Count", "times", Integer.parseInt(issueMetrics.get("info_count").toString()));
            reportMetric("Issue Metrics/Warning Count", "times", Integer.parseInt(issueMetrics.get("warning_count").toString()));
        }

        if (this.name.equals("Webpage Metrics")) {
            //Webpage Metrics
            JSONObject webpageMetrics = (JSONObject) lastSampleMetrics.get("webpage_metrics");
            JSONObject webpageMetricsAssetCount = (JSONObject) webpageMetrics.get("asset_count");
            reportMetric("Webpage Metrics/Asset Count Average", "items/sec", Double.parseDouble(webpageMetricsAssetCount.get("average").toString()));
            reportMetric("Webpage Metrics/Asset Count", "items", Integer.parseInt(webpageMetricsAssetCount.get("sum").toString()));
        }
    }


}

