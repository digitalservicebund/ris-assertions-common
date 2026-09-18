package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.w3c.dom.NodeList;

public final class XmlElementsAssert {

  private final NodeList nodelist;
  private final String path;
  private final XmlAssert parent;
  private final XmlDocumentPaths paths;

  XmlElementsAssert(NodeList nodelist, String path, XmlAssert parent, XmlDocumentPaths paths) {
    this.nodelist = nodelist;
    this.path = path;
    this.parent = parent;
    this.paths = paths;
  }

  /**
   * Returns the element at {@code index} (0-based, document order) for further assertions.
   *
   * @param index 0-based position in document order
   */
  public XmlElementAssert<XmlElementsAssert> element(int index) {
    assertThat(index)
        .as(
            "index %d out of bounds for %d element(s) at path '%s'",
            index, nodelist.getLength(), path)
        .isLessThan(nodelist.getLength());
    return new XmlElementAssert<>(nodelist.item(index), path + "[" + index + "]", this, paths);
  }

  /**
   * Asserts at least one element in this set matches the constraints added via {@link
   * XmlContainsElementAssert#hasAttribute}, {@link XmlContainsElementAssert#hasAttributeKey}, and
   * {@link XmlContainsElementAssert#hasText}. Total count is not checked.
   */
  public XmlContainsElementAssert<XmlElementsAssert> containsElement() {
    return new XmlContainsElementAssert<>(nodelist, path, this, paths);
  }

  /**
   * Returns a builder that asserts no element in this set matches the constraints added via {@link
   * XmlContainsElementAssert#hasAttribute}, {@link XmlContainsElementAssert#hasAttributeKey}, and
   * {@link XmlContainsElementAssert#hasText}. Inverse of {@link #containsElement()}.
   */
  public XmlContainsElementAssert<XmlElementsAssert> containsNoElement() {
    return new XmlContainsElementAssert<>(nodelist, path, this, paths, true);
  }

  /** Returns to the parent {@link XmlAssert} for further chaining. */
  public XmlAssert and() {
    return parent;
  }
}
