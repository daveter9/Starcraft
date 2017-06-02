package eisbw.percepts.perceivers;

import eis.eis2java.translation.Filter;
//import eis.iilang.Identifier;
//import eis.iilang.Parameter;
import eis.iilang.Percept;
import eisbw.BwapiUtility;
import eisbw.percepts.Attacking;
import eisbw.percepts.Percepts;
import eisbw.units.ConditionHandler;
import eisbw.percepts.FriendlyPercept;
import eisbw.percepts.NewUnitPercept;
import eisbw.percepts.EnemyPercept;
import jnibwapi.JNIBWAPI;
import jnibwapi.Unit;
import jnibwapi.types.UnitType.UnitTypes;

import java.util.HashSet;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Danny & Harm - The perceiver which handles all the unit percepts.
 *
 */
public class UnitsPerceiver extends Perceiver {
	/**
	 * @param api
	 *            The BWAPI.
	 */
	public UnitsPerceiver(JNIBWAPI api) {
		super(api);
	}

	/**
	 * Sets some of the generic Unit percepts.
	 * 
	 * @param units
	 *            The perceived units
	 * @param newunitpercepts
	 *            - list with newUnitPercepts; if this is passed (not null) we
	 *            assume we want friendly units in unitpercepts
	 * @param unitpercepts
	 *            - list with unitPercepts
	 * @param attackingpercepts
	 *            - list with attackingPercepts
	 * @param percepts
	 *            The list of percepts
	 * @param toReturn
	 *            - the map that will be returned
	 */
	private void setUnitPercepts(List<Unit> units, Set<Percept> newunitpercepts, Set<Percept> unitpercepts,
			Set<Percept> attackingpercepts) {
		for (Unit u : units) {
			if (u.isBeingConstructed() && u.isLoaded()) {
				continue; // Fix for the phantom marines bug
			}
			ConditionHandler conditionHandler = new ConditionHandler(api, u);
			if (newunitpercepts != null) {
				String unittype = (u.getType().getID() == UnitTypes.Zerg_Egg.getID()) ? u.getBuildType().getName()
						: BwapiUtility.getUnitType(u);
				unitpercepts.add(new FriendlyPercept(unittype, u.getID(), conditionHandler.getConditions()));
				if(u.isBeingConstructed())
				newunitpercepts.add(new NewUnitPercept(u.getID(), u.getPosition().getBX(), u.getPosition().getBY()));
			} else {
				unitpercepts
						.add(new EnemyPercept(BwapiUtility.getUnitType(u), u.getID(), u.getHitPoints(), u.getShields(),
								conditionHandler.getConditions(), u.getPosition().getBX(), u.getPosition().getBY()));
				if (u.isAttacking() && u.getOrderTarget() != null) {
					attackingpercepts.add(new Attacking(u.getID(), u.getOrderTarget().getID()));
				}
			}
		}
	}

	@Override
	public Map<PerceptFilter, Set<Percept>> perceive(Map<PerceptFilter, Set<Percept>> toReturn) {
		Set<Percept> newunitpercepts = new HashSet<>();
		Set<Percept> friendlypercepts = new HashSet<>();
		Set<Percept> enemypercepts = new HashSet<>();
		Set<Percept> attackingpercepts = new HashSet<>();

		// perceive friendly units
		setUnitPercepts(api.getMyUnits(), newunitpercepts, friendlypercepts, attackingpercepts);
		// perceive enemy units
		setUnitPercepts(api.getEnemyUnits(), null, enemypercepts, attackingpercepts);

		toReturn.put(new PerceptFilter(Percepts.FRIENDLY, Filter.Type.ALWAYS), friendlypercepts);
		toReturn.put(new PerceptFilter(Percepts.ENEMY, Filter.Type.ALWAYS), enemypercepts);
		toReturn.put(new PerceptFilter(Percepts.ATTACKING, Filter.Type.ALWAYS), attackingpercepts);
		toReturn.put(new PerceptFilter(Percepts.NEWUNIT, Filter.Type.ON_CHANGE), newunitpercepts);

		return toReturn;
	}
}
