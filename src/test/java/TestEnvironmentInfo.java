import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.ArgumentCheck;
import com.xu.environmentInit.EnvironmentInfo;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TestEnvironmentInfo extends TestCase {
    @Test
    public EnvironmentInfo testInitEnvironment() throws ExceptionHandler, IOException {
        String str = "-in source -s samples/SimpleJavaJar3 -d samples/SimpleJavaJar3/build/dependencies -V";
        ArrayList<String> args = new ArrayList(Arrays.asList(str.split(" ")));
        System.out.println(args.size());
        for(int i = 0;i < args.size();i++){
            System.out.println(args.get(i));
        }
        EnvironmentInfo environmentInfo = ArgumentCheck.parameterCheck(args);
        System.out.println("init environment info successfully");
        return environmentInfo;
    }


}
