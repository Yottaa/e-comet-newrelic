package com.yottaa.plugins.newrelic;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;

import java.util.Map;

public class YottaaFactory extends AgentFactory {

    public YottaaFactory() {
        super("com.yottaa.plugins.newrelic.yottaa.json");
    }

    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) {
        String name = (String) properties.get("name");

        /*
          int sawtoothMax = ((Long) properties.get("sawtoothMax")).intValue();
          int squarewaveMax = ((Long) properties.get("squarewaveMax")).intValue();
          */
        return new Yottaa(name);
    }
}
