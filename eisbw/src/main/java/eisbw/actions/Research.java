package eisbw.actions;

import eis.exceptions.ActException;
import eis.iilang.*;

import java.util.LinkedList;

import jnibwapi.*;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;

public class Research extends StarcraftAction {

    public Research(JNIBWAPI api) {
        super(api);
    }

    @Override
    public boolean isValid(Action action) {
        LinkedList<Parameter> parameters = action.getParameters();
        if (parameters.size() == 1) { // type
            boolean isTechType = parameters.get(0) instanceof Identifier && utility.getTechType(((Identifier) parameters.get(0)).getValue()) != null;
            return isTechType;
        }

        return false;
    }

    @Override
    public boolean canExecute(Unit unit, Action action) {
        LinkedList<Parameter> parameters = action.getParameters();
        TechType techType = utility.getTechType(((Identifier) parameters.get(0)).getValue());

        return !techType.isTargetsPosition() && !techType.isTargetsUnits();
    }

    @Override
    public void execute(Unit unit, Action action) throws ActException {
        LinkedList<Parameter> parameters = action.getParameters();
        TechType techType = utility.getTechType(((Identifier) parameters.get(0)).getValue());
        unit.research(techType);
    }

    @Override
    public String toString() {
        return "research(techId)";
    }
}