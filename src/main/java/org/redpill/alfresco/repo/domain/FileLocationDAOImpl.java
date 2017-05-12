package org.redpill.alfresco.repo.domain;

import org.mybatis.spring.SqlSessionTemplate;

public class FileLocationDAOImpl {

	protected SqlSessionTemplate template;
	
	public void setTemplate(SqlSessionTemplate template) {
		this.template = template;
	}
	
	public FileLocationEntity executeSearchQuery(String localName){
		FileLocationEntity result = (FileLocationEntity) template.selectOne("select_file_location_query", localName);
		return result;
	}
}
