package org.elasticspring.core.formation;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.ListStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.ListStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResourceSummary;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes a fully populated {@link AmazonStackResourceRegistry} instance representing the resources of
 * the specified stack.
 *
 * @author Christian Stettler
 */
public class AmazonStackResourceRegistryFactoryBean extends AbstractFactoryBean<AmazonStackResourceRegistry> {

	private final AmazonCloudFormationClient amazonCloudFormationClient;
	private final String stackName;

	public AmazonStackResourceRegistryFactoryBean(AmazonCloudFormationClient amazonCloudFormationClient, String stackName) {
		this.amazonCloudFormationClient = amazonCloudFormationClient;
		this.stackName = stackName;
	}

	@Override
	public Class<?> getObjectType() {
		return AmazonStackResourceRegistry.class;
	}

	@Override
	protected AmazonStackResourceRegistry createInstance() throws Exception {
		ListStackResourcesResult listStackResourcesResult = this.amazonCloudFormationClient.listStackResources(new ListStackResourcesRequest().withStackName(this.stackName));
		List<StackResourceSummary> stackResourceSummaries = listStackResourcesResult.getStackResourceSummaries();

		return new StaticAmazonStackResourceRegistry(convertToStackResourceMappings(stackResourceSummaries));
	}

	private static Map<String, String> convertToStackResourceMappings(List<StackResourceSummary> stackResourceSummaries) {
		Map<String, String> stackResourceMappings = new HashMap<String, String>();

		for (StackResourceSummary stackResourceSummary : stackResourceSummaries) {
			stackResourceMappings.put(stackResourceSummary.getLogicalResourceId(), stackResourceSummary.getPhysicalResourceId());
		}

		return stackResourceMappings;
	}


	private static class StaticAmazonStackResourceRegistry implements AmazonStackResourceRegistry {

		private final Map<String, String> physicalResourceIdsByLogicalResourceId;

		private StaticAmazonStackResourceRegistry(Map<String, String> physicalResourceIdsByLogicalResourceId) {
			this.physicalResourceIdsByLogicalResourceId = physicalResourceIdsByLogicalResourceId;
		}

		@Override
		public String lookupPhysicalResourceId(String logicalResourceId) {
			return this.physicalResourceIdsByLogicalResourceId.get(logicalResourceId);
		}
	}

}
