package com.sequenceiq.datalake.flow.create.event;

import com.sequenceiq.datalake.flow.SdxContext;
import com.sequenceiq.datalake.flow.SdxEvent;
import com.sequenceiq.redbeams.api.endpoint.v4.databaseserver.responses.DatabaseServerStatusV4Response;

public class EnvWaitRequest extends SdxEvent {

    private DatabaseServerStatusV4Response databaseServerResponse;

    public EnvWaitRequest(Long sdxId, String userId, DatabaseServerStatusV4Response databaseServerResponse) {
        super(sdxId, userId);
        this.databaseServerResponse = databaseServerResponse;
    }

    public static EnvWaitRequest from(SdxContext context, DatabaseServerStatusV4Response databaseServerResponse) {
        return new EnvWaitRequest(context.getSdxId(), context.getUserId(), databaseServerResponse);
    }

    @Override
    public String selector() {
        return "EnvWaitRequest";
    }

    public DatabaseServerStatusV4Response getDatabaseServerResponse() {
        return databaseServerResponse;
    }
}
