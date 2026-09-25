package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/** Assertion class for XML strings using namespace-prefixed XPath expressions. */
public final class XmlAssert extends AbstractCharSequenceAssert<XmlAssert, String> {

  private final XmlDocumentPaths paths;

  public XmlAssert(String actual, XmlDocumentPaths paths) {
    super(actual, XmlAssert.class);
    this.paths = paths;
  }

  // ── Infrastructure (package-private: used by sibling assert classes)

  static String toCleanedAbsoluteXPath(String path) {
    return Arrays.stream(path.split("/"))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining("/", "/", ""));
  }

  static NodeList evalXPath(String expression, String xml, Map<String, String> namespaces) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      xpath.setNamespaceContext(
          new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
              return namespaces.getOrDefault(prefix, "");
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

  static Node getAttrNode(Node node, String attr, Map<String, String> namespaces) {
    Element element = (Element) node;
    if (attr.contains(":")) {
      String[] parts = attr.split(":");
      return element.getAttributeNodeNS(namespaces.getOrDefault(parts[0], ""), parts[1]);
    }
    return element.getAttributeNode(attr);
  }

  /**
   * Not supported on assertion objects.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException(
        "equals is not supported on assertion objects - use assertions instead");
  }

  /**
   * Not supported on assertion objects.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("hashCode is not supported on assertion objects");
  }

  // ── API

  /**
   * Asserts exactly one element exists at {@code path} and returns it for further assertions.
   *
   * @param path namespace-prefixed path from root
   * @return an assertion for the matched element
   */
  public XmlElementAssert<XmlAssert> hasSingleElement(String path) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual, paths.namespaces());
    assertThat(nodelist.getLength())
        .as("expected 1 element at path '%s' but found %d", path, nodelist.getLength())
        .isEqualTo(1);
    return new XmlElementAssert<>(nodelist.item(0), path, this, paths);
  }

  /**
   * Asserts exactly {@code count} elements exist at {@code path} and returns them for further
   * assertions.
   *
   * @param path namespace-prefixed path from root
   * @param count expected number of elements
   * @return an assertion for the matched elements
   */
  public XmlElementsAssert hasElements(String path, int count) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual, paths.namespaces());
    assertThat(nodelist.getLength())
        .as("expected %d element(s) at path '%s' but found %d", count, path, nodelist.getLength())
        .isEqualTo(count);
    return new XmlElementsAssert(nodelist, path, this, paths);
  }

  /**
   * Returns a builder for asserting that at least one element at {@code path} matches the
   * constraints added via {@link XmlContainsElementAssert#hasAttribute}, {@link
   * XmlContainsElementAssert#hasAttributeKey}, and {@link XmlContainsElementAssert#hasText}.
   *
   * @param path namespace-prefixed path from root
   * @return a builder for constraining and asserting element matches
   */
  public XmlContainsElementAssert<XmlAssert> containsElement(String path) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual, paths.namespaces());
    return new XmlContainsElementAssert<>(nodelist, path, this, paths);
  }

  /**
   * Asserts {@code count} elements with the given name exist anywhere in the XML.
   *
   * @param elementName namespace-prefixed element name, e.g. {@code "ris:anwendungszeitraum"}
   * @param count expected number of matching elements
   * @return this assertion for further chaining
   */
  public XmlAssert hasElementCountAnywhere(String elementName, int count) {
    NodeList nodes = evalXPath("//" + elementName, actual, paths.namespaces());
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
   * @return this assertion for further chaining
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
   * @return a builder for constraining and asserting no element matches
   */
  public XmlContainsElementAssert<XmlAssert> containsNoElement(String path) {
    NodeList nodelist = evalXPath(toCleanedAbsoluteXPath(path), actual, paths.namespaces());
    return new XmlContainsElementAssert<>(nodelist, path, this, paths, true);
  }
}
