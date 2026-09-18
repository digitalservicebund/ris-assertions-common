package de.bund.digitalservice.ris.assertions.common.xml;

public final class BzstXml {

  private static final String META = "akn:akomaNtoso/akn:doc/akn:meta";

  public static final String AKN_META_X_PATH = META;
  public static final String AKN_IDENTIFICATION_X_PATH = META + "/akn:identification";
  public static final String AKN_CLASSIFICATION_X_PATH = META + "/akn:classification";
  public static final String RIS_META_X_PATH = META + "/akn:proprietary/ris:meta";
  public static final String RIS_REFERENZ_RECHTSPRECHUNG_X_PATH =
      META + "/akn:analysis/akn:otherReferences/akn:implicitReference/ris:referenzRechtsprechung";

  private BzstXml() {}

  public static XmlAssert assertThatXml(String xml) {
    return new XmlAssert(xml, XmlDocumentPaths.BZST);
  }
}
