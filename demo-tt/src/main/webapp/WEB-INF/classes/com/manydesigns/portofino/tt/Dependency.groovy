package com.manydesigns.portofino.tt

import com.manydesigns.portofino.persistence.Persistence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import jakarta.annotation.PostConstruct

/**
 * Sample class showcasing Spring context reload support.
 */
@Service
class Dependency {
    Logger logger = LoggerFactory.getLogger(this.class)

    boolean changeMe = true

    @Autowired
    Persistence persistence

    @PostConstruct
    void hello() {
        logger.info("Loaded ${this} with class ${this.class.name} - ${this.class.hashCode()} and changeMe = ${changeMe}")
    }

}
