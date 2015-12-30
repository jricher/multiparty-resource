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

package org.mitreid.multiparty.model;

import org.mitre.uma.model.ResourceSet;

/**
 * @author jricher
 *
 */
public class SharedResourceSet {

	private ResourceSet resourceSet;
	private String issuer;
	private String rsid;
	private String userAccessPolicyUri;
	private String location;
	
	/**
	 * @return the resourceSet
	 */
	public ResourceSet getResourceSet() {
		return resourceSet;
	}
	/**
	 * @param resourceSet the resourceSet to set
	 */
	public void setResourceSet(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}
	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}
	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	/**
	 * @return the rsid
	 */
	public String getRsid() {
		return rsid;
	}
	/**
	 * @param rsid the rsid to set
	 */
	public void setRsid(String rsid) {
		this.rsid = rsid;
	}
	/**
	 * @return the userAccessPolicyUri
	 */
	public String getUserAccessPolicyUri() {
		return userAccessPolicyUri;
	}
	/**
	 * @param userAccessPolicyUri the userAccessPolicyUri to set
	 */
	public void setUserAccessPolicyUri(String userAccessPolicyUri) {
		this.userAccessPolicyUri = userAccessPolicyUri;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	
	
}
