package de.bund.digitalservice.ris.adm.bzst.assertions;

import static de.bund.digitalservice.ris.adm.bzst.assertions.XmlAssert.assertThatXml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class XmlContainsElementAssertTest {

  private static final String XML =
      """
      <akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0"
                      xmlns:ris="http://ldml.neuris.de/adm/bzst/meta/">
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

  private static final String KEYWORD_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword";

  // ── hasAttribute

  @Test
  void hasAttribute_matchingElement_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1");
  }

  @Test
  void hasAttribute_noMatchingValue_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttribute("showAs", "nonexistent"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttribute_absentAttributeName_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttribute("missing", "value"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttribute_emptyNodeList_fails() {
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing");
    assertThatThrownBy(() -> containsAssert.hasAttribute("any", "value"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributes

  @Test
  void hasAttributes_allMatch_succeeds() {
    assertThatXml(XML)
        .containsElement(KEYWORD_PATH)
        .hasAttributes(Map.of("showAs", "kw1", "refersTo", "term1"));
  }

  @Test
  void hasAttributes_partialMatch_fails() {
    Map<String, String> attrs = Map.of("showAs", "kw1", "refersTo", "WRONG");
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttributes(attrs))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasText

  @Test
  void hasText_matchingElement_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasText("alpha");
  }

  @Test
  void hasText_noMatchingElement_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasText("gamma")).isInstanceOf(AssertionError.class);
  }

  // ── combined constraints

  @Test
  void combinedAttrAndText_bothMatch_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw2").hasText("beta");
  }

  @Test
  void combinedAttrAndText_attrMatchesButTextDiffers_fails() {
    // kw1 has text "alpha", not "beta"
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1");
    assertThatThrownBy(() -> containsAssert.hasText("beta")).isInstanceOf(AssertionError.class);
  }

  @Test
  void multipleHasAttribute_bothMustHoldOnSameNode_succeeds() {
    // kw1: showAs="kw1" refersTo="term1" — both on same element
    assertThatXml(XML)
        .containsElement(KEYWORD_PATH)
        .hasAttribute("showAs", "kw1")
        .hasAttribute("refersTo", "term1");
  }

  @Test
  void multipleHasAttribute_noSingleNodeSatisfiesBoth_fails() {
    // no element has showAs="kw1" AND refersTo="term2"
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1");
    assertThatThrownBy(() -> containsAssert.hasAttribute("refersTo", "term2"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributeKey

  @Test
  void hasAttributeKey_presentAttribute_succeeds() {
    assertThatXml(XML).containsElement(KEYWORD_PATH).hasAttributeKey("showAs");
  }

  @Test
  void hasAttributeKey_absentAttribute_fails() {
    XmlContainsElementAssert<?> containsAssert = assertThatXml(XML).containsElement(KEYWORD_PATH);
    assertThatThrownBy(() -> containsAssert.hasAttributeKey("missing"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttributeKey_emptyNodeList_fails() {
    XmlContainsElementAssert<?> containsAssert =
        assertThatXml(XML).containsElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing");
    assertThatThrownBy(() -> containsAssert.hasAttributeKey("showAs"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttributeKey_combinedWithHasAttribute_bothMustHoldOnSameNode_succeeds() {
    assertThatXml(XML)
        .containsElement(KEYWORD_PATH)
        .hasAttributeKey("showAs")
        .hasAttribute("refersTo", "term1");
  }

  // ── containsNoElement

  @Test
  void containsNoElement_noMatchingValue_succeeds() {
    assertThatXml(XML).containsNoElement(KEYWORD_PATH).hasAttribute("showAs", "nonexistent");
  }

  @Test
  void containsNoElement_matchingValue_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).containsNoElement(KEYWORD_PATH);
    assertThatThrownBy(() -> noContainsAssert.hasAttribute("showAs", "kw1"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void containsNoElement_absentAttributeKey_succeeds() {
    assertThatXml(XML).containsNoElement(KEYWORD_PATH).hasAttributeKey("missing");
  }

  @Test
  void containsNoElement_presentAttributeKey_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).containsNoElement(KEYWORD_PATH);
    assertThatThrownBy(() -> noContainsAssert.hasAttributeKey("showAs"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void containsNoElement_emptyNodeList_succeeds() {
    assertThatXml(XML)
        .containsNoElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing")
        .hasAttribute("any", "val");
  }

  @Test
  void containsNoElement_noMatchingText_succeeds() {
    assertThatXml(XML).containsNoElement(KEYWORD_PATH).hasText("gamma");
  }

  @Test
  void containsNoElement_matchingText_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).containsNoElement(KEYWORD_PATH);
    assertThatThrownBy(() -> noContainsAssert.hasText("alpha")).isInstanceOf(AssertionError.class);
  }

  // ── and

  @Test
  void and_returnsParentXmlAssert() {
    XmlAssert parent = assertThatXml(XML);
    XmlAssert returned = parent.containsElement(KEYWORD_PATH).hasAttribute("showAs", "kw1").and();
    assertThat(returned).isSameAs(parent);
  }
}
