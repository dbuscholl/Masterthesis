package de.dbuscholl.fahrplanauskunft.Network;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.util.ArrayList;

/**
 * structural representation for all XML Documents which are used for this program
 */
public class XMLDocument {
    protected Document document;

    /**
     * Creates an XML Document from a given String (e.g. returned by TRIAS)
     * @param xmlString String representation of the XML document
     * @return instance of this class
     * @throws JDOMException
     * @throws IOException
     */
    public static XMLDocument documentFromString(String xmlString) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(xmlString);
        XMLDocument xml = new XMLDocument();
        xml.setDocument(builder.build(in));

        return xml;
    }

    /**
     * private empty constructor for internal use when building document from String
     */
    private XMLDocument() {}

    /**
     * Initializes an XML Document via resourceName for use of templates
     * @param resourceStream the resource to be used for this class
     * @throws JDOMException
     * @throws IOException
     */
    public XMLDocument(InputStream resourceStream) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        document = builder.build(new InputStreamReader(resourceStream));
    }

    /**
     * returns the first occurrence of a Node inside the XML DOM with a given name
     * @param elementName name to be filtered
     * @return Element Object of the Child Node. Null if nothing found
     */
    public Element findElementByName(String elementName) {
        Element root = document.getRootElement();
        for (Element e : root.getDescendants(new ElementFilter(elementName))) {
            return e;
        }
        return null;
    }

    /**
     * Returns a list of Elements inside an XML Document containing a specific tagname
     * @param elementName name to be filtered
     * @return ArrayList of Elements found with the given name
     */
    public ArrayList<Element> findElementsByName(String elementName) {
        ArrayList<Element> elements = new ArrayList<>();
        Element root = document.getRootElement();
        for(Element e : root.getDescendants(new ElementFilter(elementName))) {
            elements.add(e);
        }
        return elements;
    }

    /**
     * returns Document Object of the XML Document
     * @return
     */
    public Document getDocument() {
        return document;
    }

    /**
     * sets the Document for this XML for later use
     * @param document
     */
    private void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @return String representation of the whole XML Document
     */
    @Override
    public String toString() {
        return new XMLOutputter().outputString(document);
    }
}
