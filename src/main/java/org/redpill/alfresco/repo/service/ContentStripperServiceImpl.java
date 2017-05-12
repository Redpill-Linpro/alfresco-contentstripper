package org.redpill.alfresco.repo.service;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import org.redpill.alfresco.repo.domain.FileLocationDAOImpl;
import org.redpill.alfresco.repo.domain.FileLocationEntity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


public class ContentStripperServiceImpl implements ContentStripperService, InitializingBean {
	private static final Logger logger = Logger.getLogger(ContentStripperServiceImpl.class);

	SiteService siteService;
	NodeService nodeService;
	SearchService searchService;
	ContentService contentService;
	NamespaceService namespaceService;
	DictionaryService dictionaryService;
	FileLocationDAOImpl fileLocationDAO;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(nodeService, "you must provide an instance of NodeService");
		Assert.notNull(siteService, "you must provide an instance of SiteService");
		Assert.notNull(searchService, "you must provide an instance of SearchService");
		Assert.notNull(contentService, "you must provide an instance of ContentService");
		Assert.notNull(namespaceService, "you must provide an instance of NamespaceService");
		Assert.notNull(dictionaryService, "you must provide an instance of DictionaryService");
		Assert.notNull(fileLocationDAO, "you must provide an instance of FileLocationDAOImpl");
	}

	private List<NodeRef> listAllFileNodesDeep(NodeRef nodeRef, List<NodeRef> allDeep) {

		QName type = nodeService.getType(nodeRef);
		if (ContentModel.TYPE_FOLDER.equals(type)) {
			for (ChildAssociationRef childAssoc : nodeService.getChildAssocs(nodeRef)) {
				listAllFileNodesDeep(childAssoc.getChildRef(), allDeep);
			}
		} else if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
			allDeep.add(nodeRef);
		} else {
		}

		return allDeep;
	}

	@Override
	public List<String> getSurfContigNodes() {
		String surfConfigNodesQuery = "PATH:\"/app:company_home/st:sites/cm:surf-config//*\"";

		SearchParameters sp = new SearchParameters();
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

		sp.setQuery(surfConfigNodesQuery);
		ResultSet results = searchService.query(sp);
		List<String> surfConfigNodes = new ArrayList<>();

		// The dashboard nodes are not indexed, so we need nodeService-calls
		for (ResultSetRow row : results) {
			try {

				List<NodeRef> allFiles = listAllFileNodesDeep(row.getNodeRef(), new ArrayList<NodeRef>());

				for (NodeRef file : allFiles) {
					// check if the nodeRef is of type cm:content or inherits
					// from
					// cm:content
					QName type = nodeService.getType(file);
					if (type.equals(ContentModel.TYPE_CONTENT)
							|| dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
						surfConfigNodes.add(getContentUrl(file, ContentModel.PROP_CONTENT));
					}
				}
			} catch (Exception e) {
				logger.warn("An unhandled exception occurred when resolving content url to surf-config node. nodeRef: "
						+ row.getNodeRef(), e);
			}
		}

		return surfConfigNodes;
	}

	@Override
	public List<String> getDictionaryNodes() {
		String dictionaryNodesQuery = "PATH:\"/app:company_home/app:dictionary//*\"";

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

		return dictionaryNodes;
	}

	@Override
	public List<String> getPersonPreferenceValuesNodes() {
		String personNodesQuery = "TYPE:\"cm:person\"";
		SearchParameters sp = new SearchParameters();
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

		sp.setQuery(personNodesQuery);
		ResultSet results = searchService.query(sp);

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

		return personNodes;
	}

	@Override
	public List<String> getSiteNodes(String shortName) {
		SiteInfo site = siteService.getSite(shortName);
		List<String> siteNodes = new ArrayList<>();
		if (site != null) {
			String siteNodesQuery = "PATH:\"/app:company_home/st:sites/cm:" + shortName + "//*\"";
			SearchParameters sp = new SearchParameters();
			sp.setLanguage(SearchService.LANGUAGE_LUCENE);
			sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
			sp.setQuery(siteNodesQuery);
			ResultSet results = searchService.query(sp);

			for (ResultSetRow row : results) {
				try {
					// check if the nodeRef is of type cm:content or
					// inherits from
					// cm:content
					QName type = nodeService.getType(row.getNodeRef());
					if (type.equals(ContentModel.TYPE_CONTENT)
							|| dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {

						siteNodes.add(getContentUrl(row.getNodeRef(), ContentModel.PROP_CONTENT));
					}
				} catch (Exception e) {
					logger.warn("An unhandled exception occurred when resolving content url to the site node. nodeRef: "
							+ row.getNodeRef(), e);
				}
			}

		}

		return siteNodes;
	}

	@Override
	public FileLocationEntity getFileByLocalName(String localName) {
		FileLocationEntity fileLocation = fileLocationDAO.executeSearchQuery(localName);
		return fileLocation;
	}

	private String getContentUrl(final NodeRef nodeRef, final QName propertyQName) throws Exception {

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

	public void setFileLocationDAO(FileLocationDAOImpl fileLocationDAO) {
		this.fileLocationDAO = fileLocationDAO;
	}

}
