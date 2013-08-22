package com.mediasmiths.std.util;

import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.EntityRef;
import org.jdom2.JDOMConstants;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.support.AbstractDOMOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.Walker;
import org.jdom2.util.NamespaceStack;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copy of AbstractDOMOutputProcessor changed to avoid https://github.com/hunterhacker/jdom/issues/115
 */
final class FixedDOMOutputProcessor extends AbstractDOMOutputProcessor
{
	public static final AbstractDOMOutputProcessor INSTANCE = new FixedDOMOutputProcessor();


	/**
	 * This will handle adding any <code>{@link Namespace}</code> attributes to
	 * the DOM tree.
	 *
	 * @param ns
	 * 		<code>Namespace</code> to add definition of
	 */
	private static String getXmlnsTagFor(Namespace ns)
	{
		String attrName = "xmlns";
		if (!ns.getPrefix().equals(""))
		{
			attrName += ":";
			attrName += ns.getPrefix();
		}
		return attrName;
	}

	/* *******************************************
	 * DOMOutputProcessor implementation.
	 * *******************************************
	 */


	@Override
	public org.w3c.dom.Document process(org.w3c.dom.Document basedoc, Format format, Document doc)
	{
		return printDocument(new FormatStack(format), new NamespaceStack(), basedoc, doc);
	}


	@Override
	public org.w3c.dom.Element process(org.w3c.dom.Document basedoc, Format format, Element element)
	{
		return printElement(new FormatStack(format), new NamespaceStack(), basedoc, element);
	}


	@Override
	public List<org.w3c.dom.Node> process(org.w3c.dom.Document basedoc, Format format, List<? extends Content> list)
	{
		List<org.w3c.dom.Node> ret = new ArrayList<Node>(list.size());
		FormatStack fstack = new FormatStack(format);
		NamespaceStack nstack = new NamespaceStack();
		for (Content c : list)
		{
			fstack.push();
			try
			{
				org.w3c.dom.Node node = helperContentDispatcher(fstack, nstack, basedoc, c);
				if (node != null)
				{
					ret.add(node);
				}
			}
			finally
			{
				fstack.pop();
			}
		}
		return ret;
	}


	@Override
	public org.w3c.dom.CDATASection process(org.w3c.dom.Document basedoc, Format format, CDATA cdata)
	{
		final List<CDATA> list = Collections.singletonList(cdata);
		final FormatStack fstack = new FormatStack(format);
		final Walker walker = buildWalker(fstack, list, false);
		if (walker.hasNext())
		{
			final Content c = walker.next();
			if (c == null)
			{
				return printCDATA(fstack, basedoc, new CDATA(walker.text()));
			}
			if (c.getCType() == Content.CType.CDATA)
			{
				return printCDATA(fstack, basedoc, (CDATA) c);
			}
		}
		// return an empty string if nothing happened.
		return null;
	}


	@Override
	public org.w3c.dom.Text process(org.w3c.dom.Document basedoc, Format format, Text text)
	{
		final List<Text> list = Collections.singletonList(text);
		final FormatStack fstack = new FormatStack(format);
		final Walker walker = buildWalker(fstack, list, false);
		if (walker.hasNext())
		{
			final Content c = walker.next();
			if (c == null)
			{
				return printText(fstack, basedoc, new Text(walker.text()));
			}
			if (c.getCType() == Content.CType.Text)
			{
				return printText(fstack, basedoc, (Text) c);
			}
		}
		// return an empty string if nothing happened.
		return null;
	}


	@Override
	public org.w3c.dom.Comment process(org.w3c.dom.Document basedoc, Format format, Comment comment)
	{
		return printComment(new FormatStack(format), basedoc, comment);
	}


	@Override
	public org.w3c.dom.ProcessingInstruction process(org.w3c.dom.Document basedoc, Format format, ProcessingInstruction pi)
	{
		return printProcessingInstruction(new FormatStack(format), basedoc, pi);
	}


	@Override
	public org.w3c.dom.EntityReference process(org.w3c.dom.Document basedoc, Format format, EntityRef entity)
	{
		return printEntityRef(new FormatStack(format), basedoc, entity);
	}


	@Override
	public org.w3c.dom.Attr process(org.w3c.dom.Document basedoc, Format format, Attribute attribute)
	{
		return printAttribute(new FormatStack(format), basedoc, attribute);
	}

	/* *******************************************
	 * Support methods for output. Should all be protected. All content-type
	 * print methods have a FormatStack. Only printContent is responsible for
	 * outputting appropriate indenting and newlines, which are easily available
	 * using the FormatStack.getLevelIndent() and FormatStack.getLevelEOL().
	 * *******************************************
	 */


	/**
	 * This will handle printing of a {@link Document}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param nstack
	 * 		the NamespaceStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param doc
	 * 		<code>Document</code> to write.
	 *
	 * @return The input JDOM document converted to a DOM document.
	 */
	protected org.w3c.dom.Document printDocument(final FormatStack fstack,
	                                             final NamespaceStack nstack,
	                                             final org.w3c.dom.Document basedoc,
	                                             final Document doc)
	{

		if (!fstack.isOmitDeclaration())
		{
			basedoc.setXmlVersion("1.0");
		}

		final int sz = doc.getContentSize();

		if (sz > 0)
		{
			for (int i = 0; i < sz; i++)
			{
				final Content c = doc.getContent(i);
				org.w3c.dom.Node n = null;
				switch (c.getCType())
				{
					case Comment:
						n = printComment(fstack, basedoc, (Comment) c);
						break;
					case DocType:
						// cannot simply add a DocType to a DOM object
						// it is added when the DOM Document is created.
						// leave n as null
						break;
					case Element:
						n = printElement(fstack, nstack, basedoc, (Element) c);
						break;
					case ProcessingInstruction:
						n = printProcessingInstruction(fstack, basedoc, (ProcessingInstruction) c);
						break;
					default:
						// do nothing.
				}
				if (n != null)
				{
					basedoc.appendChild(n);
				}
			}
		}

		return basedoc;
	}


	/**
	 * This will handle printing of a {@link ProcessingInstruction}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param pi
	 * 		<code>ProcessingInstruction</code> to write.
	 *
	 * @return The input JDOM ProcessingInstruction converted to a DOM
	 *         ProcessingInstruction.
	 */
	protected org.w3c.dom.ProcessingInstruction printProcessingInstruction(final FormatStack fstack,
	                                                                       final org.w3c.dom.Document basedoc,
	                                                                       final ProcessingInstruction pi)
	{
		String target = pi.getTarget();
		String rawData = pi.getData();
		if (rawData == null || rawData.trim().length() == 0)
		{
			rawData = "";
		}
		return basedoc.createProcessingInstruction(target, rawData);
	}


	/**
	 * This will handle printing of a {@link Comment}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param comment
	 * 		<code>Comment</code> to write.
	 *
	 * @return The input JDOM Comment converted to a DOM Comment
	 */
	protected org.w3c.dom.Comment printComment(final FormatStack fstack,
	                                           final org.w3c.dom.Document basedoc,
	                                           final Comment comment)
	{
		return basedoc.createComment(comment.getText());
	}


	/**
	 * This will handle printing of an {@link EntityRef}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param entity
	 * 		<code>EntotyRef</code> to write.
	 *
	 * @return The input JDOM EntityRef converted to a DOM EntityReference
	 */
	protected org.w3c.dom.EntityReference printEntityRef(final FormatStack fstack,
	                                                     final org.w3c.dom.Document basedoc,
	                                                     final EntityRef entity)
	{
		return basedoc.createEntityReference(entity.getName());
	}


	/**
	 * This will handle printing of a {@link CDATA}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param cdata
	 * 		<code>CDATA</code> to write.
	 *
	 * @return The input JDOM CDATA converted to a DOM CDATASection
	 */
	protected org.w3c.dom.CDATASection printCDATA(final FormatStack fstack, final org.w3c.dom.Document basedoc, final CDATA cdata)
	{
		// CDATAs are treated like text, not indented/newline content.
		return basedoc.createCDATASection(cdata.getText());
	}


	/**
	 * This will handle printing of a {@link Text}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param text
	 * 		<code>Text</code> to write.
	 *
	 * @return The input JDOM Text converted to a DOM Text
	 */
	protected org.w3c.dom.Text printText(final FormatStack fstack, final org.w3c.dom.Document basedoc, final Text text)
	{
		return basedoc.createTextNode(text.getText());
	}


	/**
	 * This will handle printing of a {@link Attribute}.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param attribute
	 * 		<code>Attribute</code> to write.
	 *
	 * @return The input JDOM Attribute converted to a DOM Attr
	 */
	protected org.w3c.dom.Attr printAttribute(final FormatStack fstack,
	                                          final org.w3c.dom.Document basedoc,
	                                          final Attribute attribute)
	{
		if (!attribute.isSpecified() && fstack.isSpecifiedAttributesOnly())
		{
			return null;
		}
		org.w3c.dom.Attr attr = basedoc.createAttributeNS(attribute.getNamespaceURI(), attribute.getQualifiedName());
		attr.setValue(attribute.getValue());
		return attr;
	}


	/**
	 * This will handle printing of an {@link Element}.
	 * <p/>
	 * This method arranges for outputting the Element infrastructure including
	 * Namespace Declarations and Attributes.
	 * <p/>
	 * The actual formatting of the content is managed by the Walker created for
	 * the Element's content.
	 * <p/>
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param nstack
	 * 		the NamespaceStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param element
	 * 		<code>Element</code> to write.
	 *
	 * @return The input JDOM Element converted to a DOM Element
	 */
	protected org.w3c.dom.Element printElement(final FormatStack fstack,
	                                           final NamespaceStack nstack,
	                                           final org.w3c.dom.Document basedoc,
	                                           final Element element)
	{

		nstack.push(element);
		try
		{

			Format.TextMode textmode = fstack.getTextMode();

			// Check for xml:space and adjust format settings
			final String space = element.getAttributeValue("space", Namespace.XML_NAMESPACE);

			if ("default".equals(space))
			{
				textmode = fstack.getDefaultMode();
			}
			else if ("preserve".equals(space))
			{
				textmode = Format.TextMode.PRESERVE;
			}

			org.w3c.dom.Element ret = basedoc.createElementNS(element.getNamespaceURI(), element.getQualifiedName());

			for (Namespace ns : nstack.addedForward())
			{
				if (ns == Namespace.XML_NAMESPACE)
				{
					continue;
				}
				ret.setAttributeNS(JDOMConstants.NS_URI_XMLNS, getXmlnsTagFor(ns), ns.getURI());
			}

			if (element.hasAttributes())
			{
				for (Attribute att : element.getAttributes())
				{
					final org.w3c.dom.Attr a = printAttribute(fstack, basedoc, att);
					if (a != null)
					{
						ret.setAttributeNodeNS(a);
					}
				}
			}

			final List<Content> content = element.getContent();

			if (!content.isEmpty())
			{
				fstack.push();
				try
				{
					fstack.setTextMode(textmode);
					Walker walker = buildWalker(fstack, content, false);

					if (!walker.isAllText() && fstack.getPadBetween() != null)
					{
						// we need to newline/indent
						final org.w3c.dom.Text n = basedoc.createTextNode(fstack.getPadBetween());
						ret.appendChild(n);
					}

					printContent(fstack, nstack, basedoc, ret, walker);

					if (!walker.isAllText() && fstack.getPadLast() != null)
					{
						// we need to newline/indent
						final org.w3c.dom.Text n = basedoc.createTextNode(fstack.getPadLast());
						ret.appendChild(n);
					}
				}
				finally
				{
					fstack.pop();
				}
			}

			return ret;
		}
		finally
		{
			nstack.pop();
		}
	}


	/**
	 * This will handle printing of a List of {@link Content}. Uses the Walker
	 * to ensure formatting.
	 *
	 * @param fstack
	 * 		the FormatStack
	 * @param nstack
	 * 		the NamespaceStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param target
	 * 		the DOM node this content should be appended to.
	 * @param walker
	 * 		<code>List</code> of <code>Content</code> to write.
	 */
	protected void printContent(final FormatStack fstack,
	                            final NamespaceStack nstack,
	                            final org.w3c.dom.Document basedoc,
	                            final org.w3c.dom.Node target,
	                            final Walker walker)
	{

		while (walker.hasNext())
		{
			final Content c = walker.next();
			org.w3c.dom.Node n = null;
			if (c == null)
			{
				// Formatted Text or CDATA
				final String text = walker.text();
				if (walker.isCDATA())
				{
					n = printCDATA(fstack, basedoc, new CDATA(text));
				}
				else
				{
					n = printText(fstack, basedoc, new Text(text));
				}
			}
			else
			{
				n = helperContentDispatcher(fstack, nstack, basedoc, c);
			}
			if (n != null)
			{
				target.appendChild(n);
			}
		}
	}


	/**
	 * This method contains code which is reused in a number of places. It
	 * simply determines what content is passed in, and dispatches it to the
	 * correct print* method.
	 *
	 * @param fstack
	 * 		The current FormatStack
	 * @param nstack
	 * 		the NamespaceStack
	 * @param basedoc
	 * 		The org.w3c.dom.Document for creating DOM Nodes
	 * @param content
	 * 		The content to dispatch
	 *
	 * @return the input JDOM Content converted to a DOM Node.
	 */
	protected org.w3c.dom.Node helperContentDispatcher(final FormatStack fstack,
	                                                   final NamespaceStack nstack,
	                                                   final org.w3c.dom.Document basedoc,
	                                                   final Content content)
	{
		switch (content.getCType())
		{
			case CDATA:
				return printCDATA(fstack, basedoc, (CDATA) content);
			case Comment:
				return printComment(fstack, basedoc, (Comment) content);
			case Element:
				return printElement(fstack, nstack, basedoc, (Element) content);
			case EntityRef:
				return printEntityRef(fstack, basedoc, (EntityRef) content);
			case ProcessingInstruction:
				return printProcessingInstruction(fstack, basedoc, (ProcessingInstruction) content);
			case Text:
				return printText(fstack, basedoc, (Text) content);
			case DocType:
				return null;
			default:
				throw new IllegalStateException("Unexpected Content " + content.getCType());
		}
	}
}
