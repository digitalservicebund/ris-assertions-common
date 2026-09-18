package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class XmlAssertTest {

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
            <akn:classification>
              <akn:keyword showAs="kw1" refersTo="term1"/>
              <akn:keyword showAs="kw2" refersTo="term2"/>
            </akn:classification>
            <akn:proprietary>
              <ris:meta>
                <ris:anwendungszeitraum>2024-01-01</ris:anwendungszeitraum>
              </ris:meta>
            </akn:proprietary>
          </akn:meta>
        </akn:doc>
      </akn:akomaNtoso>
      """;

  // ── toCleanedAbsoluteXPath

  @Test
  void toCleanedAbsoluteXPath_stripsSegmentWhitespace() {
    assertThat(XmlAssert.toCleanedAbsoluteXPath("akn:a / akn:b / akn:c"))
        .isEqualTo("/akn:a/akn:b/akn:c");
  }

  @Test
  void toCleanedAbsoluteXPath_noWhitespace_unchanged() {
    assertThat(XmlAssert.toCleanedAbsoluteXPath("akn:a/akn:b")).isEqualTo("/akn:a/akn:b");
  }

  @Test
  void toCleanedAbsoluteXPath_leadingSlash_droppedAndReAdded() {
    assertThat(XmlAssert.toCleanedAbsoluteXPath("/akn:a/akn:b")).isEqualTo("/akn:a/akn:b");
  }

  // ── evalXPath

  @Test
  void evalXPath_invalidExpression_throwsRuntimeException() {
    assertThatThrownBy(() -> XmlAssert.evalXPath("[[invalid]]", XML, Map.of()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid XPath expression");
  }

  // ── hasSingleElement

  @Test
  void hasSingleElement_oneMatch_succeeds() {
    assertThatXml(XML).hasSingleElement("akn:akomaNtoso/akn:doc/akn:meta/akn:identification");
  }

  @Test
  void hasSingleElement_noMatch_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(
            () -> xmlAssert.hasSingleElement("akn:akomaNtoso/akn:doc/akn:meta/akn:missing"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasSingleElement_multipleMatches_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(
            () ->
                xmlAssert.hasSingleElement(
                    "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasElements

  @Test
  void hasElements_exactCount_succeeds() {
    assertThatXml(XML)
        .hasElements("akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword", 2);
  }

  @Test
  void hasElements_wrongCount_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(
            () ->
                xmlAssert.hasElements(
                    "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword", 1))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void hasElements_zero_succeedsWhenAbsent() {
    assertThatXml(XML).hasElements("akn:akomaNtoso/akn:doc/akn:meta/akn:missing", 0);
  }

  // ── containsElement

  @Test
  void containsElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML)
            .containsElement("akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword");
    assertThat(result).isNotNull();
  }

  // ── containsNoElement

  @Test
  void containsNoElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML)
            .containsNoElement("akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword");
    assertThat(result).isNotNull();
  }

  // ── hasElementCountAnywhere

  @Test
  void hasElementCountAnywhere_correctCount_succeeds() {
    assertThatXml(XML).hasElementCountAnywhere("akn:keyword", 2);
  }

  @Test
  void hasElementCountAnywhere_wrongCount_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(() -> xmlAssert.hasElementCountAnywhere("akn:keyword", 5))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasNoElementAnywhere

  @Test
  void hasNoElementAnywhere_absentElement_succeeds() {
    assertThatXml(XML).hasNoElementAnywhere("akn:ghost");
  }

  @Test
  void hasNoElementAnywhere_presentElement_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(() -> xmlAssert.hasNoElementAnywhere("akn:keyword"))
        .isInstanceOf(AssertionError.class);
  }
}
