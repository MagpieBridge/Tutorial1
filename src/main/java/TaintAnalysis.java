import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;

import de.upb.soot.jimple.basic.PositionInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.DiagnosticSeverity;

import soot.Body;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import magpiebridge.converter.PositionInfoTag;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.Kind;
import magpiebridge.util.SourceCodeReader;

/**
 * A very simple intra-procedural taint analysis.
 * 
 * @author Linghui Luo
 *
 */
public class TaintAnalysis extends ForwardFlowAnalysis<Unit, Set<Taint>> {

  private Collection<AnalysisResult> results;

  public TaintAnalysis(Body body) {
    super(new ExceptionalUnitGraph(body));
    this.results = new HashSet<>();
  }

  @Override
  protected void flowThrough(Set<Taint> in, Unit unit, Set<Taint> out) {
    out.addAll(in);
    if (unit instanceof AssignStmt) {
      AssignStmt assignStmt = (AssignStmt) unit;
      Value leftOp = assignStmt.getLeftOp();
      Value rightOp = assignStmt.getRightOp();
      for (Taint t : in) {
        if (t.getValue().equals(rightOp)) {
          Set<Unit> path = t.getPath();
          path.add(unit);
          Taint newTaint = new Taint(t.getSource(), path, leftOp);
          out.add(newTaint);
        }
      }

      if (rightOp instanceof InvokeExpr) {
        InvokeExpr invoke = (InvokeExpr) rightOp;
        String sig = invoke.getMethod().getSignature();
        if (sig.equals("<Demo: java.lang.String source()>")) {
          Set<Unit> path = new HashSet<>();
          path.add(unit);
          Taint newTaint = new Taint(unit, path, leftOp);
          out.add(newTaint);
        }
        if (sig.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
          for (Taint t : in) {
            if (invoke.getArgs().contains(t.getValue())) {
              Set<Unit> path = t.getPath();
              path.add(unit);
              Taint newTaint = new Taint(t.getSource(), path, leftOp);
              out.add(newTaint);
            }
          }
        }
        if (sig.equals("<java.lang.StringBuilder: java.lang.StringBuilder toString()>")) {
          for (Taint t : in) {
            VirtualInvokeExpr virtualInvoke = (VirtualInvokeExpr) invoke;
            if (virtualInvoke.getBase().equals(t.getValue())) {
              Set<Unit> path = t.getPath();
              path.add(unit);
              Taint newTaint = new Taint(t.getSource(), path, leftOp);
              out.add(newTaint);
            }
          }
        }
      }

    }

    if (unit instanceof InvokeStmt) {
      InvokeStmt invokeStmt = (InvokeStmt) unit;
      String sig = invokeStmt.getInvokeExpr().getMethod().getSignature();
      if (sig.equals("<Demo: void sink(java.lang.String)>")) {
        for (Taint taint : in) {
          if (invokeStmt.getInvokeExpr().getArgs().contains(taint.getValue())) {
            PositionInfo sinkPos = ((PositionInfoTag) unit.getTag("PositionInfoTag")).getPositionInfo();
            List<Pair<Position, String>> relatedInfo = new ArrayList<>();
            try {
              for (Unit n : taint.getPath()) {
                PositionInfoTag tag = (PositionInfoTag) n.getTag("PositionInfoTag");
                if (tag != null) {
                  Position stmtPos = tag.getPositionInfo().getStmtPosition();
                  String code = SourceCodeReader.getLinesInString(stmtPos);
                  Pair<Position, String> pair = Pair.make(stmtPos, code);
                  if (!relatedInfo.contains(pair))
                    relatedInfo.add(pair);
                }
              }
              String sinkCode = SourceCodeReader.getLinesInString(sinkPos.getStmtPosition());
              StringBuilder str = new StringBuilder("Found a sensitive flow to sink ");
              str.append("[" + sinkCode + "]");
              str.append(" from source ");
              PositionInfo sourcePos = ((PositionInfoTag) taint.getSource().getTag("PositionInfoTag")).getPositionInfo();
              String sourceCode = SourceCodeReader.getLinesInString(sourcePos.getStmtPosition());
              str.append("[" + sourceCode + "].");
              this.results.add(new Result(Kind.Diagnostic, sinkPos.getStmtPosition(), str.toString(), relatedInfo,
                  DiagnosticSeverity.Error, null, sinkCode));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
    }

  }

  public void doAnalysis() {
    super.doAnalysis();
  }

  public Collection<AnalysisResult> getResults() {
    return results;
  }

  @Override
  protected Set<Taint> newInitialFlow() {
    return new HashSet<>();
  }

  @Override
  protected void merge(Set<Taint> in1, Set<Taint> in2, Set<Taint> out) {
    for (Taint t : in1) {
      if (!out.contains(t))
        out.add(t);
    }

    for (Taint t : in2) {
      if (!out.contains(t))
        out.add(t);
    }
  }

  @Override
  protected void copy(Set<Taint> source, Set<Taint> dest) {
    for (Taint t : source) {
      dest.add(new Taint(t.getSource(), t.getPath(), t.getValue()));
    }
  }

}
