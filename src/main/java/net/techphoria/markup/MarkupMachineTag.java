package net.techphoria.markup;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
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
	
	private String json;

	private Object obj;
	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setData(Object data) {
		this.obj = data;
	}

	public void setJson(String json) {
		this.json = json;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int doAfterBody() throws JspException {
		BodyContent content = this.getBodyContent();
		Map<Object, Object> data;
		
		if (obj != null) {
			if (obj instanceof Map) { 
				data = (Map <Object, Object>) obj;
			} else {
				data = extract(obj);
			}
			
		} else if (json != null) {
			data = parseJSON(json);
			
		} else {
			data = parseJSON(content.getString());
		}

		return replacePlaceholders(content, data);
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> extract(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		if (mapper.canSerialize(object.getClass())) {
			StringWriter writer = new StringWriter();
			try {
				mapper.writeValue(writer, object);
				String json = writer.getBuffer().toString();
				writer.close();
				return parseJSON(json);
				
			} catch (Exception ex) {
				LOG.log(Level.WARNING, "Could Not Serialize an Object", ex);
			}
		}
		
		return Collections.EMPTY_MAP;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Object, Object> parseJSON(String data) {
		try {
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

	private int replacePlaceholders(BodyContent content, Map<?, ?> data) throws JspException {
		try {
			String html = loadTemplate();
			if (data != null && data.size() > 0) {
				for (Entry<?,?> entry : data.entrySet()) {
					html = html.replace("{" + String.valueOf(entry.getKey()) + "}", String.valueOf(entry.getValue()));
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
