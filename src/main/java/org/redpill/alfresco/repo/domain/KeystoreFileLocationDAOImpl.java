package org.redpill.alfresco.repo.domain;

import org.mybatis.spring.SqlSessionTemplate;

public class KeystoreFileLocationDAOImpl {

	protected SqlSessionTemplate template;
	
	public void setTemplate(SqlSessionTemplate template) {
		this.template = template;
	}
	
	public KeystoreFileLocationEntity executeSearchQuery(){
		KeystoreFileLocationEntity result = (KeystoreFileLocationEntity) template.selectOne("select_keystore_location_query");
		return result;
	}
}
