package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XmlContainsElementAssert}, the constraint builder returned by {@link
 * XmlAssert#containsElement(String)} and {@link XmlAssert#doesNotContainElement(String)}.
 *
 * <p>Every constraint ({@code hasAttribute}, {@code hasAttributes}, {@code hasAttributeKey}, {@code
 * hasText}) is combined with all constraints added before it and immediately asserted against the
 * candidate elements:
 *
 * <ul>
 *   <li>{@code containsElement}: at least one candidate must satisfy all constraints at once, so an
 *       empty candidate list always fails.
 *   <li>{@code doesNotContainElement}: no candidate may satisfy all constraints at once, so an
 *       empty candidate list always passes.
 * </ul>
 */
class XmlContainsElementAssertTest {

  /** Namespace mapping for the {@code akn} and {@code ris} prefixes used by the fixture. */
  private static final XmlDocumentPaths PATHS =
      new XmlDocumentPaths(
          Map.of(
              "akn", "http://docs.oasis-open.org/legaldocml/ns/akn/3.0",
              "ris", "http://rechtsinformationen.bund.de/schema/ris/0.1"));

  /**
   * Creates an {@link XmlAssert} for {@code xml} using {@link #PATHS}.
   *
   * @param xml the XML document under test
   * @return a new assertion on {@code xml}
   */
  private static XmlAssert assertThatXml(String xml) {
    return new XmlAssert(xml, PATHS);
  }

  /**
   * LegalDocML fixture with two {@code akn:keyword} candidates:
   *
   * <ul>
   *   <li>{@code showAs="kw1" refersTo="term1"} with text {@code alpha}
   *   <li>{@code showAs="kw2" refersTo="term2"} with text {@code beta}
   * </ul>
   *
   * <p>Mixing values of both (e.g. {@code kw1} with {@code term2}) yields constraints that each
   * match some candidate but never the same one.
   */
  private static final String XML =
      """
      <akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0"
                      xmlns:ris="http://rechtsinformationen.bund.de/schema/ris/0.1">
        <akn:doc>
          <akn:meta>
            <akn:classification>
              <akn:keyword showAs="kw1" refersTo="term1">alpha</akn:keyword>
              <akn:keyword showAs="kw2" refersTo="term2">beta</akn:keyword>
            </akn:classification>
          </akn:meta>
        </akn:doc>
      </akn:akomaNtoso>
      """;

  /** Path to the two {@code akn:keyword} candidates in {@link #XML}. */
  private static final String KEYWORD_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword";

  // ── hasAttribute

  /** Passes when at least one candidate has the attribute value. */
  @Test
  void hasAttribute_matchingElement_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1");
  }

  /** Fails when the attribute exists on candidates but none has the expected value. */
  @Test
  void hasAttribute_noMatchingValue_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttribute("showAs", "nonexistent"))
        .isInstanceOf(AssertionError.class);
  }

  /** Fails when no candidate has the attribute at all. */
  @Test
  void hasAttribute_absentAttributeName_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttribute("missing", "value"))
        .isInstanceOf(AssertionError.class);
  }

  /** Fails when the path matches no element, as there is no candidate to satisfy the constraint. */
  @Test
  void hasAttribute_emptyNodeList_fails() {
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing");
    assertThatThrownBy(() -> containsAssert.hasAttribute("any", "value"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributes

  /** Passes when one candidate has all expected attribute values. */
  @Test
  void hasAttributes_allMatch_succeeds() {
    assertThatXml(XML)
        .containsElement(KEYWORD_PATH)
        .hasAttributes(Map.of("showAs", "kw1", "refersTo", "term1"));
  }

  /** Fails when a candidate matches only some of the expected attribute values. */
  @Test
  void hasAttributes_partialMatch_fails() {
    Map<String, String> attrs = Map.of("showAs", "kw1", "refersTo", "WRONG");
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttributes(attrs))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasText

  /** Passes when at least one candidate has the text. */
  @Test
  void hasText_matchingElement_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasText("alpha");
  }

  /** Fails when no candidate has the text. */
  @Test
  void hasText_noMatchingElement_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasText("gamma")).isInstanceOf(AssertionError.class);
  }

  // ── combined constraints

  /** Passes when one candidate ({@code kw2}) satisfies both the attribute and the text. */
  @Test
  void combinedAttrAndText_bothMatch_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw2").hasText("beta");
  }

  /**
   * Fails although each constraint matches some candidate: {@code kw1} has text {@code alpha}, and
   * {@code beta} belongs to {@code kw2}.
   */
  @Test
  void combinedAttrAndText_attrMatchesButTextDiffers_fails() {
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1");
    assertThatThrownBy(() -> containsAssert.hasText("beta")).isInstanceOf(AssertionError.class);
  }

  /** Chained {@code hasAttribute} calls accumulate and hold on the same candidate ({@code kw1}). */
  @Test
  void multipleHasAttribute_bothMustHoldOnSameNode_succeeds() {
    assertThatXml(XML)
        .containsElement(KEYWORD_PATH)
        .hasAttribute("showAs", "kw1")
        .hasAttribute("refersTo", "term1");
  }

  /**
   * Fails although each attribute value exists on some candidate: no single element has both {@code
   * showAs="kw1"} and {@code refersTo="term2"}.
   */
  @Test
  void multipleHasAttribute_noSingleNodeSatisfiesBoth_fails() {
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1");
    assertThatThrownBy(() -> containsAssert.hasAttribute("refersTo", "term2"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributeKey

  /** Passes when at least one candidate has the attribute, regardless of its value. */
  @Test
  void hasAttributeKey_presentAttribute_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttributeKey("showAs");
  }

  /** Fails when no candidate has the attribute. */
  @Test
  void hasAttributeKey_absentAttribute_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttributeKey("missing"))
        .isInstanceOf(AssertionError.class);
  }

  /** Fails when the path matches no element, as there is no candidate to satisfy the constraint. */
  @Test
  void hasAttributeKey_emptyNodeList_fails() {
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing");
    assertThatThrownBy(() -> containsAssert.hasAttributeKey("showAs"))
        .isInstanceOf(AssertionError.class);
  }

  /** An attribute-presence constraint combines with an attribute-value constraint on one node. */
  @Test
  void hasAttributeKey_combinedWithHasAttribute_bothMustHoldOnSameNode_succeeds() {
    assertThatXml(XML)
        .containsElement(KEYWORD_PATH)
        .hasAttributeKey("showAs")
        .hasAttribute("refersTo", "term1");
  }

  // ── doesNotContainElement

  /** Passes when no candidate has the attribute value. */
  @Test
  void doesNotContainElement_noMatchingValue_succeeds() {
    assertThatXml(XML).doesNotContainElement(KEYWORD_PATH).hasAttribute("showAs", "nonexistent");
  }

  /** Fails when any candidate has the attribute value. */
  @Test
  void doesNotContainElement_matchingValue_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).doesNotContainElement(KEYWORD_PATH);
    assertThatThrownBy(() -> noContainsAssert.hasAttribute("showAs", "kw1"))
        .isInstanceOf(AssertionError.class);
  }

  /** Passes when no candidate has the attribute. */
  @Test
  void doesNotContainElement_absentAttributeKey_succeeds() {
    assertThatXml(XML).doesNotContainElement(KEYWORD_PATH).hasAttributeKey("missing");
  }

  /** Fails when any candidate has the attribute, regardless of its value. */
  @Test
  void doesNotContainElement_presentAttributeKey_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).doesNotContainElement(KEYWORD_PATH);
    assertThatThrownBy(() -> noContainsAssert.hasAttributeKey("showAs"))
        .isInstanceOf(AssertionError.class);
  }

  /** Passes when the path matches no element, as there is no candidate that could match. */
  @Test
  void doesNotContainElement_emptyNodeList_succeeds() {
    assertThatXml(XML)
        .doesNotContainElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing")
        .hasAttribute("any", "val");
  }

  /** Passes when no candidate has the text. */
  @Test
  void doesNotContainElement_noMatchingText_succeeds() {
    assertThatXml(XML).doesNotContainElement(KEYWORD_PATH).hasText("gamma");
  }

  /** Fails when any candidate has the text. */
  @Test
  void doesNotContainElement_matchingText_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).doesNotContainElement(KEYWORD_PATH);
    assertThatThrownBy(() -> noContainsAssert.hasText("alpha")).isInstanceOf(AssertionError.class);
  }

  // ── and

  /** {@code and()} returns the very {@link XmlAssert} the builder was created from. */
  @Test
  void and_returnsParentXmlAssert() {
    XmlAssert parent = assertThatXml(XML);
    XmlAssert returned = parent.containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1").and();
    assertThat(returned).isSameAs(parent);
  }
}
