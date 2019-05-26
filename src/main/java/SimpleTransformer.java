import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;

import magpiebridge.core.AnalysisResult;

public class SimpleTransformer extends BodyTransformer {

  private Collection<AnalysisResult> results;

  public SimpleTransformer() {
    results = new HashSet<>();
  }

  public Collection<AnalysisResult> getAnalysisResults() {
    return results;
  }

  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
    TaintAnalysis analysis = new TaintAnalysis(b);
    analysis.doAnalysis();
    results.addAll(analysis.getResults());
  }

}
