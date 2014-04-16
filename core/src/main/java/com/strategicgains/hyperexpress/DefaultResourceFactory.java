/*
    Copyright 2014, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.hyperexpress;

import java.util.HashMap;
import java.util.Map;

import com.strategicgains.hyperexpress.domain.Resource;

/**
 * A ResourceFactory implementation that has no functionality on its own, but must have ResourceFactoryStrategy
 * implementations added to it, associating the strategy to a given content type.
 * </p>
 * Usage: ResourceFactory rf = new DefaultResourceFactory()
 *     .addStrategy(new HalResourceFactoryStrategy(), "application/json")
 *     .addStrategy(new AtomResourceFactoryStrategy(), "application/xml");
 * </p>
 * Resource resource = rf.createResource(object, response.getContentType());
 * 
 * @author toddf
 * @since Apr 7, 2014
 */
public class DefaultResourceFactory
implements ResourceFactory
{
	private Map<String, ResourceFactoryStrategy> strategies = new HashMap<String, ResourceFactoryStrategy>();

	/**
	 * Create a Resource using data from the given object for the requested content type.
	 * 
	 * @param object the object to use as a source of properties (data).
	 * @param contentType the content type to use when creating the new resource.
	 */
	@Override
	public Resource createResource(Object object, String contentType)
	{
		ResourceFactoryStrategy strategy = strategies.get(contentType);
		
		if (strategy == null)
		{
			throw new ResourceException("Cannot create resource for content type: " + contentType);
		}

		return strategy.createResource(object);
	}

	/**
	 * Add a ResourceFactoryStrategy to create resource instances for the given content type.
	 * 
	 * @param strategy the ResourceFactoryStrategy to use when creating new instances.
	 * @param contentType the content type the strategy should be invoked for.
	 * @return this DefaultResourceFactory to facilitate method chaining.
	 */
	public DefaultResourceFactory addStrategy(ResourceFactoryStrategy strategy, String contentType)
	{
		if (strategies.containsKey(contentType))
		{
			throw new ResourceException("Duplicate content type: " + contentType);
		}

		strategies.put(contentType, strategy);
		return this;
	}
}