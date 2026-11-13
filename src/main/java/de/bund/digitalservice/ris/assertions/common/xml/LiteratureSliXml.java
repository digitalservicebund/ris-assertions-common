package de.bund.digitalservice.ris.assertions.common.xml;

/** Entry point and XPath constants for SLI Literature XML assertions. */
public final class LiteratureSliXml {

    private static final String META = "akn:akomaNtoso/akn:doc/akn:meta";

    /** XPath to the {@code akn:meta} element. */
    public static final String AKN_META_X_PATH = META;

    /** XPath to the {@code akn:identification} element. */
    public static final String AKN_IDENTIFICATION_X_PATH = META + "/akn:identification";

    /** XPath to the {@code akn:classification} element. */
    public static final String AKN_CLASSIFICATION_X_PATH = META + "/akn:classification";

    /** XPath to the {@code ris:meta} element. */
    public static final String RIS_META_X_PATH = META + "/akn:proprietary/ris:meta";

    /** XPath to the {@code ris:referenzRechtsprechung} element. */
    public static final String RIS_REFERENZ_RECHTSPRECHUNG_X_PATH =
            META + "/akn:analysis/akn:otherReferences/akn:implicitReference/ris:referenzRechtsprechung";

    private LiteratureSliXml() {}

    /**
     * Creates an assertion for the given SLI Literature XML string.
     *
     * @param xml the XML string to assert
     * @return a new {@link XmlAssert} configured with SLI Literature namespaces
     */
    public static XmlAssert assertThatXml(String xml) {
        return new XmlAssert(xml, XmlDocumentPaths.LITERATURE_SLI);
    }
}
