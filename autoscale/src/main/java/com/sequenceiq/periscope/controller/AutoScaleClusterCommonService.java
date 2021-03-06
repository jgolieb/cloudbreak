package com.sequenceiq.periscope.controller;


import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.periscope.api.model.AutoscaleClusterResponse;
import com.sequenceiq.periscope.api.model.AutoscaleClusterState;
import com.sequenceiq.periscope.api.model.ScalingStatus;
import com.sequenceiq.periscope.api.model.StateJson;
import com.sequenceiq.periscope.converter.ClusterConverter;
import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.domain.History;
import com.sequenceiq.periscope.notification.HttpNotificationSender;
import com.sequenceiq.periscope.service.AutoscaleRestRequestThreadLocalService;
import com.sequenceiq.periscope.service.ClusterService;
import com.sequenceiq.periscope.service.HistoryService;

@Component
public class AutoScaleClusterCommonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScaleClusterCommonService.class);

    @Inject
    private ClusterConverter clusterConverter;

    @Inject
    private ClusterService clusterService;

    @Inject
    private HistoryService historyService;

    @Inject
    private HttpNotificationSender notificationSender;

    @Inject
    private AutoscaleRestRequestThreadLocalService restRequestThreadLocalService;

    public List<AutoscaleClusterResponse> getClusters() {
        List<Cluster> clusters = clusterService.findAllByUser(restRequestThreadLocalService.getCloudbreakUser());
        return clusterConverter.convertAllToJson(clusters);
    }

    public AutoscaleClusterResponse getCluster(Long clusterId) {
        return createClusterJsonResponse(clusterService.findById(clusterId));
    }

    public void deleteCluster(Long clusterId) {
        clusterService.removeById(clusterId);
    }

    public AutoscaleClusterResponse setState(Long clusterId, StateJson stateJson) {
        Cluster cluster = clusterService.setState(clusterId, stateJson.getState());
        createHistoryAndNotification(cluster);
        return createClusterJsonResponse(cluster);
    }

    public AutoscaleClusterResponse setAutoscaleState(Long clusterId, AutoscaleClusterState autoscaleState) {
        Cluster cluster = clusterService.setAutoscaleState(clusterId, autoscaleState.isEnableAutoscaling());
        createHistoryAndNotification(cluster);
        return createClusterJsonResponse(cluster);
    }

    private AutoscaleClusterResponse createClusterJsonResponse(Cluster cluster) {
        return clusterConverter.convert(cluster);
    }

    private void createHistoryAndNotification(Cluster cluster) {
        History history;
        history = cluster.isAutoscalingEnabled()
                ? historyService.createEntry(ScalingStatus.ENABLED, "Autoscaling has been enabled for the cluster.", 0, cluster)
                : historyService.createEntry(ScalingStatus.DISABLED, "Autoscaling has been disabled for the cluster.", 0, cluster);
        notificationSender.send(cluster, history);
    }
}
