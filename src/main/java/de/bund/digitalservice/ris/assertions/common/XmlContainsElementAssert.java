package de.bund.digitalservice.ris.adm.bzst.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlContainsElementAssert<P> {

  private final NodeList nodelist;
  private final String path;
  private final P parent;
  private final boolean negate;
  private Map<String, String> attrsFilter = null;
  private Set<String> attrKeysFilter = null;
  private String textFilter = null;

  XmlContainsElementAssert(NodeList nodelist, String path, P parent) {
    this(nodelist, path, parent, false);
  }

  XmlContainsElementAssert(NodeList nodelist, String path, P parent, boolean negate) {
    this.nodelist = nodelist;
    this.path = path;
    this.parent = parent;
    this.negate = negate;
  }

  /**
   * Constrains elements to those with all {@code attrs} at the given values, then fires the
   * assertion (≥1 match for {@code containsElement}, 0 matches for {@code containsNoElement}).
   * Combined with any prior constraints.
   *
   * @param attrs attribute name→value pairs to match
   */
  public XmlContainsElementAssert<P> hasAttributes(Map<String, String> attrs) {
    if (this.attrsFilter == null) this.attrsFilter = new HashMap<>();
    this.attrsFilter.putAll(attrs);
    assertConstraint();
    return this;
  }

  /**
   * Constrains elements to those with attribute {@code attr} equal to {@code value}, then fires the
   * assertion. Combined with any prior constraints.
   *
   * @param attr attribute name, e.g. {@code "domainTerm"} or {@code "akn:refersTo"}
   * @param value expected attribute value
   */
  public XmlContainsElementAssert<P> hasAttribute(String attr, String value) {
    return hasAttributes(Map.of(attr, value));
  }

  /**
   * Constrains elements to those that have attribute {@code attr} (any value), then fires the
   * assertion. Combined with any prior constraints.
   *
   * @param attr attribute name, e.g. {@code "domainTerm"} or {@code "akn:refersTo"}
   */
  public XmlContainsElementAssert<P> hasAttributeKey(String attr) {
    if (this.attrKeysFilter == null) this.attrKeysFilter = new HashSet<>();
    this.attrKeysFilter.add(attr);
    assertConstraint();
    return this;
  }

  /**
   * Constrains elements to those with text content equal to {@code text}, then fires the assertion.
   * Combined with any prior constraints.
   *
   * @param text expected text content
   */
  public XmlContainsElementAssert<P> hasText(String text) {
    this.textFilter = text;
    assertConstraint();
    return this;
  }

  /** Returns to the parent assertion for further chaining. */
  public P and() {
    return parent;
  }

  private void assertConstraint() {
    if (negate) assertNoneMatch();
    else assertAtLeastOneMatches();
  }

  private void assertAtLeastOneMatches() {
    boolean found = false;
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (matches(nodelist.item(i))) {
        found = true;
        break;
      }
    }
    assertThat(found)
        .as(
            "no element at path '%s' matching attrs=%s attrKeys=%s text=%s (checked %d candidate(s))",
            path, attrsFilter, attrKeysFilter, textFilter, nodelist.getLength())
        .isTrue();
  }

  private void assertNoneMatch() {
    boolean found = false;
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (matches(nodelist.item(i))) {
        found = true;
        break;
      }
    }
    assertThat(found)
        .as(
            "expected no element at path '%s' matching attrs=%s attrKeys=%s text=%s, but found one",
            path, attrsFilter, attrKeysFilter, textFilter)
        .isFalse();
  }

  private boolean matches(Node node) {
    if (attrKeysFilter != null) {
      for (String attr : attrKeysFilter) {
        if (XmlAssert.getAttrNode(node, attr) == null) return false;
      }
    }
    if (attrsFilter != null) {
      for (Map.Entry<String, String> entry : attrsFilter.entrySet()) {
        Node attrNode = XmlAssert.getAttrNode(node, entry.getKey());
        if (attrNode == null || !attrNode.getNodeValue().equals(entry.getValue())) return false;
      }
    }
    return textFilter == null || node.getTextContent().strip().equals(textFilter);
  }
}
