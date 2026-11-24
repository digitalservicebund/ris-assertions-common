package de.bund.digitalservice.ris.assertions.common.xml;

import static de.bund.digitalservice.ris.assertions.common.xml.LegalDocMl.*;
import static de.bund.digitalservice.ris.assertions.common.xml.XmlDocumentPaths.LEGALDOCML;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XmlAssert}, the document-level entry point of the XML assertions.
 *
 * <p>Covers the package-private XPath infrastructure ({@code toCleanedAbsoluteXPath}, {@code
 * evalXPath} and its {@link NamespaceContext}) as well as the public API that selects elements by
 * path ({@code hasElementOnlyOnce}, {@code hasElements}, {@code containsElement}, {@code
 * doesNotContainElement}) or by name anywhere in the document ({@code hasElementCountAnywhere},
 * {@code hasNoElementAnywhere}).
 *
 * <p>Assertions are created via {@link LegalDocMl#assertThatXml(String)}, so the tests run against
 * the same {@link XmlDocumentPaths#LEGALDOCML} namespace mapping that consumers use.
 */
class XmlAssertTest {

  /**
   * LegalDocML fixture using both the {@code akn} and {@code ris} namespaces. It contains exactly
   * one {@code akn:identification}, two {@code akn:keyword} elements and no {@code akn:missing} or
   * {@code akn:ghost} element, so single, multiple and absent matches can be asserted.
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

  /** Whitespace around {@code /} separators is removed and the path is made absolute. */
  @Test
  void toCleanedAbsoluteXPath_stripsSegmentWhitespace() {
    assertThat(XmlAssert.toCleanedAbsoluteXPath("akn:a / akn:b / akn:c"))
        .isEqualTo("/akn:a/akn:b/akn:c");
  }

  /** A relative path without whitespace only gains the leading {@code /}. */
  @Test
  void toCleanedAbsoluteXPath_noWhitespace_unchanged() {
    assertThat(XmlAssert.toCleanedAbsoluteXPath("akn:a/akn:b")).isEqualTo("/akn:a/akn:b");
  }

  /** An already absolute path is not prefixed twice: the empty first segment is dropped. */
  @Test
  void toCleanedAbsoluteXPath_leadingSlash_droppedAndReAdded() {
    assertThat(XmlAssert.toCleanedAbsoluteXPath("/akn:a/akn:b")).isEqualTo("/akn:a/akn:b");
  }

  // ── evalXPath

  /**
   * A syntactically invalid expression surfaces as an unchecked exception naming the expression,
   * instead of the checked {@link javax.xml.xpath.XPathExpressionException}.
   */
  @Test
  void evalXPath_invalidExpression_throwsRuntimeException() {
    assertThatThrownBy(() -> XmlAssert.evalXPath("[[invalid]]", XML, Map.of()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid XPath expression");
  }

  /**
   * The {@link NamespaceContext} built by {@code evalXPath} only supports prefix-to-URI lookup; the
   * reverse lookups {@code getPrefix} and {@code getPrefixes} return {@code null}.
   *
   * <p>The XPath engine never calls the reverse lookups, so the anonymous class ({@code
   * XmlAssert$1}) is instantiated reflectively to exercise them directly.
   */
  @Test
  void namespaceContext_reverseLookup_returnsNull() throws Exception {
    Constructor<?> constructor =
        Class.forName(XmlAssert.class.getName() + "$1").getDeclaredConstructor(Map.class);
    constructor.setAccessible(true);
    NamespaceContext context = (NamespaceContext) constructor.newInstance(LEGALDOCML.namespaces());

    assertThat(context.getPrefix("http://rechtsinformationen.bund.de/schema/ris/0.1")).isNull();
    assertThat(context.getPrefixes("http://rechtsinformationen.bund.de/schema/ris/0.1")).isNull();
  }

  // ── equals / hashCode

  /**
   * {@code equals} is rejected so it cannot be mistaken for an assertion such as {@code isEqualTo}.
   */
  @Test
  void equals_isUnsupported() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(() -> xmlAssert.equals(xmlAssert))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("equals is not supported");
  }

  /** {@code hashCode} is rejected, consistent with the unsupported {@code equals}. */
  @Test
  void hashCode_isUnsupported() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(xmlAssert::hashCode)
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("hashCode is not supported");
  }

  // ── hasElementOnlyOnce

  /** Passes when exactly one element exists at the path. */
  @Test
  void hasElementOnlyOnce_oneMatch_succeeds() {
    assertThatXml(XML).hasElementOnlyOnce("akn:akomaNtoso/akn:doc/akn:meta/akn:identification");
  }

  /** Fails when no element exists at the path. */
  @Test
  void hasElementOnlyOnce_noMatch_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(
            () -> xmlAssert.hasElementOnlyOnce("akn:akomaNtoso/akn:doc/akn:meta/akn:missing"))
        .isInstanceOf(AssertionError.class);
  }

  /** Fails when more than one element exists at the path (the fixture has two keywords). */
  @Test
  void hasElementOnlyOnce_multipleMatches_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(
            () ->
                xmlAssert.hasElementOnlyOnce(
                    "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword"))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasElements

  /** Passes when the number of elements at the path equals the expected count. */
  @Test
  void hasElements_exactCount_succeeds() {
    assertThatXml(XML)
        .hasElements("akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword", 2);
  }

  /** Fails when the number of elements at the path differs from the expected count. */
  @Test
  void hasElements_wrongCount_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(
            () ->
                xmlAssert.hasElements(
                    "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword", 1))
        .isInstanceOf(AssertionError.class);
  }

  /** An expected count of zero asserts that the path does not exist. */
  @Test
  void hasElements_zero_succeedsWhenAbsent() {
    assertThatXml(XML).hasElements("akn:akomaNtoso/akn:doc/akn:meta/akn:missing", 0);
  }

  // ── containsElement

  /**
   * Returns a builder; the match itself is only asserted once a constraint is added (see {@link
   * XmlContainsElementAssertTest}).
   */
  @Test
  void containsElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML)
            .containsElement("akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword");
    assertThat(result).isNotNull();
  }

  // ── doesNotContainElement

  /**
   * Returns a negated builder; the absence of a match is only asserted once a constraint is added
   * (see {@link XmlContainsElementAssertTest}).
   */
  @Test
  void doesNotContainElement_returnsNonNullAssert() {
    XmlContainsElementAssert<?> result =
        assertThatXml(XML)
            .doesNotContainElement(
                "akn:akomaNtoso/akn:doc/akn:meta/akn:classification/akn:keyword");
    assertThat(result).isNotNull();
  }

  // ── hasElementCountAnywhere

  /** Counts elements by name at any depth ({@code //akn:keyword}), independent of their path. */
  @Test
  void hasElementCountAnywhere_correctCount_succeeds() {
    assertThatXml(XML).hasElementCountAnywhere("akn:keyword", 2);
  }

  /** Fails when the number of elements with that name anywhere differs from the expected count. */
  @Test
  void hasElementCountAnywhere_wrongCount_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(() -> xmlAssert.hasElementCountAnywhere("akn:keyword", 5))
        .isInstanceOf(AssertionError.class);
  }

  // ── hasNoElementAnywhere

  /** Passes when no element with that name exists at any depth. */
  @Test
  void hasNoElementAnywhere_absentElement_succeeds() {
    assertThatXml(XML).hasNoElementAnywhere("akn:ghost");
  }

  /** Fails when at least one element with that name exists at any depth. */
  @Test
  void hasNoElementAnywhere_presentElement_fails() {
    XmlAssert xmlAssert = assertThatXml(XML);
    assertThatThrownBy(() -> xmlAssert.hasNoElementAnywhere("akn:keyword"))
        .isInstanceOf(AssertionError.class);
  }
}
