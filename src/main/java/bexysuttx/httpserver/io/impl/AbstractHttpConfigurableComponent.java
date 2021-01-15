package bexysuttx.httpserver.io.impl;

import bexysuttx.httpserver.io.config.HttpServerConfig;

class AbstractHttpConfigurableComponent {
  final HttpServerConfig httpServerConfig;
 
 AbstractHttpConfigurableComponent(HttpServerConfig httpServerConfig) {
	super();
	this.httpServerConfig = httpServerConfig;
}
}
