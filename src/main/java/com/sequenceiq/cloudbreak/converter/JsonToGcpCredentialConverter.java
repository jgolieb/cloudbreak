package com.sequenceiq.cloudbreak.converter;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.controller.json.CredentialJson;
import com.sequenceiq.cloudbreak.controller.validation.GccCredentialParam;
import com.sequenceiq.cloudbreak.domain.GccCredential;

@Component
public class JsonToGcpCredentialConverter extends AbstractConversionServiceAwareConverter<CredentialJson, GccCredential> {

    @Override
    public GccCredential convert(CredentialJson source) {
        GccCredential gccCredential = new GccCredential();
        gccCredential.setName(source.getName());
        gccCredential.setServiceAccountId(String.valueOf(source.getParameters().get(GccCredentialParam.SERVICE_ACCOUNT_ID.getName())));
        gccCredential.setServiceAccountPrivateKey(String.valueOf(source.getParameters().get(GccCredentialParam.SERVICE_ACCOUNT_PRIVATE_KEY.getName())));
        gccCredential.setProjectId(String.valueOf(source.getParameters().get(GccCredentialParam.PROJECTID.getName())));
        gccCredential.setDescription(source.getDescription());
        gccCredential.setPublicKey(source.getPublicKey());
        return gccCredential;
    }
}
