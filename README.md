# ris-assertions-common

Common library for custom assertions.

## XML Assertions

### Usage

Import the document-specific factory and use `assertThatXml`:

```
import static de.bund.digitalservice.ris.assertions.common.xml.BzstXml.assertThatXml;

assertThatXml(xmlString)
    .hasSingleElement(BzstXml.AKN_IDENTIFICATION_X_PATH)
    .hasAttribute("value", "expected-uri");
```

### Adding a new document type

1. **Add a `XmlDocumentPaths` constant** in `XmlDocumentPaths.java`:

```java
public static final XmlDocumentPaths MY_DOCUMENT =
    new XmlDocumentPaths(
        Map.of(
            "akn", "http://docs.oasis-open.org/legaldocml/ns/akn/3.0",
            "ris", "http://ldml.neuris.de/<your-document-specific-namespace>/"));
```

2. **Create a factory class** `MyDocumentXml.java` in the same package:

```java
package de.bund.digitalservice.ris.assertions.common.xml;

public final class MyDocumentXml {

  public static final String META = "akn:akomaNtoso/akn:doc/akn:meta";
  // add XPath constants here

  private MyTeamXml() {}

  public static XmlAssert assertThatXml(String xml) {
    return new XmlAssert(xml, XmlDocumentPaths.MY_DOCUMENT);
  }
}
```

3. **Use it in tests** via static import:

```java
import static de.bund.digitalservice.ris.assertions.common.xml.MyDocumentXml.assertThatXml;
```
