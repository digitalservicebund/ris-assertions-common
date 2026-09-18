package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.w3c.dom.Node;

public final class XmlElementAssert<P> {

  private final Node node;
  private final String path;
  private final P parent;
  private final XmlDocumentPaths paths;

  XmlElementAssert(Node node, String path, P parent, XmlDocumentPaths paths) {
    this.node = node;
    this.path = path;
    this.parent = parent;
    this.paths = paths;
  }

  /**
   * Asserts all {@code attrs} are present on this element with the expected values.
   *
   * @param attrs attribute name→value pairs to assert
   */
  public XmlElementAssert<P> hasAttributes(Map<String, String> attrs) {
    attrs.forEach(
        (attr, value) -> {
          Node attrNode = XmlAssert.getAttrNode(node, attr, paths.namespaces());
          assertThat(attrNode)
              .as("attribute '%s' not found on element at path '%s'", attr, path)
              .isNotNull();
          assertThat(attrNode.getNodeValue())
              .as("value of attribute '%s' on element at path '%s'", attr, path)
              .isEqualTo(value);
        });
    return this;
  }

  /**
   * Asserts attribute {@code attr} is present with value {@code value}.
   *
   * @param attr attribute name, e.g. {@code "domainTerm"} or {@code "akn:refersTo"}
   * @param value expected attribute value
   */
  public XmlElementAssert<P> hasAttribute(String attr, String value) {
    return hasAttributes(Map.of(attr, value));
  }

  /**
   * Asserts attribute {@code attr} is present on this element (any value).
   *
   * @param attr attribute name, e.g. {@code "domainTerm"} or {@code "akn:refersTo"}
   */
  public XmlElementAssert<P> hasAttributeKey(String attr) {
    assertThat(XmlAssert.getAttrNode(node, attr, paths.namespaces()))
        .as("attribute '%s' not found on element at path '%s'", attr, path)
        .isNotNull();
    return this;
  }

  /**
   * Asserts attribute {@code attr} is not present on this element.
   *
   * @param attr attribute name, e.g. {@code "domainTerm"} or {@code "akn:refersTo"}
   */
  public XmlElementAssert<P> doesNotHaveAttributeKey(String attr) {
    assertThat(XmlAssert.getAttrNode(node, attr, paths.namespaces()))
        .as("attribute '%s' was unexpectedly found on element at path '%s'", attr, path)
        .isNull();
    return this;
  }

  /**
   * Asserts the text content of this element equals {@code text}.
   *
   * @param text expected text content
   */
  public XmlElementAssert<P> hasText(String text) {
    assertThat(node.getTextContent().strip())
        .as("text content of element at path '%s'", path)
        .isEqualTo(text);
    return this;
  }

  /**
   * Asserts the text content of this element does not equal {@code text}.
   *
   * @param text text content that must not be present
   */
  public XmlElementAssert<P> doesNotHaveText(String text) {
    assertThat(node.getTextContent().strip())
        .as("text content of element at path '%s'", path)
        .isNotEqualTo(text);
    return this;
  }

  /** Returns to the parent assertion for further chaining. */
  public P and() {
    return parent;
  }
}
