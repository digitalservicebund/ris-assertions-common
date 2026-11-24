package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XmlElementsAssert}, the assertions on a fixed-count set of elements obtained via
 * {@link XmlAssert#hasElements(String, int)}.
 *
 * <p>Covers index-based access ({@code element}), set-wide matching ({@code containsElement},
 * {@code doesNotContainElement}) and navigation between the set and its elements via {@code and()}.
 */
class XmlElementsAssertTest {

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
   * LegalDocML fixture with three {@code akn:keyword} elements whose attributes ({@code kw1}…{@code
   * kw3}) and texts ({@code alpha}, {@code beta}, {@code gamma}) identify their document order.
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
              <akn:keyword showAs="kw3" refersTo="term3">gamma</akn:keyword>
            </akn:classification>
          </akn:meta>
        </akn:doc>
      </akn:akomaNtoso>
      """;

  /** Path to the three {@code akn:keyword} elements in {@link #XML}. */
  private static final String KEYWORD_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword";

  // ── element

  /** Index {@code 0} selects the first element in document order. */
  @Test
  void element_firstIndex_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).element(0).hasAttribute("showAs", "kw1");
  }

  /** The last valid index ({@code count - 1}) selects the last element in document order. */
  @Test
  void element_lastIndex_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).element(2).hasAttribute("showAs", "kw3");
  }

  /**
   * An index equal to the element count fails with an {@link AssertionError}, not an {@link
   * IndexOutOfBoundsException}.
   */
  @Test
  void element_outOfBounds_fails() {
    XmlElementsAssert elementsAssert = assertThatXml(XML).hasElements(KEYWORD_PATH, 3);
    assertThatThrownBy(() -> elementsAssert.element(3)).isInstanceOf(AssertionError.class);
  }

  /** A valid index yields an element assertion for further checks. */
  @Test
  void element_returnsNonNullAssert() {
    XmlElementAssert<?> el = assertThatXml(XML).hasElements(KEYWORD_PATH, 3).element(0);
    assertThat(el).isNotNull();
  }

  // ── containsElement

  /** Returns a builder over the whole set; matching is asserted once a constraint is added. */
  @Test
  void containsElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsElement();
    assertThat(result).isNotNull();
  }

  /** Passes when any element of the set, here the second one, has the attribute value. */
  @Test
  void containsElement_canChainHasAttribute_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsElement().hasAttribute("showAs", "kw2");
  }

  /** Passes when any element of the set, here the last one, has the text. */
  @Test
  void containsElement_canChainHasText_succeeds() {
    assertThatXml(XML).hasElements(KEYWORD_PATH, 3).containsElement().hasText("gamma");
  }

  // ── doesNotContainElement

  /**
   * Returns a negated builder over the whole set; absence is asserted once a constraint is added.
   */
  @Test
  void doesNotContainElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).doesNotContainElement();
    assertThat(result).isNotNull();
  }

  /** Passes when no element of the set has the attribute. */
  @Test
  void doesNotContainElement_absentAttributeKey_succeeds() {
    assertThatXml(XML)
        .hasElements(KEYWORD_PATH, 3)
        .doesNotContainElement()
        .hasAttributeKey("missing");
  }

  /** Fails when any element of the set has the attribute. */
  @Test
  void doesNotContainElement_presentAttributeKey_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).doesNotContainElement();
    assertThatThrownBy(() -> noContainsAssert.hasAttributeKey("showAs"))
        .isInstanceOf(AssertionError.class);
  }

  /** Passes when no element of the set has the attribute value. */
  @Test
  void doesNotContainElement_noMatchingValue_succeeds() {
    assertThatXml(XML)
        .hasElements(KEYWORD_PATH, 3)
        .doesNotContainElement()
        .hasAttribute("showAs", "nonexistent");
  }

  /** Fails when any element of the set, here the first one, has the attribute value. */
  @Test
  void doesNotContainElement_matchingValue_fails() {
    XmlContainsElementAssert<?> noContainsAssert =
        assertThatXml(XML).hasElements(KEYWORD_PATH, 3).doesNotContainElement();
    assertThatThrownBy(() -> noContainsAssert.hasAttribute("showAs", "kw1"))
        .isInstanceOf(AssertionError.class);
  }

  // ── and

  /** {@code and()} returns the very {@link XmlAssert} the set was selected from. */
  @Test
  void and_returnsParentXmlAssert() {
    XmlAssert parent = assertThatXml(XML);
    XmlAssert returned = parent.hasElements(KEYWORD_PATH, 3).and();
    assertThat(returned).isSameAs(parent);
  }

  // ── chaining across elements

  /**
   * {@code and()} on an element returns to the set, so every element can be checked in one chain.
   */
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
