package com.manydesigns.portofino.dispatcher;

import java.util.List;

public interface WithParameters {

    int getMinParameters();
    int getMaxParameters();

    List<String> getParameters();

    void consumeParameter(String pathSegment);
}
