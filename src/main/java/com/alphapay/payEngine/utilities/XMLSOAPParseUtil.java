package com.alphapay.payEngine.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;


/**
 * This class contains some methods that used to convert {@link HashMap} objects
 * into xml messages of type {@link String} and vice versa.
 * *
 *
 * @version 1.0
 */
public class XMLSOAPParseUtil {


    private static final Logger logger = LoggerFactory.getLogger(XMLSOAPParseUtil.class);

    /**
     * This method is used to convert the {@link HashMap} message into xml message.
     *
     * @param hashMap The {@link HashMap} message to be converted.
     * @param rootTag Name of the root tag of the new xml message.
     * @return The {@link String} that represents the xml message.
     */
    @SuppressWarnings("unchecked")
    public static String fromHashMapToXML(HashMap<String, Object> hashMap, String rootTag) {
        Iterator<Entry<String, Object>> iterator = hashMap.entrySet().iterator();

        StringBuffer xmlString = new StringBuffer();
        StringBuffer xmlSubString = new StringBuffer();

        xmlString.append("<");
        xmlString.append(rootTag);

        while (iterator.hasNext()) {

            Entry<String, Object> pairs = iterator.next();
            String key = pairs.getKey();
            Object value = pairs.getValue();

            logger.debug(pairs.getKey() + " = " + pairs.getValue());

            if (value != null) {
                if (value.getClass().equals(HashMap.class))
                    xmlSubString.append(fromHashMapToXML((HashMap<String, Object>) value, key));
                else xmlString.append(" " + key + "=\"" + value + "\"");
            } else xmlString.append(" " + key + "=\"" + value + "\"");
        }
        xmlString.append(">");
        xmlString.append(xmlSubString);
        xmlString.append("</");
        xmlString.append(rootTag);
        xmlString.append(">");
        iterator.remove(); // avoids a ConcurrentModificationException
        return xmlString.toString();
    }

    /**
     * This method is used to convert an xml message of type {@link String} into
     * {@link HashMap} object.
     *
     * @param xmlString The xml message of type {@link String}.
     * @param rootTag   The root tag which contains the message that will be converted to
     *                  {@link HashMap} object.
     * @return The {@link HashMap} object produced from the xml message with the specified
     * root tag.
     * @throws Exception
     */
    public static HashMap<String, Object> xmlResponseConvertor(String xmlString, String rootTag) throws Exception {

        String thisMethod = "XMLParseUtil.xmlResponseConvertor";

        HashMap<String, Object> responseMap = new HashMap<String, Object>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            logger.debug("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            NodeList rootElementList = doc.getElementsByTagName(rootTag);
            int totalRootElements = rootElementList.getLength();

            for (int s = 0; s < totalRootElements; s++) {

                Node node = rootElementList.item(s);
                responseMap = getNodes(node, "");
            }

        } catch (Exception e) {
            throw new Exception(thisMethod + ": can't process xml string" + e);
        }

        return responseMap;

    }

    /**
     * This method is used by <code>fromXMLToHashMap</code> method to convert all xml tags,
     * including nested tags, to {@link HashMap} object.
     *
     * @param rootNode   {@link Node} object that contains sub nodes to be included into the xml message.
     * @param parentName Name of the parent tags of the current <code>rootNode</code>.
     * @return The {@link HashMap} of the current <code>rootNode</code>.
     * @throws Exception
     */
    private static HashMap<String, Object> getNodes(Node rootNode, String parentName) throws Exception {

        HashMap<String, Object> responseMap = new HashMap<String, Object>();

        if (rootNode.hasAttributes() && rootNode.getNodeType() == Node.ELEMENT_NODE) {

            NamedNodeMap nodeMap = rootNode.getAttributes();

            for (int i = 0; i < nodeMap.getLength(); i++) {
                Node attributeNode = nodeMap.item(i);

                if (!attributeNode.getNodeName().equals("xmlns") && rootNode.getNodeType() == Node.ELEMENT_NODE)
                    responseMap.put(parentName + attributeNode.getNodeName(), attributeNode.getTextContent());

            }
        }

        if (rootNode.getNodeType() == Node.ELEMENT_NODE && rootNode.hasChildNodes()) {

            NodeList nodeChilds = rootNode.getChildNodes();

            int totalChildsElements = nodeChilds.getLength();

            for (int i = 0; i < totalChildsElements; i++) {
                Node nodeChild = nodeChilds.item(i);
                if (nodeChild.getNodeName().equals("#text")) {
                    responseMap.put(parentName.substring(0, parentName.length() - 1), nodeChild.getNodeValue());
                } else {
                    String newName = parentName + nodeChild.getNodeName() + "_";
                    if (nodeChild.getAttributes().getNamedItem("id") != null)
                        newName = newName + nodeChild.getAttributes().getNamedItem("id").getNodeValue() + "_";
                    responseMap.putAll(getNodes(nodeChild, newName));
                }
            }
            if (rootNode.getNodeValue() != null) responseMap.put(rootNode.getNodeName(), rootNode.getNodeValue());
        }
        return responseMap;
    }

    public static HashMap<String, Object> fromSaopToMap(String soapString) throws Exception {
        HashMap<String, Object> responseMap = new HashMap<String, Object>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(soapString));
            Document doc = null;
            try {
                doc = builder.parse(is);
            } catch (Exception e) {
                soapString = "<root>" + soapString + "</root>";
                is = new InputSource(new StringReader(soapString));
                doc = builder.parse(is);
            }
            // normalize text representation
            doc.getDocumentElement().normalize();

            NodeList rootElementList = doc.getElementsByTagName(doc.getDocumentElement().getNodeName());
            int totalRootElements = rootElementList.getLength();

            for (int s = 0; s < totalRootElements; s++) {

                Node node = rootElementList.item(s);
                responseMap = getSoapNodes(node);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        responseMap = fromNestedMapToNormalMap(responseMap);

        return responseMap;

    }

    /**
     * This method used to extract Normal map (Map that contains just value)
     * from Nested map (Map that contains sub map).
     *
     * @param msgFieldsMap Nested map.
     * @return Normal map.
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> fromNestedMapToNormalMap(HashMap<String, Object> msgFieldsMap) {
        HashMap<String, Object> newMsgFieldsMap = new HashMap<String, Object>();
        for (Entry<String, Object> entry : msgFieldsMap.entrySet()) {
            try {
                if (entry.getValue().getClass() != LinkedHashMap.class && entry.getValue().getClass() != HashMap.class)
                    newMsgFieldsMap.put(entry.getKey(), entry.getValue());
                else
                    newMsgFieldsMap = appendMap(fromNestedMapToNormalMap((HashMap<String, Object>) entry.getValue()), newMsgFieldsMap);

            } catch (Exception e) {
                newMsgFieldsMap.put(entry.getKey(), entry.getValue());
            }
        }
        return newMsgFieldsMap;
    }

    /**
     * This method used to hold all keys and values in newMap and added in
     * oldMap.
     *
     * @param newMap small map.
     * @param oldMap big map.
     * @return oldMap contains newMap and oldMap keys.
     */
    public static HashMap<String, Object> appendMap(HashMap<String, Object> newMap, HashMap<String, Object> oldMap) {

        for (Entry<String, Object> entry : newMap.entrySet()) {
            oldMap.put(entry.getKey(), entry.getValue());

        }
        return oldMap;
    }

    private static HashMap<String, Object> getSoapNodes(Node rootNode) throws Exception {

        HashMap<String, Object> responseMap = new HashMap<String, Object>();

        if (rootNode.getNodeType() == Node.ELEMENT_NODE && rootNode.hasChildNodes()) {

            NodeList nodeChilds = rootNode.getChildNodes();

            int totalChildsElements = nodeChilds.getLength();

            for (int i = 0; i < totalChildsElements; i++) {
                Node nodeChild = nodeChilds.item(i);

                if (!nodeChild.hasChildNodes() && nodeChild.getNodeValue() != null && nodeChild.getNodeValue().trim() != null && nodeChild.getNodeValue().trim() != "" && nodeChild.getNodeValue().trim().hashCode() != 0) {
                    responseMap.put(rootNode.getNodeName(), nodeChild.getNodeValue().trim());
                } else {
                    responseMap.put(rootNode.getNodeName() + i, getSoapNodes(nodeChild));
                }
            }
        }
        return responseMap;
    }

    public static Map<String, Object> soapResponseConvertor(String strResponse) throws Exception {
        String strResponseSoap = strResponse.replace("<?xml version=\"1.0\" ?>", "<?xml version=\"1.0\"?>");
        Map<String, Object> responseMap = fromSaopToMap(strResponseSoap);
        strResponseSoap = (String) responseMap.entrySet().iterator().next().getValue();

        if (strResponseSoap.contains("&lt;?xml version='1.0'?&gt;")) {
            strResponseSoap = strResponseSoap.replace("&lt;?xml version='1.0'?&gt;", " ");
            strResponseSoap = strResponseSoap.replaceAll("&gt;", ">");
            strResponseSoap = strResponseSoap.replaceAll("&lt;", "<");
            responseMap = fromSaopToMap(strResponseSoap);
        } else {
            if ((strResponseSoap.contains("&lt;")) || (strResponseSoap.contains("&gt;"))) {
                strResponseSoap = strResponseSoap.replaceAll("&gt;", ">");
                strResponseSoap = strResponseSoap.replaceAll("&lt;", "<");
                strResponseSoap = strResponseSoap.replace("<?xml version=\"1.0\" ?>", "<?xml version=\"1.0\"?>");
                responseMap = fromSaopToMap(strResponseSoap);
            } else {
                if ((strResponseSoap.contains("<")) || (strResponseSoap.contains(">")))
                    responseMap = fromSaopToMap(strResponseSoap);
            }
        }
        return responseMap;
    }

    public static List<HashMap<String, Object>> parseXMLMapToList(String xmlString, String tag) {

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new ByteArrayInputStream(xmlString.getBytes()));

            doc.getDocumentElement().normalize();

            List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();

            NodeList resultNode = doc.getElementsByTagName(tag);

            for (int i = 0; i < resultNode.getLength(); i++) {


                HashMap<String, Object> rm=getSoapNodes(resultNode.item(i));
                resultList.add(fromNestedMapToNormalMap(rm));
            }

            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static HashMap<String,Object> listFieldFromXmlMap(String sub,String responseString)
    {
         HashMap<String,Object> map = new HashMap<>();
        if(responseString.contains("<" + sub + ">")) {
            map.put(sub, XMLSOAPParseUtil.parseXMLMapToList(responseString, sub)) ;
        }
        else {
            map.put(sub, null);
        }
        return map;

    }
}

