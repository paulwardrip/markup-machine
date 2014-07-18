package net.techphoria.markup;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MarkupMachineTag extends BodyTagSupport {
	private static final long serialVersionUID = -2815566743605507823L;
	
	private static Map<String, String> templates = new HashMap<String, String>();
	
	private String url;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int doAfterBody() throws JspException {
		BodyContent content = this.getBodyContent();
		String data = content.getString();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			Map<String,Object> result = mapper.readValue(data, HashMap.class);
			
			String html = loadTemplate();
			for (Entry<String,Object> entry : result.entrySet()) {
				html = html.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
			}
			
			content.getEnclosingWriter().write(html);
			
		} catch (Exception ex) {
			throw new JspException("Could not parse contents:\n" + data, ex);
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
