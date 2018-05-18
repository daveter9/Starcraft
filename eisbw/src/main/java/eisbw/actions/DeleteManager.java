package eisbw.actions;

import java.util.List;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eisbw.Game;
import jnibwapi.JNIBWAPI;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class DeleteManager extends StarcraftAction {
	private final Game game;
	
	/**
	 * The DeleteManager constructor.
	 *
	 * @param api
	 *            The BWAPI
	 */
	public DeleteManager(JNIBWAPI api, Game game) {
		super(api);
		this.game = game;
	}
	
	@Override
	public boolean isValid(Action action) {
		List<Parameter> parameters = action.getParameters();
		/* TODO: check if given parameter is a manager */
		return parameters.size() == 1; 
	}
	
	@Override
	public void canExecute(UnitType type, Action action) {
		return true;
	}
	
	@Override
	public void execute(Unit unit, Action action) {
		List<Parameter> parameters = action.getParameters();
		this.game.deleteManager(((Identifier) parameters.get(0)).getValue());
	}
	
	@Override
	public String toString() {
		return "deleteManager";
	}
}
