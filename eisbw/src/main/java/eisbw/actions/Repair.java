package eisbw.actions;

import java.util.List;

import eis.iilang.Action;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import jnibwapi.JNIBWAPI;
import jnibwapi.Unit;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType;

public class Repair extends StarcraftAction {
	public Repair(JNIBWAPI api) {
		super(api);
	}

	@Override
	public boolean isValid(Action action) {
		List<Parameter> parameters = action.getParameters();
		return parameters.size() == 1 && parameters.get(0) instanceof Numeral;
	}

	@Override
	public boolean canExecute(UnitType type, Action action) {
		return type.isWorker() && type.getRaceID() == RaceTypes.Terran.getID();
	}

	@Override
	public void execute(Unit unit, Action action) {
		List<Parameter> parameters = action.getParameters();
		int targetId = ((Numeral) parameters.get(0)).getValue().intValue();
		Unit target = this.api.getUnit(targetId);

		if (target != null && target.isCompleted()) {
			unit.repair(target, false);
		} else {
			unit.rightClick(target, false);
		}
	}

	@Override
	public String toString() {
		return "repair(TargetID)";
	}
}
