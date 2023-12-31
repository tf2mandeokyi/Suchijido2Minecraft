package com.mndk.scjdmc.util;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class StackedThrowables extends ArrayList<Throwable> {

    public void popAllToLogger(Logger logger, String extraMessage) {
        logger.error(extraMessage);
        for(Throwable e : this) {
            logger.error(e);
        }
        clear();
    }

}
