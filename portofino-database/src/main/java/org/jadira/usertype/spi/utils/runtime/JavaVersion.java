package org.jadira.usertype.spi.utils.runtime;

/**
 * Patched to work on all versions of Java. https://github.com/JadiraOrg/jadira/issues/71
 */
public class JavaVersion {

    public static boolean isJava8OrLater() {
        return true;
    }
}
