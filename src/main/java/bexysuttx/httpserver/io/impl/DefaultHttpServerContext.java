package bexysuttx.httpserver.io.impl;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

import bexysuttx.httpserver.io.Constants;
import bexysuttx.httpserver.io.HtmlTemplateManager;
import bexysuttx.httpserver.io.HttpServerContext;
import bexysuttx.httpserver.io.ServerInfo;
import bexysuttx.httpserver.io.config.HttpServerConfig;
import bexysuttx.httpserver.io.exception.HttpServerConfigException;
 
class DefaultHttpServerContext extends AbstractHttpConfigurableComponent implements HttpServerContext {

	DefaultHttpServerContext(HttpServerConfig httpServerConfig) {
		super(httpServerConfig);
	}

	private DefaultHttpServerConfig getHttpServerConfig() {
		return (DefaultHttpServerConfig) httpServerConfig;
	}

	@Override
	public ServerInfo getServerInfo() {
		return getHttpServerConfig().getServerInfo();
	}

	@Override
	public Collection<String> getSupportedRequestMethods() {
		return Constants.ALLOWED_METHOD;
	}

	@Override
	public Properties getSupportedResponseStatuses() {
		Properties prop = new Properties();
		prop.putAll(getHttpServerConfig().getStatusesProperties());
		return prop;
	}

	@Override
	public DataSource getDataSource() {
		if (getHttpServerConfig().getDataSource() != null) {
			return getHttpServerConfig().getDataSource();
		} else {
			throw new HttpServerConfigException("Datasource is not configured for this context");
		}
	}

	@Override
	public Path getPathRoot() {
		return getHttpServerConfig().getRootPath();
	}

	@Override
	public String getContentType(String extention) {
		String res = getHttpServerConfig().getMimeTypesProperties().getProperty(extention);
		return res != null ? res : "text/plain";
	}

	@Override
	public HtmlTemplateManager getHtmlTemplateManager() {
		return getHttpServerConfig().getHtmlTemplateManager();
	}

	@Override
	public Integer getExpriresDaysForResource(String extention) {
		if (getHttpServerConfig().getStaticExpiresExtensions().contains(extention)) {
			return getHttpServerConfig().getStaticExpiresDays();
		}
		return null;
	}

}
