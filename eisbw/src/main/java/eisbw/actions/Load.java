package eisbw.actions;

import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import jnibwapi.JNIBWAPI;
import jnibwapi.Unit;

import java.util.LinkedList;

public class Load extends StarcraftAction {

  public Load(JNIBWAPI api) {
    super(api);
  }

  @Override
  public boolean isValid(Action action) {
    LinkedList<Parameter> parameters = action.getParameters();
    return parameters.size() == 1 && parameters.get(0) instanceof Numeral;
  }

  @Override
  public boolean canExecute(Unit unit, Action action) {
    return unit.getType().getSpaceProvided() > 0;
  }

  @Override
  public void execute(Unit unit, Action action) throws ActException {
    Unit target = api.getUnit(((Numeral) action.getParameters().get(0)).getValue().intValue());
    boolean res = unit.load(target, false);
    if (!res) {
      throw new ActException("Couldn't load " + unit.getType().getName());
    }
  }

  @Override
  public String toString() {
    return "load(Target)";
  }
}
