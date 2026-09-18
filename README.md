# ris-assertions-common

Common library for custom assertions.

## Prerequisites

- Java 25
- [Lefthook](https://github.com/evilmartians/lefthook) - git hook manager
- [Trivy](https://github.com/aquasecurity/trivy) - vulnerability scanner (used in pre-push hook)

Gradle is managed by the wrapper (`./gradlew`) - no separate installation needed.

## Setup

```
lefthook install
```

## CI/CD

Workflows in `.github/workflows/` are thin callers that delegate to reusable workflows in
[digitalservicebund/ris-migration-common](https://github.com/digitalservicebund/ris-migration-common).

The following workflows must exist there with an `on: workflow_call` trigger:
- `.github/workflows/pipeline.yml`
- `.github/workflows/publish-package.yml`

`pipeline.yml` in `ris-migration-common` must reference `docs-generate.yml` using a full ref
(not a relative path) since relative `uses:` paths are not allowed inside reusable workflows.

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
