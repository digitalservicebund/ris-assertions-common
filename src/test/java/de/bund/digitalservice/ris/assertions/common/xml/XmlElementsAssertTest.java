package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class XmlElementsAssertTest {

  private static final XmlDocumentPaths PATHS =
      new XmlDocumentPaths(
          Map.of(
              "akn", "http://docs.oasis-open.org/legaldocml/ns/akn/3.0",
              "ris", "http://ldml.neuris.de/adm/bzst/meta/"));

  private static XmlAssert assertThatXml(String xml) {
    return new XmlAssert(xml, PATHS);
  }

  private static final String XML =
      """
      <akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0"
                      xmlns:ris="http://ldml.neuris.de/adm/bzst/meta/">
        <akn:doc>
          <akn:meta>
            <akn:classification>
              <akn:keyword showAs="kw1" refersTo="term1">alpha</akn:keyword>
              <akn:keyword showAs="kw2" refersTo="term2">beta</akn:keyword>
              <akn:keyword showAs="kw3" refersTo="term3">gamma</akn:keyword>
            </akn:classification>
          </akn:meta>
        </akn:doc>
      </akn:akomaNtoso>
      """;

  private static final String KEYWORD_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword";

  // ── element

  @Test
  void element_firstIndex_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).element(0).hasAttribute("showAs", "kw1");
  }

  @Test
  void element_lastIndex_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).element(2).hasAttribute("showAs", "kw3");
  }

  @Test
  void element_outOfBounds_fails() {
    XmlElementsAssert elementsAssert = assertThatXml(XML).hasElements(KEYWORD_PATH, 3);
    assertThatThrownBy(() -> elementsAssert.element(3)).isInstanceOf(AssertionError.class);
  }

  @Test
  void element_returnsNonNullAssert() {
    XmlElementAssert<?> el = assertThatXml(XML).hasElements(KEYWORD_PATH, 3).element(0);
    assertThat(el).isNotNull();
  }

  // ── containsElement

  @Test
  void containsElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsElement();
    assertThat(result).isNotNull();
  }

  @Test
  void containsElement_canChainHasAttribute_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsElement().hasAttribute("showAs", "kw2");
  }

  @Test
  void containsElement_canChainHasText_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsElement().hasText("gamma");
  }

  // ── containsNoElement

  @Test
  void containsNoElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsNoElement();
    assertThat(result).isNotNull();
  }

  @Test
  void containsNoElement_absentAttributeKey_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsNoElement().hasAttributeKey("missing");
  }

  @Test
  void containsNoElement_presentAttributeKey_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsNoElement();
    assertThatThrownBy(() -> noContainsAssert.hasAttributeKey("showAs"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void containsNoElement_noMatchingValue_succeeds() {
    assertThatXml(XML)
        .hasElements(KEYWORD_PATH, 3)
        .containsNoElement()
        .hasAttribute("showAs", "nonexistent");
  }

  @Test
  void containsNoElement_matchingValue_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsNoElement();
    assertThatThrownBy(() -> noContainsAssert.hasAttribute("showAs", "kw1"))
        .isInstanceOf(AssertionError.class);
  }

  // ── and

  @Test
  void and_returnsParentXmlAssert() {
    XmlAssert parent = assertThatXml(XML);
    XmlAssert returned = parent.hasElements(KEYWORD_PATH, 3).and();
    assertThat(returned).isSameAs(parent);
  }

  // ── chaining across elements

  @Test
  void elementAndAndChain_traversesMultipleElements() {
    assertThatXml(XML)
        .hasElements(KEYWORD_PATH, 3)
        .element(0)
        .hasText("alpha")
        .and()
        .element(1)
        .hasText("beta")
        .and()
        .element(2)
        .hasText("gamma");
  }
}
