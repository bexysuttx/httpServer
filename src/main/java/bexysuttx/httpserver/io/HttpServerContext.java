package bexysuttx.httpserver.io;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

public interface HttpServerContext {

	ServerInfo getServerInfo();

	Collection<String> getSupportedRequestMethods();

	Properties getSupportedResponseStatuses();

	DataSource getDataSource();

	Path getPathRoot();

	String getContentType(String extention);
	
	HtmlTemplateManager getHtmlTemplateManager();
	
	Integer getExpriresDaysForResource(String extention);
	

}
