package es.tid.pce.tests;

import org.junit.Test;
import org.apache.log4j.Logger;

public class LogTester {
    private static Logger logger = Logger.getLogger(Test.class);
    @Test
    public void logTester() {
        // 记录debug级别的信息
        logger.debug("This is debug message.");
        // 记录info级别的信息
        logger.info("This is info message.");
        // 记录error级别的信息
        logger.error("This is error message.");
    }
}
