package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class XmlElementAssertTest {

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

  private static final String NS_ATTR_XML =
      """
      <root xmlns:ris="http://ldml.neuris.de/adm/bzst/meta/">
        <item ris:category="cat1" plain="val1">hello</item>
      </root>
      """;

  private static final String FRBRURI_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:identification/akn:FRBRuri";

  private static final String ANWENDUNGSZEITRAUM_PATH =
      "akn:akomaNtoso/akn:doc/akn:meta/akn:proprietary/ris:meta/ris:anwendungszeitraum";

  // ── hasAttribute

  @Test
  void hasAttribute_presentWithCorrectValue_succeeds() {
    assertThatXml(XML).hasSingleElement(FRBRURI_PATH).hasAttribute("value", "some-uri");
  }

  @Test
  void hasAttribute_absentAttribute_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasSingleElement(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttribute("missing", "any"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttribute_wrongValue_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasSingleElement(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttribute("value", "wrong-uri"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttribute_namespacedAttr_succeeds() {
    assertThatXml(NS_ATTR_XML).hasSingleElement("root/item").hasAttribute("ris:category", "cat1");
  }

  @Test
  void hasAttribute_namespacedAttr_wrongValue_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(NS_ATTR_XML).hasSingleElement("root/item");
    assertThatThrownBy(() -> elementAssert.hasAttribute("ris:category", "wrong"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributes

  @Test
  void hasAttributes_allPresent_succeeds() {
    assertThatXml(XML).hasSingleElement(FRBRURI_PATH).hasAttributes(Map.of("value", "some-uri"));
  }

  @Test
  void hasAttributes_oneMissing_fails() {
    Map<String, String> attrs = Map.of("value", "some-uri", "missing", "x");
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasSingleElement(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttributes(attrs)).isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttributes_oneWrongValue_fails() {
    Map<String, String> attrs = Map.of("value", "wrong");
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasSingleElement(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttributes(attrs)).isInstanceOf(AssertionError.class);
  }

  // ── hasText

  @Test
  void hasText_correctValue_succeeds() {
    // XML contains "  2024-01-01  "; strip() is applied before comparison
    assertThatXml(XML).hasSingleElement(ANWENDUNGSZEITRAUM_PATH).hasText("2024-01-01");
  }

  @Test
  void hasText_wrongValue_fails() {
    XmlElementAssert<?> elementAssert =
        assertThatXml(XML).hasSingleElement(ANWENDUNGSZEITRAUM_PATH);
    assertThatThrownBy(() -> elementAssert.hasText("1999-01-01"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasAttributeKey

  @Test
  void hasAttributeKey_presentAttribute_succeeds() {
    assertThatXml(XML).hasSingleElement(FRBRURI_PATH).hasAttributeKey("value");
  }

  @Test
  void hasAttributeKey_absentAttribute_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasSingleElement(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.hasAttributeKey("missing"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasAttributeKey_namespacedAttr_succeeds() {
    assertThatXml(NS_ATTR_XML).hasSingleElement("root/item").hasAttributeKey("ris:category");
  }

  // ── doesNotHaveAttributeKey

  @Test
  void doesNotHaveAttributeKey_absentAttribute_succeeds() {
    assertThatXml(XML).hasSingleElement(FRBRURI_PATH).doesNotHaveAttributeKey("missing");
  }

  @Test
  void doesNotHaveAttributeKey_presentAttribute_fails() {
    XmlElementAssert<?> elementAssert = assertThatXml(XML).hasSingleElement(FRBRURI_PATH);
    assertThatThrownBy(() -> elementAssert.doesNotHaveAttributeKey("value"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void doesNotHaveAttributeKey_namespacedAttr_succeeds() {
    assertThatXml(NS_ATTR_XML).hasSingleElement("root/item").doesNotHaveAttributeKey("ris:missing");
  }

  // ── doesNotHaveText

  @Test
  void doesNotHaveText_differentValue_succeeds() {
    assertThatXml(XML).hasSingleElement(ANWENDUNGSZEITRAUM_PATH).doesNotHaveText("1999-01-01");
  }

  @Test
  void doesNotHaveText_matchingValue_fails() {
    XmlElementAssert<?> elementAssert =
        assertThatXml(XML).hasSingleElement(ANWENDUNGSZEITRAUM_PATH);
    assertThatThrownBy(() -> elementAssert.doesNotHaveText("2024-01-01"))
        .isInstanceOf(AssertionError.class);
  }

  // ── chaining

  @Test
  void methodsAreChainable() {
    assertThatXml(XML)
        .hasSingleElement(FRBRURI_PATH)
        .hasAttribute("value", "some-uri")
        .hasAttributes(Map.of("value", "some-uri"));
  }

  @Test
  void and_returnsParent() {
    XmlAssert parent = assertThatXml(XML);
    XmlAssert returned =
        parent.hasSingleElement(FRBRURI_PATH).hasAttribute("value", "some-uri").and();
    assertThat(returned).isSameAs(parent);
  }
}
