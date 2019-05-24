import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import magpiebridge.core.AnalysisResult;

/**
 * 
 * @author Linghui Luo
 *
 */
public class DemoTest {

  @Test
  public void test() {
    String testTargetPath = new File("src/test/resources/DemoProject/src/main/java").getAbsolutePath();
    SimpleServerAnalysis analysis = new SimpleServerAnalysis();
    Collection<AnalysisResult> results = analysis.analyze(Collections.singleton(testTargetPath), Collections.emptySet());
    for (AnalysisResult re : results) {
      System.err.println(re.toString());
    }
  }
}
