package de.bund.digitalservice.ris.assertions.common.xml;

import java.util.Map;

/**
 * Namespace map for a specific XML document schema, used by XPath evaluation.
 *
 * @param namespaces map of namespace prefix to namespace URI
 */
public record XmlDocumentPaths(Map<String, String> namespaces) {

  /** Namespace configuration for BZST XML documents. */
  public static final XmlDocumentPaths BZST =
      new XmlDocumentPaths(
          Map.of(
              "akn", "http://docs.oasis-open.org/legaldocml/ns/akn/3.0",
              "ris", "http://ldml.neuris.de/adm/bzst/meta/"));
}
