package com.sequenceiq.environment.environment.dto.telemetry;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.common.api.type.FeatureSetting;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvironmentFeatures implements Serializable {

    private FeatureSetting workloadAnalytics;

    private FeatureSetting clusterLogsCollection;

    private FeatureSetting useSharedAltusCredential;

    public FeatureSetting getClusterLogsCollection() {
        return clusterLogsCollection;
    }

    public void setClusterLogsCollection(FeatureSetting clusterLogsCollection) {
        this.clusterLogsCollection = clusterLogsCollection;
    }

    public FeatureSetting getWorkloadAnalytics() {
        return workloadAnalytics;
    }

    public void setWorkloadAnalytics(FeatureSetting workloadAnalytics) {
        this.workloadAnalytics = workloadAnalytics;
    }

    public FeatureSetting getUseSharedAltusCredential() {
        return useSharedAltusCredential;
    }

    public void setUseSharedAltusCredential(FeatureSetting useSharedAltusCredential) {
        this.useSharedAltusCredential = useSharedAltusCredential;
    }

    @JsonIgnore
    public void addWorkloadAnalytics(boolean enabled) {
        workloadAnalytics = new FeatureSetting();
        workloadAnalytics.setEnabled(enabled);
    }

    @JsonIgnore
    public void addClusterLogsCollection(boolean enabled) {
        clusterLogsCollection = new FeatureSetting();
        clusterLogsCollection.setEnabled(enabled);
    }

    @JsonIgnore
    public void addUseSharedAltusredential(boolean enabled) {
        useSharedAltusCredential = new FeatureSetting();
        useSharedAltusCredential.setEnabled(enabled);
    }
}
