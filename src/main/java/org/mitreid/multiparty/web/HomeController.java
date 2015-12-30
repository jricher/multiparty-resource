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
package org.mitreid.multiparty.web;

import java.security.Principal;
import java.util.Collection;
import java.util.UUID;

import org.mitreid.multiparty.model.Resource;
import org.mitreid.multiparty.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private ResourceService resourceService;
	
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model, Principal p) {

		// get resources for current user
		Collection<Resource> resources = resourceService.getAllForUser(p);
		// display form
		
		model.addAttribute("resources", resources);
		
		return "home";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@PreAuthorize("hasRole('ROLE_USER')")
	public String createResource(@RequestParam("label") String label, @RequestParam("value") String value, Principal p) {
		if (!Strings.isNullOrEmpty(label) && !Strings.isNullOrEmpty(value)) {
			// create a unique resource for the current user (use principal name?)
			Resource res = new Resource();
			res.label = label;
			res.value = value;
			// give it a random ID
			res.id = UUID.randomUUID().toString();
			// save it into the store
			resourceService.addResource(res, p);
			// redirect back to the home page
		}
		
		return "redirect:";
	}
	
	@RequestMapping(value = "/share", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@PreAuthorize("hasRole('ROLE_USER')")
	public void shareResource(@RequestParam("authserver") String authServer) {
		// load the resource
		// discover/load the auth server configuration
		// load client configuration (register if needed)
		// register resource set with AS
		// save resource set registration
		// redirect back to home page
	}
	
	@RequestMapping("/login")
	public String login(Principal p) {
		return "login";
	}
	
	@RequestMapping(value = "/api/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void getResource(@PathVariable("id") String rsId, OAuth2Authentication auth) {
		// check the authentication object to get the incoming token
		// introspect/load the token
		// load the resource from the ID
		// check to see if the token is from the AS associated with the resource
		// check to see if the token has the right scopes
		// check to see that the token is for the right resource set
		// if the token isn't good enough, return a ticket with the AS reference
		// if the resource isn't shared, return a 404
		// if the token is good enough, return the resource
		
	}

}
