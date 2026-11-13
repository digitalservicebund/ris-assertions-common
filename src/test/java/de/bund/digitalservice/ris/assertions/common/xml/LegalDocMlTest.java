package de.bund.digitalservice.ris.assertions.common.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.junit.jupiter.api.Test;

class LegalDocMlTest {

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
}
