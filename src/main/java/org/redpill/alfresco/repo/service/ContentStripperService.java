package org.redpill.alfresco.repo.service;

import java.util.List;

import org.redpill.alfresco.repo.domain.FileLocationEntity;

public interface ContentStripperService {
	 
	public List<String> getDictionaryNodes();
	public List<String> getSurfConfigUserNodes();
	public List<String> getSurfConfigSiteNodes();
	public List<String> getPersonPreferenceValuesNodes();
	public List<String> getSiteNodes(String shortName);
	public FileLocationEntity getFileByLocalName(String localName);
}


