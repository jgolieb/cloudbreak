package com.sequenceiq.cloudbreak.cloud.handler.testcontext;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sequenceiq.cloudbreak.cloud.Authenticator;
import com.sequenceiq.cloudbreak.cloud.CloudConnector;
import com.sequenceiq.cloudbreak.cloud.CloudPlatformVariant;
import com.sequenceiq.cloudbreak.cloud.InstanceConnector;
import com.sequenceiq.cloudbreak.cloud.MetadataCollector;
import com.sequenceiq.cloudbreak.cloud.ResourceConnector;
import com.sequenceiq.cloudbreak.cloud.event.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.event.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.handler.ParameterGenerator;
import com.sequenceiq.cloudbreak.cloud.init.CloudPlatformConnectors;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstanceMetaData;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceTemplate;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.service.Persister;
import com.sequenceiq.cloudbreak.common.type.AdjustmentType;
import com.sequenceiq.cloudbreak.common.type.ResourceType;

import reactor.Environment;

@Configuration
@ComponentScan("com.sequenceiq.cloudbreak.cloud")
@PropertySource("classpath:application.properties")
public class TestApplicationContext {

    private CloudInstance cloudInstance = new CloudInstance("instanceId",
            new CloudInstanceMetaData("privateIp", "publicIp"),
            new InstanceTemplate("flavor", "groupName", 1L, InstanceStatus.CREATE_REQUESTED));
    private CloudInstance cloudInstanceBad = new CloudInstance("instanceIdBad",
            new CloudInstanceMetaData("privateIp", "publicIp"),
            new InstanceTemplate("flavor", "groupName", 1L, InstanceStatus.CREATE_REQUESTED));

    @Mock
    private CloudPlatformConnectors cloudPlatformConnectors;

    @Mock
    private CloudConnector cloudConnector;

    @Mock
    private Authenticator authenticator;

    @Mock
    private MetadataCollector collector;

    @Mock
    private ResourceConnector resourceConnector;

    @Mock
    private InstanceConnector instanceConnector;

    @Mock
    private PersistenceNotifier persistenceNotifier;

    @Mock
    private Persister persister;

    @Inject
    private ParameterGenerator g;

    @PostConstruct
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Bean(name = "instance")
    public CloudInstance cloudInstance() {
        return cloudInstance;
    }

    @Bean(name = "bad-instance")
    public CloudInstance cloudInstanceBad() {
        return cloudInstanceBad;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ListeningScheduledExecutorService listeningScheduledExecutorService() {
        return MoreExecutors.listeningDecorator(new ScheduledThreadPoolExecutor(1));
    }

    @Bean
    public CloudPlatformConnectors cloudPlatformConnectors() {
        when(cloudPlatformConnectors.get((CloudPlatformVariant) any())).thenReturn(cloudConnector);
        return cloudPlatformConnectors;
    }

    @Bean
    public CloudConnector cloudConnectors() throws Exception {
        CloudResource resource = new CloudResource.Builder().type(ResourceType.HEAT_STACK).name("ref").build();
        when(cloudConnector.authentication()).thenReturn(authenticator);
        when(instanceConnector.metadata()).thenReturn(collector);
        when(authenticator.authenticate((CloudContext) any(), (CloudCredential) any())).thenReturn(g.createAuthenticatedContext());
        when(cloudConnector.platform()).thenReturn("TESTCONNECTOR");
        when(cloudConnector.variant()).thenReturn("TESTVARIANT");
        when(cloudConnector.resources()).thenReturn(resourceConnector);
        when(cloudConnector.instances()).thenReturn(instanceConnector);
        when(resourceConnector.launch((AuthenticatedContext) any(), (CloudStack) any(), (PersistenceNotifier) any(), (AdjustmentType) any(), anyLong()))
                .thenReturn(Arrays.asList(new CloudResourceStatus(resource, ResourceStatus.CREATED)));
        when(resourceConnector.terminate((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any()))
                .thenReturn(Arrays.asList(new CloudResourceStatus(resource, ResourceStatus.DELETED)));
        when(resourceConnector.update((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any()))
                .thenReturn(Arrays.asList(new CloudResourceStatus(resource, ResourceStatus.UPDATED)));
        when(resourceConnector.upscale((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any()))
                .thenReturn(Arrays.asList(new CloudResourceStatus(resource, ResourceStatus.UPDATED)));
        when(resourceConnector.downscale((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any(), anyList()))
                .thenReturn(Arrays.asList(new CloudResourceStatus(resource, ResourceStatus.UPDATED)));
        when(instanceConnector.check((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudInstance>) any()))
                .thenReturn(Arrays.asList(new CloudVmInstanceStatus(cloudInstance, InstanceStatus.STARTED)));
        when(collector.collect((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any(), (List<InstanceTemplate>) any()))
                .thenReturn(Arrays.asList(new CloudVmInstanceStatus(cloudInstance, InstanceStatus.IN_PROGRESS)));
        when(instanceConnector.start((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any(), (List<CloudInstance>) any()))
                .thenReturn(Arrays.asList(new CloudVmInstanceStatus(cloudInstance, InstanceStatus.STARTED)));
        when(instanceConnector.stop((AuthenticatedContext) any(), (CloudStack) any(), (List<CloudResource>) any(), (List<CloudInstance>) any()))
                .thenReturn(Arrays.asList(new CloudVmInstanceStatus(cloudInstance, InstanceStatus.STOPPED)));
        when(instanceConnector.getConsoleOutput((AuthenticatedContext) any(), (CloudStack) any(), eq(cloudInstance)))
                .thenReturn(g.getSshFingerprint() + "    RSA/n-----END SSH HOST KEY FINGERPRINTS-----");
        when(instanceConnector.getConsoleOutput((AuthenticatedContext) any(), (CloudStack) any(), eq(cloudInstanceBad)))
                .thenReturn("XYZ    RSA/n-----END SSH HOST KEY FINGERPRINTS-----");
        return cloudConnector;
    }

    @Bean
    public Environment env() {
        return Environment.initializeIfEmpty();
    }

    @Bean
    public Persister getPersister() {
        return persister;
    }

}
