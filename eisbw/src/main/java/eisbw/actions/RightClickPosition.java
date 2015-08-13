package eisbw.actions;

import eis.exceptions.ActException;
import eis.iilang.*;
import java.util.LinkedList;
import jnibwapi.*;
import jnibwapi.types.UnitType;

public class RightClickPosition extends StarcraftAction {

    public RightClickPosition(JNIBWAPI api) {
        super(api);
    }

    @Override
    public boolean isValid(Action action) {
        LinkedList<Parameter> parameters = action.getParameters();
        return (parameters.size() == 2 && parameters.get(0) instanceof Numeral && parameters.get(1) instanceof Numeral);
    }

    @Override
    public boolean canExecute(Unit unit, Action action) {
        return true;
    }

    @Override
    public void execute(Unit unit, Action action) throws ActException {
        LinkedList<Parameter> parameters = action.getParameters();
        int x = ((Numeral) parameters.get(0)).getValue().intValue();
        int y = ((Numeral) parameters.get(1)).getValue().intValue();
        
		Position pos = new Position(x, y, Position.PosType.BUILD);
		unit.rightClick(pos, false);
    }

    @Override
    public String toString() {
        return "move(unitId, x, y)"; //To change body of generated methods, choose Tools | Templates.
    }
}