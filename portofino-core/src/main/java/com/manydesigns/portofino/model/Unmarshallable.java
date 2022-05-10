package com.manydesigns.portofino.model;

import javax.xml.bind.Unmarshaller;

public interface Unmarshallable {

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent);

}
