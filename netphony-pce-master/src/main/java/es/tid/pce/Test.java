package es.tid.pce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        System.out.println("1111");
        for (int i=0;i<5;i++){
            LOGGER.info("这是一条数据"+i);
        }
        System.out.println("2222");
    }
}
