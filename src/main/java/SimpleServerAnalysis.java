import com.ibm.wala.classLoader.Module;

import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.java.WalaClassLoader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import soot.PackManager;
import soot.Transform;
import soot.Transformer;

import magpiebridge.converter.JimpleConverter;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;

/**
 * 
 * @author Linghui Luo
 *
 */
public class SimpleServerAnalysis implements ServerAnalysis {
  private static final Logger LOG = Logger.getLogger("main");
  private Set<String> srcPath;
  private Set<String> libPath;
  private ExecutorService exeService;
  private Future<?> last;

  public SimpleServerAnalysis() {
    exeService = Executors.newSingleThreadExecutor();
  }

  @Override
  public String source() {
    return "Hello World";
  }

  @Override
  public void analyze(Collection<Module> files, MagpieServer server) {

    if (last != null && !last.isDone()) {
      last.cancel(false);
      if (last.isCancelled())
        LOG.info("Susscessfully cancelled last analysis and start new");
    }
    Future<?> future = exeService.submit(new Runnable() {
      @Override
      public void run() {
        setClassPath(server);
        Collection<AnalysisResult> results = Collections.emptyList();
        if (srcPath != null) {
          results = analyze(srcPath, libPath);
        }
        server.consume(results, source());
      }
    });
    last = future;
  }

  /**
   * set up source code path and library path with the project service provided by the server.
   *
   * @param server
   */
  public void setClassPath(MagpieServer server) {
    if (srcPath == null) {
      Optional<IProjectService> opt = server.getProjectService("java");
      if (opt.isPresent()) {
        JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
        Set<Path> sourcePath = ps.getSourcePath();
        if (libPath == null) {
          libPath = new HashSet<>();
          ps.getLibraryPath().stream().forEach(path -> libPath.add(path.toString()));
        }
        if (!sourcePath.isEmpty()) {
          Set<String> temp = new HashSet<>();
          sourcePath.stream().forEach(path -> temp.add(path.toString()));
          srcPath = temp;
        }
      }
    }
  }

  public Collection<AnalysisResult> analyze(Set<String> srcPath, Set<String> libPath) {
    SimpleTransformer transformer = new SimpleTransformer();
    WalaClassLoader loader = new WalaClassLoader(srcPath, libPath, null);
    List<SootClass> sootClasses = loader.getSootClasses();
    JimpleConverter jimpleConverter = new JimpleConverter(sootClasses);
    jimpleConverter.convertAllClasses();
    runSootPacks(transformer);
    Collection<AnalysisResult> results = transformer.getAnalysisResults();
    return results;
  }

  private void runSootPacks(Transformer t) {
    Transform transform = new Transform("jtp.analysis", t);
    PackManager.v().getPack("jtp").add(transform);
    PackManager.v().runBodyPacks();
  }
}
