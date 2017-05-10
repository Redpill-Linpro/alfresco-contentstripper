package org.redpill.alfresco.repo.webscripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

/**
 * Webscript that returns file paths to the contentstore for content properties
 * that needs to be preserved when running the alfresco-contentstripper python script
 * that is distributed within this project.
 * 
 * By default all content in the dataDictionary is preserved toghether with the
 * cm:preferenceValues of all users.
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

	SiteService siteService;
	NodeService nodeService;
	SearchService searchService;
	ContentService contentService;
	NamespaceService namespaceService;
	DictionaryService dictionaryService;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(nodeService, "you must provide an instance of NodeService");
		Assert.notNull(siteService, "you must provide an instance of SiteService");
		Assert.notNull(searchService, "you must provide an instance of SearchService");
		Assert.notNull(contentService, "you must provide an instance of ContentService");
		Assert.notNull(namespaceService, "you must provide an instance of NamespaceService");
		Assert.notNull(dictionaryService, "you must provide an instance of DictionaryService");
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<>();

		String shortName = req.getParameter("shortName");
		String dictionaryNodesQuery = "PATH:\"/app:company_home/app:dictionary//*\"";
		String personNodesQuery = "TYPE:\"cm:person\"";

		SearchParameters sp = new SearchParameters();
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

		sp.setQuery(dictionaryNodesQuery);

		ResultSet results = searchService.query(sp);
		if (results != null && results.length() > 0) {

			if (logger.isDebugEnabled()) {
				logger.debug(
						"Listing Data Dictionary nodes, found " + results.length() + " nodes with potential content!");
			}
		}
		List<String> dictionaryNodes = new ArrayList<>();
		for (ResultSetRow row : results) {
			try {

				// check if the nodeRef is of type cm:content or inherits from
				// cm:content
				QName type = nodeService.getType(row.getNodeRef());
				if (type.equals(ContentModel.TYPE_CONTENT)
						|| dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
					dictionaryNodes.add(getContentUrl(row.getNodeRef(), ContentModel.PROP_CONTENT));
				}
			} catch (Exception e) {
				logger.warn("An unhandled exception occurred when resolving content url to dictionary node. nodeRef: "
						+ row.getNodeRef(), e);
			}
		}
		model.put("dictionaryNodes", dictionaryNodes);

		sp.setQuery(personNodesQuery);
		results = searchService.query(sp);

		List<String> personNodes = new ArrayList<>();
		for (ResultSetRow row : results) {
			try {
				personNodes.add(getContentUrl(row.getNodeRef(), ContentModel.PROP_PREFERENCE_VALUES));
			} catch (Exception e) {
				logger.warn(
						"An unhandled exception occurred when resolving content url to preference values for a user. nodeRef: "
								+ row.getNodeRef(),
						e);
			}
		}

		model.put("personNodes", personNodes);

		List<String> siteNodes = new ArrayList<>();

		if (shortName != null && shortName.length() > 0) {
			SiteInfo site = siteService.getSite(shortName);
			if (site != null) {
				String siteNodesQuery = "PATH:\"/app:company_home/st:sites/cm:" + shortName + "//*\"";
				sp.setQuery(siteNodesQuery);
				results = searchService.query(sp);
				for (ResultSetRow row : results) {
					try {
						// check if the nodeRef is of type cm:content or inherits from
						// cm:content
						QName type = nodeService.getType(row.getNodeRef());
						if (type.equals(ContentModel.TYPE_CONTENT)
								|| dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {

							siteNodes.add(getContentUrl(row.getNodeRef(), ContentModel.PROP_CONTENT));
						}
					} catch (Exception e) {
						logger.warn(
								"An unhandled exception occurred when resolving content url to the site node. nodeRef: "
										+ row.getNodeRef(),
								e);
					}
				}

			}

		}
		model.put("siteNodes", siteNodes);
		return model;
	}

	public String getContentUrl(final NodeRef nodeRef, final QName propertyQName) throws Exception {

		if (nodeRef != null) {

			if (nodeService.getProperty(nodeRef, propertyQName) == null) {
				throw new Exception("Could not find property " + propertyQName + " on node " + nodeRef);
			}

			final ContentReader reader = contentService.getReader(nodeRef, propertyQName);
			if (reader != null && reader.exists()) {
				return reader.getContentUrl();
			} else {
				throw new Exception(
						"Could not resolve content url for NodeRef " + nodeRef + " and property " + propertyQName);
			}
		}

		throw new Exception("Could not resolve content url when NodeRef is 'null'! Is index corrupt?");

	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

}
