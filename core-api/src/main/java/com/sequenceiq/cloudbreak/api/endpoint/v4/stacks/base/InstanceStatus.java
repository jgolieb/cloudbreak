package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base;

public enum InstanceStatus {
    REQUESTED,
    FAILED,
    CREATED,
    ORCHESTRATION_FAILED,
    SERVICES_RUNNING,
    SERVICES_HEALTHY,
    SERVICES_UNHEALTHY,
    WAITING_FOR_REPAIR,
    STOPPED,
    DELETED_ON_PROVIDER_SIDE,
    DELETE_REQUESTED,
    DECOMMISSIONED,
    DECOMMISSION_FAILED,
    TERMINATED;

    public String getAsHostState() {
        switch (this) {
            case SERVICES_HEALTHY:
                return "HEALTHY";
            case DECOMMISSION_FAILED:
            case SERVICES_UNHEALTHY:
                return "UNHEALTHY";
            case WAITING_FOR_REPAIR:
                return "WAITING_FOR_REPAIR";
            case SERVICES_RUNNING:
                return "RUNNING";
            default:
                return "UNKNOWN";
        }
    }
}
