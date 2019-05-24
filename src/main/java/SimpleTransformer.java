import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Scene;
import soot.options.Options;

import magpiebridge.core.AnalysisResult;

public class SimpleTransformer extends BodyTransformer {

  private Collection<AnalysisResult> results;

  public SimpleTransformer() {
    results = new HashSet<>();
    initilizeSootOptions();
  }

  private void initilizeSootOptions() {
    G.v().reset();
    // Options.v().setPhaseOption("cg.spark", "on");
    // Options.v().setPhaseOption("cg", "all-reachable:true");
    Options.v().set_output_format(Options.output_format_none);
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_full_resolver(true);
    Scene.v().loadNecessaryClasses();
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
