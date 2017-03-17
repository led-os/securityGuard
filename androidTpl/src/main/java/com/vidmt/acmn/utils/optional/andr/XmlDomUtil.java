package com.vidmt.acmn.utils.optional.andr;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.vidmt.acmn.abs.CodeException;

public final class XmlDomUtil {
	public static Document getXmlDocFromFile(String path) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return doc;
	}

	public static Document getXmlDocFromXml(String xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return doc;
	}
}
