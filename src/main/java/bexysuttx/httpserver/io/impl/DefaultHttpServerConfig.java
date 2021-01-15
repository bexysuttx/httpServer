package bexysuttx.httpserver.io.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bexysuttx.httpserver.io.HandlerConfig;
import bexysuttx.httpserver.io.HtmlTemplateManager;
import bexysuttx.httpserver.io.HttpHandler;
import bexysuttx.httpserver.io.HttpServerContext;
import bexysuttx.httpserver.io.ServerInfo;
import bexysuttx.httpserver.io.config.HttpClientSocketHandler;
import bexysuttx.httpserver.io.config.HttpRequestParser;
import bexysuttx.httpserver.io.config.HttpResponseBilder;
import bexysuttx.httpserver.io.config.HttpResponseWritter;
import bexysuttx.httpserver.io.config.HttpServerConfig;
import bexysuttx.httpserver.io.config.HttpServerDispatcher;
import bexysuttx.httpserver.io.exception.HttpServerConfigException;

class DefaultHttpServerConfig implements HttpServerConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpServerConfig.class);

	private final Properties serverProperties = new Properties();
	private final Properties statusesProperties = new Properties();
	private final Properties mimeTypesProperties = new Properties();
	private final BasicDataSource dataSource;
	private final Path rootPath;
	private final Map<String, HttpHandler> httpHandlers;
	private final HttpServerContext httpServerContext;
	private final HttpResponseBilder httpResponseBuilder;
	private final HttpRequestParser httpRequestParser;
	private final HttpHandler defaultHttpHandler;
	private final HttpResponseWritter httpResponseWritter;
	private final HttpServerDispatcher httpServerDispatcher;
	private final ThreadFactory workerThreadFactory;
	private final HtmlTemplateManager htmlTemplateManager;
	private final ServerInfo serverInfo;
	private final List<String> staticExpriresExtensions;
	private final int staticExpiresDays;

	@SuppressWarnings("unchecked")
	public DefaultHttpServerConfig(HandlerConfig handlerConfig, Properties overrideProperties) throws Exception {
		super();
		loadAllProperties(overrideProperties);
		this.httpHandlers = handlerConfig != null ? handlerConfig.toMap()
				: (Map<String, HttpHandler>) Collections.EMPTY_MAP;
		this.rootPath = createRootPath();
		this.dataSource = createDataSource();
		this.serverInfo = createServerInfo();
		this.staticExpriresExtensions = Arrays
				.asList(this.serverProperties.getProperty("webapp.static.expires.extensions").split(","));
		this.staticExpiresDays = Integer.parseInt(this.serverProperties.getProperty("webapp.static.expires.days"));

		// Create default implementations
		this.httpServerContext = new DefaultHttpServerContext(this);
		this.httpRequestParser = new DefaultHttpRequestParser();
		this.httpResponseBuilder = new DefaultHttpResponseBuilder(this);
		this.defaultHttpHandler = new DefaultHttpHandler();
		this.httpResponseWritter = new DefaultHttpResponseWritter(this);
		this.httpServerDispatcher = new DefaultHttpRequestDispatcher(defaultHttpHandler, this.httpHandlers);
		this.workerThreadFactory = new DefaultThreadFactory();
		this.htmlTemplateManager = new DefaultHtmlTemplateManager();

	}

	private void loadAllProperties(Properties overrideProperties) throws IOException {
		ClassLoader classLoader = DefaultHttpServerConfig.class.getClassLoader();
		loadProperties(this.serverProperties, classLoader, "server.properties");
		loadProperties(this.statusesProperties, classLoader, "statuses.properties");
		loadProperties(this.mimeTypesProperties, classLoader, "mime-types.properties");
		if (overrideProperties != null) {
			LOGGER.info("Overrides default server properties");
			this.serverProperties.putAll(overrideProperties);
		}
		logServerProperties();
	}

	private void logServerProperties() {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder res = new StringBuilder("Current server properties if:\n");
			for (Map.Entry<Object, Object> entry : this.serverProperties.entrySet()) {
				res.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
			}
			LOGGER.debug(res.toString());
		}
	}

	private void loadProperties(Properties serverProperties2, ClassLoader classLoader, String string)
			throws IOException {
		try (InputStream in = classLoader.getResourceAsStream(string)) {
			if (in != null) {
				serverProperties2.load(in);
				LOGGER.debug("Sucessful loaded properties from classpath resource: {}", string);
			} else {
				throw new HttpServerConfigException("Classpath resource not found: " + string);
			}
		} catch (IOException e) {
			throw new HttpServerConfigException("Can't loaded properties from resource: " + string);
		}
	}

	private Path createRootPath() {
		Path path = Paths
				.get(new File(this.serverProperties.getProperty("wepapp.static.dir.root")).getAbsoluteFile().toURI());
		if (!Files.exists(path)) {
			throw new HttpServerConfigException("Root path not found: " + path);
		}
		if (!Files.isDirectory(path)) {
			throw new HttpServerConfigException("Root path is not directory: " + path);

		}
		return path;
	}

	private BasicDataSource createDataSource() {
		BasicDataSource ds = null;
		if (Boolean.parseBoolean(serverProperties.getProperty("db.datasource.enabled"))) {
			ds = new BasicDataSource();
			ds.setDefaultAutoCommit(false);
			ds.setRollbackOnReturn(true);
			ds.setDriverClassName(Objects.requireNonNull(this.serverProperties.getProperty("db.datasource.driver")));
			ds.setUrl(Objects.requireNonNull(this.serverProperties.getProperty("db.datasource.url")));
			ds.setUsername(Objects.requireNonNull(this.serverProperties.getProperty("db.datasource.username")));
			ds.setPassword(Objects.requireNonNull(this.serverProperties.getProperty("db.datasource.password")));
			ds.setInitialSize(Integer.parseInt(
					Objects.requireNonNull(this.serverProperties.getProperty("db.datasource.pool.initSize"))));
			ds.setMaxTotal(Integer
					.parseInt(Objects.requireNonNull(this.serverProperties.getProperty("db.datasource.pool.maxSize"))));
			LOGGER.info("Data source is enable. JDBC URL is {}", ds.getUrl());
		} else {
			LOGGER.info("Data Source is disabled");
		}

		return ds;
	}

	private ServerInfo createServerInfo() {
		ServerInfo si = new ServerInfo(serverProperties.getProperty("server.name"),
				Integer.parseInt(serverProperties.getProperty("server.port")),
				Integer.parseInt(serverProperties.getProperty("server.thread.count")));
		if (si.getThreadCount() < 0) {
			throw new HttpServerConfigException("server.thread.count should be >= 0");
		}
		return si;
	}

	@Override
	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	@Override
	public String getStatusMessage(int statusCode) {
		String message = statusesProperties.getProperty(String.valueOf(statusCode));
		return message != null ? message : statusesProperties.getProperty("500");
	}

	@Override
	public HttpResponseBilder getHttpResponseBilder() {
		return httpResponseBuilder;
	}

	@Override
	public HttpResponseWritter getHttpResponseWritter() {
		return httpResponseWritter;
	}

	@Override
	public HttpRequestParser getHttpRequestParser() {
		return httpRequestParser;
	}

	@Override
	public HttpServerDispatcher getHttpServerDispatcher() {
		return httpServerDispatcher;
	}

	@Override
	public HttpServerContext getHttpServerContext() {
		return httpServerContext;
	}

	@Override
	public ThreadFactory getWorkerThreadFactory() {
		return workerThreadFactory;
	}

	@Override
	public HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket) {
		return new DefaultHttpClientSocketHandler(clientSocket, this);
	}

	protected Properties getServerProperties() {
		return serverProperties;
	}

	protected Properties getStatusesProperties() {
		return statusesProperties;
	}

	protected Properties getMimeTypesProperties() {
		return mimeTypesProperties;
	}

	protected BasicDataSource getDataSource() {
		return dataSource;
	}

	protected Path getRootPath() {
		return rootPath;
	}

	protected HtmlTemplateManager getHtmlTemplateManager() {
		return htmlTemplateManager;
	}

	protected HttpHandler getDefaultHttpHandler() {
		return defaultHttpHandler;
	}
	
	protected Map<String, HttpHandler> getHttpHandlers() {
		return httpHandlers;
	}
	
	protected List<String> getStaticExpiresExtensions() {
		return staticExpriresExtensions;
	}

	protected int getStaticExpiresDays() {
		return staticExpiresDays;
	}

	public void close() {
		if (dataSource != null) {
			try {
				dataSource.close();
			} catch (SQLException e) {
				LOGGER.error("Close datasource failed: " + e.getMessage(), e);
			}
		}
		LOGGER.info("DefaultHttpServer closed");
	}

}
