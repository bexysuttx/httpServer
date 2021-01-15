package bexysuttx.httpserver.io;

import java.util.Map;

public interface HtmlTemplateManager {
	
	String processTemplate(String template, Map<String, Object> args);
	

}
