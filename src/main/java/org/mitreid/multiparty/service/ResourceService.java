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

import org.mitreid.multiparty.model.Resource;
import org.mitreid.multiparty.model.SharedResourceSet;

/**
 * @author jricher
 *
 */
public interface ResourceService {

	/**
	 * @param p
	 * @return
	 */
	Collection<Resource> getAllForUser(Principal p);

	/**
	 * @param res
	 * @param p
	 */
	void addResource(Resource res, Principal p);

	/**
	 * @param p
	 * @return
	 */
	SharedResourceSet getSharedResourceSetForUser(Principal p);

	/**
	 * @param srs
	 * @param p
	 */
	void shareResourceForUser(SharedResourceSet srs, Principal p);

	/**
	 * @param rsId
	 * @return
	 */
	Resource getById(String rsId);

}
