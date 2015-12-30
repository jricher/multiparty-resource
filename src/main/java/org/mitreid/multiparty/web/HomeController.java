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

import static org.mitre.util.JsonUtils.getAsLong;
import static org.mitre.util.JsonUtils.getAsString;
import static org.mitre.util.JsonUtils.getAsStringSet;

import java.security.Principal;
import java.text.ParseException;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.util.JsonUtils;
import org.mitreid.multiparty.model.MultipartyServerConfiguration;
import org.mitreid.multiparty.model.Resource;
import org.mitreid.multiparty.model.SharedResourceSet;
import org.mitreid.multiparty.service.AccessTokenService;
import org.mitreid.multiparty.service.MultipartyServerConfigurationService;
import org.mitreid.multiparty.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private ClientConfigurationService clientConfig;
	
	@Autowired
	private MultipartyServerConfigurationService serverConfig;
	
	@Autowired
	private AccessTokenService acccessTokenService;

	private HttpClient httpClient = HttpClientBuilder.create()
			.useSystemProperties()
			.build();

	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
	
	private RestTemplate restTemplate = new RestTemplate(httpFactory);

	private JsonParser parser = new JsonParser();
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model, Principal p, Authentication a) {

		// get resources for current user
		Collection<Resource> resources = resourceService.getAllForUser(p);
		// display form
		
		model.addAttribute("resources", resources);
		
		
		if (a instanceof OIDCAuthenticationToken) {
			try {
				model.addAttribute("issuer", ((OIDCAuthenticationToken)a).getIdToken().getJWTClaimsSet().getIssuer());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.error("WAT", e);
			}
		}
		
		SharedResourceSet sharedResourceSet = resourceService.getSharedResourceSetForUser(p);
		
		if (sharedResourceSet != null) {
			MultipartyServerConfiguration serverConfiguration = serverConfig.getServerConfiguration(sharedResourceSet.getIssuer());

			model.addAttribute("sharedResourceSet", sharedResourceSet);
			model.addAttribute("serverConfiguration", serverConfiguration);
		}
		
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
			res.id = new RandomValueStringGenerator().generate();
			// save it into the store
			resourceService.addResource(res, p);
			// redirect back to the home page
		}
		
		return "redirect:";
	}
	
	@RequestMapping(value = "/share", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@PreAuthorize("hasRole('ROLE_USER')")
	public String shareResource(@RequestParam("issuer") String issuer, Principal p, HttpSession session) {
		// load the resource
		SharedResourceSet oldSharedResourceSet = resourceService.getSharedResourceSetForUser(p);
		// discover/load the auth server configuration
		MultipartyServerConfiguration server = serverConfig.getServerConfiguration(issuer);
		// load client configuration (register if needed)
		RegisteredClient client = clientConfig.getClientConfiguration(server);
		// get an access token (this might redirect)
		String accessTokenValue = acccessTokenService.getAccessToken(server, client);
		if (Strings.isNullOrEmpty(accessTokenValue)) {
			// we don't have an access token yet, let's go get one
			// first save what we're working on so we can come back to it
			session.setAttribute("SHARE_ISSUER", issuer);
			return redirectForPAT(server, client, session);
		} else {
			// register the resource set
			unregisterOldResourceSet(oldSharedResourceSet, accessTokenValue);
			registerResourceSet(p, issuer, server, accessTokenValue);
			
			// redirect back to home page
			return "redirect:";
		}
	}
	
	/**
	 * @param oldSharedResourceSet
	 * @param accessTokenValue
	 */
	private void unregisterOldResourceSet(SharedResourceSet oldSharedResourceSet, String accessTokenValue) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + accessTokenValue);

		HttpEntity<String> request = new HttpEntity<>(headers);

		restTemplate.exchange(oldSharedResourceSet.getLocation(), HttpMethod.DELETE, request, String.class);
	}

	/**
	 * @param server
	 * @param client
	 * @return
	 */
	private String redirectForPAT(MultipartyServerConfiguration server, RegisteredClient client, HttpSession session) {
		
		String state = new RandomValueStringGenerator().generate();
		session.setAttribute("STATE", state);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(server.getAuthorizationEndpointUri());
		builder.queryParam("client_id", client.getClientId());
		builder.queryParam("response_type", "code");
		builder.queryParam("state", state);
		builder.queryParam("scope", "uma_protection");
		builder.queryParam("redirect_uri", "http://localhost:8080/multiparty-resource/pat_callback");
		
		logger.warn("Redirecting to: " + builder.toUriString());
		
		return "redirect:" + builder.toUriString();
	}

	@RequestMapping("/pat_callback")
	@PreAuthorize("hasRole('ROLE_USER')")
	public String patCallback(@RequestParam("code") String code, @RequestParam("state") String state, Principal p, HttpSession session) {
		String savedState = (String) session.getAttribute("STATE");
		
		if (!state.equals(savedState)) {
			throw new IllegalArgumentException("State doesn't match: shenanigans!");
		}
		
		String issuer = (String) session.getAttribute("SHARE_ISSUER");
		MultipartyServerConfiguration server = serverConfig.getServerConfiguration(issuer);
		RegisteredClient client = clientConfig.getClientConfiguration(server);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		
		params.add("client_id", client.getClientId());
		params.add("client_secret", client.getClientSecret());
		params.add("redirect_uri", "http://localhost:8080/multiparty-resource/pat_callback");
		params.add("grant_type", "authorization_code");
		params.add("code", code);
		
		String responseString = restTemplate.postForObject(server.getTokenEndpointUri(), params, String.class);
		JsonObject o = parser.parse(responseString).getAsJsonObject();
		
		String accessTokenValue = o.get("access_token").getAsString();
		acccessTokenService.saveAccesstoken(server, client, accessTokenValue);
		
		// now we register the resource set
		
		return registerResourceSet(p, issuer, server, accessTokenValue);
	}

	private String registerResourceSet(Principal p, String issuer, MultipartyServerConfiguration server, String accessTokenValue) {
		JsonObject requestJson = new JsonObject();
		/*
 				rs.setId(getAsLong(o, "_id"));
				rs.setName(getAsString(o, "name"));
				rs.setIconUri(getAsString(o, "icon_uri"));
				rs.setType(getAsString(o, "type"));
				rs.setScopes(getAsStringSet(o, "scopes"));
				rs.setUri(getAsString(o, "uri"));

		 */
		requestJson.addProperty("name", p.getName() + "'s Resources");
		JsonArray scopes = new JsonArray();
		scopes.add(new JsonPrimitive("read"));
		scopes.add(new JsonPrimitive("write"));
		requestJson.add("resource_set_scopes", scopes);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + accessTokenValue);

		HttpEntity<String> request = new HttpEntity<String>(requestJson.toString(), headers);
		
		HttpEntity<String> responseEntity = restTemplate.postForEntity(server.getResourceSetRegistrationEndpoint(), request, String.class);
		
		JsonObject rso = parser.parse(responseEntity.getBody()).getAsJsonObject();
		String location = responseEntity.getHeaders().getLocation().toString();

		SharedResourceSet srs = new SharedResourceSet();
		srs.setIssuer(issuer);
		srs.setRsid(rso.get("_id").getAsString());
		srs.setUserAccessPolicyUri(rso.get("user_access_policy_uri").getAsString());
		srs.setLocation(location);
		
		resourceService.shareResourceForUser(srs, p);
		
		return "redirect:";
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
