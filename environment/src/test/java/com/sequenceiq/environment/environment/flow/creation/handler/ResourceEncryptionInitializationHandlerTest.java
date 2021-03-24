package com.sequenceiq.environment.environment.flow.creation.handler;

import static com.sequenceiq.environment.environment.flow.creation.event.EnvCreationStateSelectors.FAILED_ENV_CREATION_EVENT;
import static com.sequenceiq.environment.environment.flow.creation.event.EnvCreationStateSelectors.START_FREEIPA_CREATION_EVENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.environment.environment.domain.Environment;
import com.sequenceiq.environment.environment.dto.EnvironmentDto;
import com.sequenceiq.environment.environment.flow.creation.event.EnvCreationEvent;
import com.sequenceiq.environment.environment.flow.creation.event.EnvCreationFailureEvent;
import com.sequenceiq.environment.environment.service.EnvironmentService;
import com.sequenceiq.flow.reactor.api.event.BaseNamedFlowEvent;
import com.sequenceiq.flow.reactor.api.event.EventSender;

import reactor.bus.Event;
import reactor.bus.Event.Headers;
import reactor.bus.EventBus;

@ExtendWith(MockitoExtension.class)
class ResourceEncryptionInitializationHandlerTest {

    private static final Long ENVIRONMENT_ID = 1234L;

    private static final String ENVIRONMENT_NAME = "environmentName";

    private static final String ENVIRONMENT_CRN = "environmentCrn";

    @Mock
    private EventSender eventSender;

    @Mock
    private EnvironmentService environmentService;

    @Mock
    private Event<EnvironmentDto> environmentDtoEvent;

    @Mock
    private EntitlementService entitlementService;

    @Mock
    private Headers headers;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private ResourceEncryptionInitializationHandler underTest;

    @Captor
    private ArgumentCaptor<BaseNamedFlowEvent> baseNamedFlowEventCaptor;

    @Captor
    private ArgumentCaptor<Headers> headersArgumentCaptor;

    @Captor
    private ArgumentCaptor<Event<EnvCreationFailureEvent>> envCreationFailureEventEventCaptor;

    @BeforeEach
    void setUp() {
        EnvironmentDto eventDto = EnvironmentDto.builder()
                .withId(ENVIRONMENT_ID)
                .withResourceCrn(ENVIRONMENT_CRN)
                .withName(ENVIRONMENT_NAME)
                .build();
        when(environmentDtoEvent.getData()).thenReturn(eventDto);
        when(environmentDtoEvent.getHeaders()).thenReturn(headers);
    }

    @Test
    void acceptEnvironmentNotFound() {
        doAnswer(i -> null).when(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), any(Headers.class));
        when(environmentService.findEnvironmentById(ENVIRONMENT_ID)).thenReturn(Optional.empty());

        underTest.accept(environmentDtoEvent);

        verify(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), headersArgumentCaptor.capture());
        verify(environmentService, never()).save(any());
        verifyEnvCreationEvent();
    }

    @Test
    void acceptTestEnvironmentFailure() {
        IllegalStateException error = new IllegalStateException("error");
        when(environmentService.findEnvironmentById(ENVIRONMENT_ID)).thenThrow(error);

        underTest.accept(environmentDtoEvent);

        verify(eventBus).notify(anyString(), envCreationFailureEventEventCaptor.capture());
        verifyEnvCreationFailedEvent(error);
    }

    @Test
    void acceptTestEnvironmentShouldNotBeUpdatedWhenCloudPlatformIsNotAzure() {
        doAnswer(i -> null).when(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), any(Headers.class));
        Environment environment = new Environment();
        environment.setCloudPlatform("Dummy-cloud");
        when(environmentService.findEnvironmentById(ENVIRONMENT_ID)).thenReturn(Optional.of(environment));

        underTest.accept(environmentDtoEvent);

        verify(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), headersArgumentCaptor.capture());
        verify(environmentService, never()).save(any());
        verifyEnvCreationEvent();
    }

    @Test
    void acceptTestEnvironmentShouldNotBeUpdatedWhenEncryptionKeyUrlIsEmpty() {
        doAnswer(i -> null).when(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), any(Headers.class));
        Environment environment = new Environment();
        environment.setCloudPlatform("AZURE");
        when(environmentService.findEnvironmentById(ENVIRONMENT_ID)).thenReturn(Optional.of(environment));

        underTest.accept(environmentDtoEvent);

        verify(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), headersArgumentCaptor.capture());
        verify(environmentService, never()).save(any());
        verifyEnvCreationEvent();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void selector() {
        doAnswer(i -> null).when(eventSender).sendEvent(baseNamedFlowEventCaptor.capture(), any(Headers.class));
        assertThat(underTest.selector()).isEqualTo("INITIALIZE_ENVIRONMENT_RESOURCE_ENCRYPTION_EVENT");
    }

    private void verifyEnvCreationEvent() {
        BaseNamedFlowEvent event = baseNamedFlowEventCaptor.getValue();
        assertThat(event).isInstanceOf(EnvCreationEvent.class);

        EnvCreationEvent envCreationEvent = (EnvCreationEvent) event;
        assertThat(envCreationEvent.getResourceName()).isEqualTo(ENVIRONMENT_NAME);
        assertThat(envCreationEvent.getResourceCrn()).isEqualTo(ENVIRONMENT_CRN);
        assertThat(envCreationEvent.getResourceId()).isEqualTo(ENVIRONMENT_ID);
        assertThat(envCreationEvent.selector()).isEqualTo(START_FREEIPA_CREATION_EVENT.selector());

        assertThat(headersArgumentCaptor.getValue()).isSameAs(headers);
    }

    private void verifyEnvCreationFailedEvent(Exception exceptionExpected) {
        Event<EnvCreationFailureEvent> value = envCreationFailureEventEventCaptor.getValue();
        EnvCreationFailureEvent event = value.getData();
        assertThat(event).isInstanceOf(EnvCreationFailureEvent.class);

        EnvCreationFailureEvent envCreateFailedEvent = (EnvCreationFailureEvent) event;
        assertThat(envCreateFailedEvent.getResourceName()).isEqualTo(ENVIRONMENT_NAME);
        assertThat(envCreateFailedEvent.getResourceCrn()).isEqualTo(ENVIRONMENT_CRN);
        assertThat(envCreateFailedEvent.getResourceId()).isEqualTo(ENVIRONMENT_ID);
        assertThat(envCreateFailedEvent.selector()).isEqualTo(FAILED_ENV_CREATION_EVENT.selector());
        assertThat(envCreateFailedEvent.getException()).isSameAs(exceptionExpected);
    }
}
