package de.bund.digitalservice.ris.adm.bzst.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public final class XmlAssert extends AbstractCharSequenceAssert<XmlAssert, String> {

  public XmlAssert(String actual) {
    super(actual, XmlAssert.class);
  }

  public static XmlAssert assertThatXml(String actual) {
    return new XmlAssert(actual);
  }

  // ── Constants

  public static final Map<String, String> XML_NAMESPACES =
      Map.of(
          "akn", "http://docs.oasis-open.org/legaldocml/ns/akn/3.0",
          "ris", "http://ldml.neuris.de/adm/bzst/meta/");

  public static final String AKN_META_X_PATH = "akn:akomaNtoso/akn:doc/akn:meta";
  public static final String AKN_IDENTIFICATION_X_PATH = AKN_META_X_PATH + "/akn:identification";
  public static final String AKN_CLASSIFICATION_X_PATH = AKN_META_X_PATH + "/akn:classification";
  public static final String RIS_META_X_PATH = AKN_META_X_PATH + "/akn:proprietary/ris:meta";
  public static final String RIS_REFERENZ_RECHTSPRECHUNG_X_PATH =
      AKN_META_X_PATH
          + "/akn:analysis/akn:otherReferences/akn:implicitReference/ris:referenzRechtsprechung";

  // ── Infrastructure (package-private: used by sibling assert classes)

  static String toCleanedAbsoluteXPath(String path) {
    return Arrays.stream(path.split("/"))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining("/", "/", ""));
  }

  static NodeList evalXPath(String expression, String xml) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      xpath.setNamespaceContext(
          new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
              return XML_NAMESPACES.getOrDefault(prefix, "");
            }

            public String getPrefix(String ns) {
              return null;
            }

            public Iterator<String> getPrefixes(String ns) {
              return null;
            }
          });
      return (NodeList)
          xpath.evaluate(
              expression, new InputSource(new StringReader(xml)), XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new RuntimeException("Invalid XPath expression: " + expression, e);
    }
  }

  static Node getAttrNode(Node node, String attr) {
    Element element = (Element) node;
    if (attr.contains(":")) {
      String[] parts = attr.split(":");
      return element.getAttributeNodeNS(XML_NAMESPACES.getOrDefault(parts[0], ""), parts[1]);
    }
    return element.getAttributeNode(attr);
  }

  // ── API

  /**
   * Asserts exactly one element exists at {@code path} and returns it for further assertions.
   *
   * @param path namespace-prefixed path from root
   */
  public XmlElementAssert<XmlAssert> hasSingleElement(String path) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual);
    assertThat(nodelist.getLength())
        .as("expected 1 element at path '%s' but found %d", path, nodelist.getLength())
        .isEqualTo(1);
    return new XmlElementAssert<>(nodelist.item(0), path, this);
  }

  /**
   * Asserts exactly {@code count} elements exist at {@code path} and returns them for further
   * assertions.
   *
   * @param path namespace-prefixed path from root
   * @param count expected number of elements
   */
  public XmlElementsAssert hasElements(String path, int count) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual);
    assertThat(nodelist.getLength())
        .as("expected %d element(s) at path '%s' but found %d", count, path, nodelist.getLength())
        .isEqualTo(count);
    return new XmlElementsAssert(nodelist, path, this);
  }

  /**
   * Returns a builder for asserting that at least one element at {@code path} matches the
   * constraints added via {@link XmlContainsElementAssert#hasAttribute}, {@link
   * XmlContainsElementAssert#hasAttributeKey}, and {@link XmlContainsElementAssert#hasText}.
   *
   * @param path namespace-prefixed path from root
   */
  public XmlContainsElementAssert<XmlAssert> containsElement(String path) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual);
    return new XmlContainsElementAssert<>(nodelist, path, this);
  }

  /**
   * Asserts {@code count} elements with the given name exist anywhere in the XML.
   *
   * @param elementName namespace-prefixed element name, e.g. {@code "ris:anwendungszeitraum"}
   * @param count expected number of matching elements
   */
  public XmlAssert hasElementCountAnywhere(String elementName, int count) {
    NodeList nodes = evalXPath("//" + elementName, actual);
    assertThat(nodes.getLength())
        .as(
            "expected %d element(s) named <%s> anywhere in document but found %d",
            count, elementName, nodes.getLength())
        .isEqualTo(count);
    return this;
  }

  /**
   * Asserts no element with the given name exists anywhere in the XML.
   *
   * @param elementName namespace-prefixed element name, e.g. {@code "ris:anwendungszeitraum"}
   */
  public XmlAssert hasNoElementAnywhere(String elementName) {
    return hasElementCountAnywhere(elementName, 0);
  }

  /**
   * Returns a builder that asserts no element at {@code path} matches the constraints added via
   * {@link XmlContainsElementAssert#hasAttribute}, {@link
   * XmlContainsElementAssert#hasAttributeKey}, and {@link XmlContainsElementAssert#hasText}.
   * Inverse of {@link #containsElement(String)}.
   *
   * @param path namespace-prefixed path from root
   */
  public XmlContainsElementAssert<XmlAssert> containsNoElement(String path) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual);
    return new XmlContainsElementAssert<>(nodelist, path, this, true);
  }
}
