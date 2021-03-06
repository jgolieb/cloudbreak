package com.sequenceiq.cloudbreak;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.sequenceiq.cloudbreak.tag.CentralTagUpdater;
import com.sequenceiq.cloudbreak.tag.DefaultApplicationTag;
import com.sequenceiq.cloudbreak.tag.DefaultCostTaggingService;
import com.sequenceiq.cloudbreak.tag.request.CDPTagGenerationRequest;
import com.sequenceiq.cloudbreak.tag.request.CDPTagMergeRequest;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCostTaggingServiceTest {

    @InjectMocks
    private DefaultCostTaggingService underTest;

    @Mock
    private CentralTagUpdater centralTagUpdater;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPrepareDefaultTagsForAWSShouldReturnAllDefaultMap() {
        Map<String, String> result = underTest.prepareDefaultTags(tagRequest("AWS"));

        Assert.assertEquals(3L, result.size());
        Assert.assertEquals("environment-crn", result.get(DefaultApplicationTag.ENVIRONMENT_CRN.key()));
        Assert.assertEquals("creator-crn", result.get(DefaultApplicationTag.CREATOR_CRN.key()));
        Assert.assertEquals("resource-crn", result.get(DefaultApplicationTag.RESOURCE_CRN.key()));
    }

    @Test
    public void testPrepareDefaultTagsForAWSAndAdditionalTagsShouldReturnAllDefaultMapPlusTagsWhichAreNotEmpty() {
        Map<String, String> result = underTest.prepareDefaultTags(tagRequest("AWS", new HashMap<>(), new HashMap<>()));

        Assert.assertEquals(3L, result.size());
        Assert.assertEquals("environment-crn", result.get(DefaultApplicationTag.ENVIRONMENT_CRN.key()));
        Assert.assertEquals("creator-crn", result.get(DefaultApplicationTag.CREATOR_CRN.key()));
        Assert.assertEquals("resource-crn", result.get(DefaultApplicationTag.RESOURCE_CRN.key()));
    }

    @Test
    public void testPrepareDefaultTagsForGCPShouldReturnAllDefaultMap() {
        Map<String, String> result = underTest.prepareDefaultTags(tagRequest("GCP"));

        Assert.assertEquals(3L, result.size());
        Assert.assertEquals("environment-crn", result.get(DefaultApplicationTag.ENVIRONMENT_CRN.key().toLowerCase()));
        Assert.assertEquals("creator-crn", result.get(DefaultApplicationTag.CREATOR_CRN.key().toLowerCase()));
        Assert.assertEquals("resource-crn", result.get(DefaultApplicationTag.RESOURCE_CRN.key().toLowerCase()));
        Assert.assertNull(result.get(DefaultApplicationTag.OWNER.key()));
    }

    @Test
    public void testPrepareDefaultTagsForAZUREWhenOwnerPresentedShouldReturnAllDefaultMap() {
        Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put(DefaultApplicationTag.OWNER.key(), "appletree");
        sourceMap.put(DefaultApplicationTag.owner.key(), "appletree");

        Map<String, String> result = underTest.prepareDefaultTags(tagRequest("AZURE", sourceMap, new HashMap<>()));

        Assert.assertEquals(3L, result.size());
        Assert.assertEquals("environment-crn", result.get(DefaultApplicationTag.ENVIRONMENT_CRN.key()));
        Assert.assertEquals("creator-crn", result.get(DefaultApplicationTag.CREATOR_CRN.key()));
        Assert.assertEquals("resource-crn", result.get(DefaultApplicationTag.RESOURCE_CRN.key()));
        Assert.assertNull(result.get(DefaultApplicationTag.OWNER.key()));
    }

    @Test
    public void testMergeTagsShouldReturnWithAnUnion() {
        Map<String, String> envMap = new HashMap<>();
        envMap.put("", "apple1");
        envMap.put("apple2", "");
        envMap.put("apple3", "apple3");
        envMap.put("apple4", "apple4");
        Map<String, String> requestTag = new HashMap<>();
        envMap.put("pear1", "");
        envMap.put("", "pear2");
        envMap.put("pear3", "pear3");
        envMap.put("pear4", "pear4");

        Map<String, String> result = underTest.mergeTags(mergeRequest("AWS", envMap, requestTag));

        Assert.assertEquals(4L, result.size());
        Assert.assertEquals("pear3", "pear3");
        Assert.assertEquals("pear4", "pear4");
        Assert.assertEquals("apple3", "apple3");
        Assert.assertEquals("apple4", "apple4");
    }

    private CDPTagGenerationRequest tagRequest(String platform) {
        return tagRequest(platform, new HashMap<>(), new HashMap<>());
    }

    private CDPTagGenerationRequest tagRequest(String platform, Map<String, String> sourceMap, Map<String, String> accountTags) {
        return CDPTagGenerationRequest.Builder.builder()
                .withEnvironmentCrn("environment-crn")
                .withCreatorCrn("creator-crn")
                .withResourceCrn("resource-crn")
                .withUserName("apache1@apache.com")
                .withPlatform(platform)
                .withAccountId("pepsi")
                .withIsInternalTenant(true)
                .withSourceMap(sourceMap)
                .withAccountTags(accountTags)
                .build();
    }

    private CDPTagMergeRequest mergeRequest(String platform, Map<String, String> envMap, Map<String, String> requestTag) {
        return CDPTagMergeRequest.Builder.builder()
                .withPlatform(platform)
                .withEnvironmentTags(envMap)
                .withRequestTags(requestTag)
                .build();
    }
}
