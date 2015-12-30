/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.mitreid.multiparty.service;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mitreid.multiparty.model.Resource;
import org.mitreid.multiparty.model.SharedResourceSet;
import org.springframework.stereotype.Service;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * @author jricher
 *
 */
@Service
public class InMemoryResourceService implements ResourceService {

	private Multimap<String, Resource> resources = MultimapBuilder.linkedHashKeys().hashSetValues().build();
	private Map<String, SharedResourceSet> sharedResourceSets = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see org.mitreid.multiparty.service.ResourceService#getAllForUser(java.security.Principal)
	 */
	@Override
	public Collection<Resource> getAllForUser(Principal p) {
		
		if (p == null) {
			return Collections.emptySet();
		} else {
			Collection<Resource> res = resources.get(p.getName());
			if (res == null) {
				return Collections.emptySet();
			} else {
				return res;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.mitreid.multiparty.service.ResourceService#addResource(org.mitreid.multiparty.model.Resource, java.security.Principal)
	 */
	@Override
	public void addResource(Resource res, Principal p) {
		if (p == null) {
			throw new IllegalArgumentException("Principal can't be null");
		}
		resources.put(p.getName(), res);		
	}

	/* (non-Javadoc)
	 * @see org.mitreid.multiparty.service.ResourceService#getSharedResourceSetForUser(java.security.Principal)
	 */
	@Override
	public SharedResourceSet getSharedResourceSetForUser(Principal p) {
		if (p == null) {
			return null;
		} else {
			SharedResourceSet sharedResourceSet = sharedResourceSets.get(p.getName());
			return sharedResourceSet;
		}
	}

	/* (non-Javadoc)
	 * @see org.mitreid.multiparty.service.ResourceService#shareResourceForUser(org.mitreid.multiparty.model.SharedResourceSet, java.security.Principal)
	 */
	@Override
	public void shareResourceForUser(SharedResourceSet srs, Principal p) {
		sharedResourceSets.put(p.getName(), srs);
	}

}
