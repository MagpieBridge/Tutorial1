import java.util.Set;

import soot.Unit;
import soot.Value;

/**
 * 
 * @author Linghui Luo
 *
 */
public class Taint {

  private Unit source;
  private Set<Unit> path;
  private Value value;

  public Taint(Unit source, Set<Unit> path, Value value) {
    this.source = source;
    this.path = path;
    this.value = value;
  }

  public Unit getSource() {
    return source;
  }

  public void setSource(Unit source) {
    this.source = source;
  }

  public Set<Unit> getPath() {
    return path;
  }

  public void setPath(Set<Unit> path) {
    this.path = path;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
