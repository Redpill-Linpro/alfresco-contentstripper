package org.redpill.alfresco.repo.webscripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redpill.alfresco.repo.domain.FileLocationEntity;
import org.redpill.alfresco.repo.service.ContentStripperService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

/**
 * Webscript that returns file paths to the contentstore for content properties
 * that needs to be preserved when running the alfresco-contentstripper python
 * script that is distributed within this project.
 * 
 * By default all content in the dataDictionary is preserved together with the
 * cm:preferenceValues of all users.
 * 
 * Also a keystore node and the license file nodes are preserved by default - if present.
 * 
 * By suppling the request parameter shortName with a site shortname the content
 * properties of that site is also preserved.
 * 
 * @author erib - mostly ripped from old Alfresco Tooling project by @oakman
 *         and @carn
 *
 */
public class GetContentPathsToExclude extends DeclarativeWebScript implements InitializingBean {
	private static final Logger logger = Logger.getLogger(GetContentPathsToExclude.class);

	ContentStripperService contentStripperService;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(contentStripperService, "you must provide an instance of ContentStripperService");
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<>();

		String shortName = req.getParameter("shortName");
		
		if (logger.isDebugEnabled()){
			logger.debug("Requesting paths to nodes that should be preserved during content store truncation.");
		}

		List<String> dictionaryNodes = contentStripperService.getDictionaryNodes();

		model.put("dictionaryNodes", dictionaryNodes);

		List<String> surfConfigUserNodes = contentStripperService.getSurfConfigUserNodes();
		model.put("surfConfigUserNodes", surfConfigUserNodes);

		List<String> surfConfigSiteNodes = contentStripperService.getSurfConfigSiteNodes();
		model.put("surfConfigSiteNodes", surfConfigSiteNodes);

		List<String> personNodes = contentStripperService.getPersonPreferenceValuesNodes();
		model.put("personNodes", personNodes);

		// Get the keystore file location by its local_name in the alf_qname
		// table
		FileLocationEntity keystoreLocation = contentStripperService.getFileByLocalName("keyStore");

		if (keystoreLocation != null) {
			model.put("keystoreNode", keystoreLocation.getContentUrl());
		}

		// Get the license file location by its local_name in the alf_qname
		// table
		FileLocationEntity licenseFileLocation = contentStripperService.getFileByLocalName("versionEdition");

		if (licenseFileLocation != null) {
			model.put("licenseNode", licenseFileLocation.getContentUrl());
		}

		List<String> siteNodes = new ArrayList<>();

		if (shortName != null && shortName.length() > 0) {
			siteNodes = contentStripperService.getSiteNodes(shortName);
		}
		model.put("siteNodes", siteNodes);
		return model;
	}

	public void setContentStripperService(ContentStripperService contentStripperService) {
		this.contentStripperService = contentStripperService;
	}
}
