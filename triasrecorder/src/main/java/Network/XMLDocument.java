package Network;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.util.ArrayList;

public class XMLDocument {
    protected Document document;

    public static XMLDocument documentFromString(String xmlString) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(xmlString);
        XMLDocument xml = new XMLDocument();
        xml.setDocument(builder.build(in));

        return xml;
    }

    private XMLDocument() {}

    public XMLDocument(String resourceName) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        document = builder.build(new InputStreamReader(this.getClass().getResource(resourceName).openStream()));
    }

    public Element findElementByName(String elementName) {
        Element root = document.getRootElement();
        for (Element e : root.getDescendants(new ElementFilter(elementName))) {
            return e;
        }
        return null;
    }

    public ArrayList<Element> findElementsByName(String elementName) {
        ArrayList<Element> elements = new ArrayList<>();
        Element root = document.getRootElement();
        for(Element e : root.getDescendants(new ElementFilter(elementName))) {
            elements.add(e);
        }
        return elements;
    }

    public Document getDocument() {
        return document;
    }

    private void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return new XMLOutputter().outputString(document);
    }
}
