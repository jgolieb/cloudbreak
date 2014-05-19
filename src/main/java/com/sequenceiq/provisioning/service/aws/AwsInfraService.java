package com.sequenceiq.provisioning.service.aws;

import com.sequenceiq.provisioning.controller.json.CloudInstanceResult;
import com.sequenceiq.provisioning.controller.json.InfraRequest;
import com.sequenceiq.provisioning.domain.CloudPlatform;
import com.sequenceiq.provisioning.domain.User;
import com.sequenceiq.provisioning.service.InfraService;

public class AwsInfraService implements InfraService {

    @Override
    public CloudInstanceResult createInfra(User user, InfraRequest infraRequest) {
        return new CloudInstanceResult("created");
    }

    @Override
    public CloudPlatform getCloudPlatform() {
        return CloudPlatform.AWS;
    }
}
