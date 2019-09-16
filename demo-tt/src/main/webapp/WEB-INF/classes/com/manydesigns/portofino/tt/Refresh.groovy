package com.manydesigns.portofino.tt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * Sample class showcasing Spring context reload support.
 */
@Component
class Refresh {
    Logger logger = LoggerFactory.getLogger(this.class)

    boolean changeMe = true

    @PostConstruct
    void hello() {
        logger.info("Loaded ${this} with class ${this.class.name} - ${this.class.hashCode()} and changeMe = ${changeMe}")
    }

}
