package com.peterphi.std.guice.web.rest.pagewriter;

import com.peterphi.std.guice.web.rest.util.BootstrapStaticResources;
import org.apache.commons.lang.StringEscapeUtils;

public class BootstrapPageWriter
{
	public void writeHTML(StringBuilder sb)
	{
		writePrologue(sb);
		{
			writeBodyContent(sb);
		}
		writeEpilogue(sb);
	}


	protected void writePrologue(StringBuilder sb)
	{
		sb.append("<!doctype html>\n").append("<html lang='en'><head>");
		sb.append("<title>").append(escape(getTitle())).append("</title>");
		sb.append("<meta charset='utf-8'/>");
		writeBootstrapCSS(sb);
		writeMainJS(sb);
		writeCustomHeadContent(sb);
		sb.append("</head>");
		sb.append("<body id='top'>");
		sb.append("<div class='container'>");
	}


	protected void writeEpilogue(StringBuilder sb)
	{
		sb.append("</div>");
		sb.append("</body>");
		sb.append("</html>");
	}

	protected void writeBodyContent(StringBuilder sb)
	{
	}

	protected String escape(String text)
	{
		return StringEscapeUtils.escapeHtml(text);
	}

	protected String getTitle()
	{
		return "";
	}

	protected void writeCustomHeadContent(StringBuilder sb)
	{

	}

	protected void writeBootstrapCSS(StringBuilder sb)
	{
		sb.append("<style>\n");
		BootstrapStaticResources.get().appendCSS(sb);
		sb.append("\n</style>");
	}


	protected void writeMainJS(StringBuilder sb)
	{
		sb.append("<script>\n");
		sb.append(BootstrapStaticResources.get().getJS());
		sb.append("\n</script>");
	}
}
