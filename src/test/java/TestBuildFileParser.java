import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.projectParser.BuildFileParser;
import com.xu.environmentInit.projectParser.BuildFileParserFactory;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestBuildFileParser extends TestCase {
    @Test
    public void testMavenPomFileParser() throws ExceptionHandler {
        String projectRoot = "/home/xu/xu/JWTGuard/samples/4_linux-china_jwt-demo/linux-china-jwt-demo-eb471ae";
        BuildFileParser buildFileParser =
                BuildFileParserFactory.getBuildFileParser(projectRoot);
        System.out.println("Using the build parser: " + buildFileParser.toString());

        Map<String, List<String>> moduleVsDependency = buildFileParser.getDependencyList();
        System.out.println(moduleVsDependency.size());
    }

}
