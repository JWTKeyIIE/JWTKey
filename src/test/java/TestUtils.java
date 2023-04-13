import com.xu.environmentInit.Exception.ExceptionHandler;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.xu.utils.Utils.retrievePackageFromJavaFiles;

/**
 * Utils类的单元测试
 */
public class TestUtils extends TestCase {
    @Test
    public void testRetrievePackageFromJavaFiles() throws ExceptionHandler {
        List<String> sourceFiles = new ArrayList<>();
        sourceFiles.add("../../main/java/com/xu/Entry.java");
        sourceFiles.add("src/main/java/com/xu/BaseAnalysis.java");
        String result = retrievePackageFromJavaFiles(sourceFiles);
        System.out.println(result);
    }
}
