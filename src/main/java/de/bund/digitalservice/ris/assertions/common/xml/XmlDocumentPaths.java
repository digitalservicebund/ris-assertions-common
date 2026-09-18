package de.bund.digitalservice.ris.assertions.common.xml;

import java.util.Map;

public record XmlDocumentPaths(Map<String, String> namespaces) {

  public static final XmlDocumentPaths BZST =
      new XmlDocumentPaths(
          Map.of(
              "akn", "http://docs.oasis-open.org/legaldocml/ns/akn/3.0",
              "ris", "http://ldml.neuris.de/adm/bzst/meta/"));
}
