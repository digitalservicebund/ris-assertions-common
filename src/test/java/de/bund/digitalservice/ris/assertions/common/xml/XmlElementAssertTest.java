package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XmlElementAssert}, the assertions on a single element obtained via {@link
 * XmlAssert#hasElementOnlyOnce(String)}.
 *
 * <p>Covers attribute values ({@code hasAttribute}, {@code hasAttributes}), attribute presence
 * ({@code hasAttributeKey}, {@code doesNotHaveAttributeKey}) and text content ({@code hasText},
 * {@code doesNotHaveText}), including namespace-prefixed attribute names, as well as navigating
 * back to the parent via {@code and()}.
 */
class XmlElementAssertTest {

  /** Namespace mapping for the {@code akn} and {@code ris} prefixes used by the fixtures. */
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
   * LegalDocML fixture with an {@code akn:FRBRuri} carrying a {@code value} attribute and a {@code
   * ris:anwendungszeitraum} whose text is padded with whitespace to verify stripping.
   */
  private static final String XML =
      """
      <akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0"
                      xmlns:ris="http://rechtsinformationen.bund.de/schema/ris/0.1">
        <akn:doc>
          <akn:meta>
            <akn:identification>
              <akn:FRBRuri value="some-uri"/>
            </akn:identification>
            <akn:proprietary>
              <ris:meta>
                <ris:anwendungszeitraum>  2024-01-01  </ris:anwendungszeitraum>
              </ris:meta>
            </akn:proprietary>
          </akn:meta>
        </akn:doc>
      </akn:akomaNtoso>
      """;

  /**
   * Fixture with an un-namespaced {@code item} element carrying both a namespaced ({@code
   * ris:category}) and a plain ({@code plain}) attribute, to verify prefix resolution in attribute
   * names.
   */
  private static final String NS_ATTR_XML =
      """
      <root xmlns:ris="http://rechtsinformationen.bund.de/schema/ris/0.1">
        <item ris:category="cat1" plain="val1">hello</item>
      </root>
      """;

  /** Path to the single {@code akn:FRBRuri} element in {@link #XML}. */
  private static final String FRBRURI_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:identification/akn:FRBRuri";

  /** Path to the single {@code ris:anwendungszeitraum} element in {@link #XML}. */
  private static final String ANWENDUNGSZEITRAUM_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:proprietary/ris:meta/ris:anwendungszeitraum";

  // ── hasAttribute

  /** Passes when the attribute exists with the expected value. */
  @Test
  void hasAttribute_presentWithCorrectValue_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH).hasAttribute("value", "some-uri");
  }

  /** Fails when the element has no such attribute. */
  @Test
  void hasAttribute_absentAttribute_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttribute("missing", "any"))
        .isInstanceOf(AssertionError.class);
  }

  /** Fails when the attribute exists but has a different value. */
  @Test
  void hasAttribute_wrongValue_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttribute("value", "wrong-uri"))
        .isInstanceOf(AssertionError.class);
  }

  /** A prefixed attribute name is resolved through the namespace mapping. */
  @Test
  void hasAttribute_namespacedAttr_succeeds() {
    assertThatXml(NS_ATTR_XML).hasElementOnlyOnce("root/item").hasAttribute("ris:category", "cat1");
  }

  /** A prefixed attribute with a different value fails like an un-prefixed one. */
  @Test
  void hasAttribute_namespacedAttr_wrongValue_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(NS_ATTR_XML).hasElementOnlyOnce("root/item");
    assertThatThrownBy(() -> elementAssert.hasAttribute("ris:category", "wrong"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributes

  /** Passes when every expected attribute exists with its expected value. */
  @Test
  void hasAttributes_allPresent_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH).hasAttributes(Map.of("value", "some-uri"));
  }

  /** Fails when any one of the expected attributes is missing, even if the others match. */
  @Test
  void hasAttributes_oneMissing_fails() {
    Map<String, String> attrs = Map.of("value", "some-uri", "missing", "x");
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttributes(attrs)).isInstanceOf(AssertionError.class);
  }

  /** Fails when an expected attribute exists but has a different value. */
  @Test
  void hasAttributes_oneWrongValue_fails() {
    Map<String, String> attrs = Map.of("value", "wrong");
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttributes(attrs)).isInstanceOf(AssertionError.class);
  }

  // ── hasText

  /**
   * Text content is stripped before comparison: the fixture pads {@code 2024-01-01} with leading
   * and trailing spaces and still matches {@code "2024-01-01"}.
   */
  @Test
  void hasText_correctValue_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce(ANWENDUNGSZEITRAUM_PATH).hasText("2024-01-01");
  }

  /** Fails when the stripped text content differs from the expected text. */
  @Test
  void hasText_wrongValue_fails() {
    XmlElementAssert<?> elementAssert =
        assertThatXml(XML).hasElementOnlyOnce(ANWENDUNGSZEITRAUM_PATH);
    assertThatThrownBy(() -> elementAssert.hasText("1999-01-01"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributeKey

  /** Passes when the attribute exists, regardless of its value. */
  @Test
  void hasAttributeKey_presentAttribute_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH).hasAttributeKey("value");
  }

  /** Fails when the element has no such attribute. */
  @Test
  void hasAttributeKey_absentAttribute_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttributeKey("missing"))
        .isInstanceOf(AssertionError.class);
  }

  /** A prefixed attribute name is resolved through the namespace mapping. */
  @Test
  void hasAttributeKey_namespacedAttr_succeeds() {
    assertThatXml(NS_ATTR_XML).hasElementOnlyOnce("root/item").hasAttributeKey("ris:category");
  }

  // ── doesNotHaveAttributeKey

  /** Passes when the element has no such attribute. */
  @Test
  void doesNotHaveAttributeKey_absentAttribute_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH).doesNotHaveAttributeKey("missing");
  }

  /** Fails when the attribute exists, regardless of its value. */
  @Test
  void doesNotHaveAttributeKey_presentAttribute_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasElementOnlyOnce(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.doesNotHaveAttributeKey("value"))
        .isInstanceOf(AssertionError.class);
  }

  /** A prefixed attribute name absent from the element passes. */
  @Test
  void doesNotHaveAttributeKey_namespacedAttr_succeeds() {
    assertThatXml(NS_ATTR_XML)
        .hasElementOnlyOnce("root/item")
        .doesNotHaveAttributeKey("ris:missing");
  }

  // ── doesNotHaveText

  /** Passes when the stripped text content differs from the given text. */
  @Test
  void doesNotHaveText_differentValue_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce(ANWENDUNGSZEITRAUM_PATH).doesNotHaveText("1999-01-01");
  }

  /** Fails when the stripped text content equals the given text; padding does not avoid a match. */
  @Test
  void doesNotHaveText_matchingValue_fails() {
    XmlElementAssert<?> elementAssert =
        assertThatXml(XML).hasElementOnlyOnce(ANWENDUNGSZEITRAUM_PATH);
    assertThatThrownBy(() -> elementAssert.doesNotHaveText("2024-01-01"))
        .isInstanceOf(AssertionError.class);
  }

  // ── chaining

  /** Each assertion returns the same element assertion, so several can be chained. */
  @Test
  void methodsAreChainable() {
    assertThatXml(XML)
        .hasElementOnlyOnce(FRBRURI_PATH)
        .hasAttribute("value", "some-uri")
        .hasAttributes(Map.of("value", "some-uri"));
  }

  /** {@code and()} returns the very {@link XmlAssert} the element was selected from. */
  @Test
  void and_returnsParent() {
    XmlAssert parent = assertThatXml(XML);
    XmlAssert returned =
        parent.hasElementOnlyOnce(FRBRURI_PATH).hasAttribute("value", "some-uri").and();
    assertThat(returned).isSameAs(parent);
  }
}
