package com.manydesigns.portofino.model;

import jakarta.xml.bind.Unmarshaller;

public interface Unmarshallable {

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent);

}
