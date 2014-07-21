package net.techphoria.markup;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MarkupMachineTag extends BodyTagSupport {
	private static final long serialVersionUID = -2815566743605507823L;

	private static Map<String, String> templates = new HashMap<String, String>();

	private static final Logger LOG = Logger.getLogger(MarkupMachineTag.class.getName());
	
	private String url;

	private Map<String, Object> map;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public int doAfterBody() throws JspException {
		BodyContent content = this.getBodyContent();
		Map<String, Object> data;
		if (map != null) {
			data = map;
		} else {
			data = parseBodyJSON(content);
		}

		return replacePlaceholders(content, data);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseBodyJSON(BodyContent content) {
		try {
			String data = content.getString();
			if (data != null && data.length() > 0) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
				return mapper.readValue(data, HashMap.class);
			} 
		} catch (Exception ex) {
			LOG.log(Level.FINE, "JSON Parsing Exception", ex);
		}

		return null;
	}

	private int replacePlaceholders(BodyContent content, Map<String, Object> data) throws JspException {
		try {
			String html = loadTemplate();
			if (data != null && data.size() > 0) {
				for (Entry<String,Object> entry : data.entrySet()) {
					html = html.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
				}
			}
			content.getEnclosingWriter().write(html);

		} catch (Exception ex) {
			throw new JspException("Could not render contents.", ex);
		}

		return SKIP_BODY;
	}

	private String loadTemplate() throws IOException {
		if (!templates.containsKey(url)) {
			InputStream stream = this.pageContext.getServletContext().getResourceAsStream(url);
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			stream.close();
			return new String(bytes);
		} else {
			return templates.get(url);
		}
	}
}
