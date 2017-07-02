package eisbw.debugger.draw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eis.eis2java.exception.TranslationException;
import eisbw.BwapiUtility;
import eisbw.Game;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Position.PosType;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.util.BWColor;

/**
 * @author Danny & Harm - The class which handles the drawing of the buildings
 *         of the dev. tool.
 *
 */
public class DrawUnitInfo extends IDraw {
	private final static int barHeight = 18;
	private final static BWColor barColor = BWColor.Blue;
	private final Set<Unit> alive = new HashSet<>();
	private final Map<Integer, Integer> dead = new HashMap<>();

	/**
	 * The DrawBuildingDetails constructor.
	 *
	 * @param game
	 *            The current game.
	 */
	public DrawUnitInfo(Game game) {
		super(game);
	}

	@Override
	protected void drawOnMap(JNIBWAPI api) throws TranslationException {
		drawTimerInfo(api);
		drawHealth(api);
		drawTargets(api);
		drawIDs(api);
		drawUnitInformation(api, 440, 6);
		drawAgentCount(api);
	}

	/**
	 * Draws remaining research/upgrade times; unit building/training is already
	 * covered by the health drawing
	 */
	private void drawTimerInfo(JNIBWAPI api) {
		for (final Unit unit : api.getMyUnits()) {
			if (!BwapiUtility.isValid(unit)) {
				continue;
			}
			int total = 0;
			int done = 0;
			String txt = "";
			if (unit.getRemainingResearchTime() > 0) {
				total = unit.getTech().getResearchTime();
				done = total - unit.getRemainingResearchTime();
				txt = unit.getTech().getName();
			}
			if (unit.getRemainingUpgradeTime() > 0) {
				total = unit.getUpgrade().getUpgradeTimeBase();
				done = total - unit.getRemainingUpgradeTime();
				txt = unit.getUpgrade().getName();
			}
			if (total > 0) {
				int width = unit.getType().getTileWidth() * 32;
				Position start = new Position(unit.getPosition().getPX() - width / 2, unit.getPosition().getPY() - 30);
				api.drawBox(start, new Position(start.getPX() + width, start.getPY() + barHeight), barColor, false,
						false);
				int progress = (int) ((double) done / (double) total * width);
				api.drawBox(start, new Position(start.getPX() + progress, start.getPY() + barHeight), barColor, true,
						false);
				api.drawText(new Position(start.getPX() + 5, start.getPY() + 2), txt, false);
			}
		}
	}

	/**
	 * Draws health boxes for units (ported from JNIBWAPI native code); added a
	 * max>0 check to prevent crashes on spell units (with health 255)
	 */
	private void drawHealth(JNIBWAPI api) {
		for (final Unit unit : api.getAllUnits()) {
			if (!BwapiUtility.isValid(unit)) {
				continue;
			}
			int health = unit.getHitPoints();
			int max = unit.getType().getMaxHitPoints();
			if (health > 0 && max > 0) {
				int x = unit.getPosition().getPX();
				int y = unit.getPosition().getPY();
				int l = unit.getType().getDimensionLeft();
				int t = unit.getType().getDimensionUp();
				int r = unit.getType().getDimensionRight();
				int b = unit.getType().getDimensionDown();
				int width = ((r + l) * health) / max;
				if (health * 3 < max) {
					api.drawBox(new Position(x - l, y - t - 5), new Position(x - l + width, y - t), BWColor.Red, true,
							false);
				} else if (health * 3 < 2 * max) {
					api.drawBox(new Position(x - l, y - t - 5), new Position(x - l + width, y - t), BWColor.Yellow,
							true, false);
				} else {
					api.drawBox(new Position(x - l, y - t - 5), new Position(x - l + width, y - t), BWColor.Green, true,
							false);
				}
				boolean self = (unit.getPlayer().getID() == api.getSelf().getID());
				api.drawBox(new Position(x - l, y - t - 5), new Position(x + r, y - t),
						self ? BWColor.White : BWColor.Red, false, false);
				api.drawBox(new Position(x - l, y - t), new Position(x + r, y + b), self ? BWColor.White : BWColor.Red,
						false, false);
				api.drawText(new Position(x - l, y - t), unit.getType().getName(), false);
			}
		}
	}

	/**
	 * Draws the targets of each unit. (ported from JNIBWAPI native code)
	 */
	private void drawTargets(JNIBWAPI api) {
		for (final Unit unit : api.getAllUnits()) {
			if (!BwapiUtility.isValid(unit)) {
				continue;
			}
			boolean self = (unit.getPlayer().getID() == api.getSelf().getID());
			Unit target = (unit.getTarget() == null) ? unit.getOrderTarget() : unit.getTarget();
			if (target != null) {
				api.drawLine(unit.getPosition(), target.getPosition(), self ? BWColor.Yellow : BWColor.Purple, false);
			}
			Position position = unit.getTargetPosition();
			if (position != null) {
				api.drawLine(unit.getPosition(), position, self ? BWColor.Yellow : BWColor.Purple, false);
			}
		}
	}

	/**
	 * Draws the IDs of each unit. (ported from JNIBWAPI native code)
	 */
	private void drawIDs(JNIBWAPI api) {
		for (final Unit unit : api.getAllUnits()) {
			if (!BwapiUtility.isValid(unit)) {
				continue;
			}
			api.drawText(unit.getPosition(), Integer.toString(unit.getID()), false);
		}
	}

	/**
	 * Draws a list of all unit types, counting how many are still alive and how
	 * many have died (ported from native code of the tournament manager)
	 */
	private void drawUnitInformation(JNIBWAPI api, int x, int y) {
		api.drawText(new Position(x, y + 20), api.getSelf().getName() + "'s Units", true);
		api.drawText(new Position(x + 160, y + 20), "#", true);
		api.drawText(new Position(x + 180, y + 20), "X", true);

		Map<Integer, Integer> count = new HashMap<>();
		Set<Unit> previous = new HashSet<>(this.alive);
		this.alive.clear();
		for (final Unit unit : api.getMyUnits()) {
			if (!BwapiUtility.isValid(unit)) {
				continue;
			}
			this.alive.add(unit);
			int type = unit.getType().getID();
			if (type == UnitTypes.Terran_Siege_Tank_Siege_Mode.getID()) {
				type = UnitTypes.Terran_Siege_Tank_Tank_Mode.getID();
			}
			if (count.containsKey(type)) {
				count.put(type, count.get(type).intValue() + 1);
			} else {
				count.put(type, 1);
			}
		}
		previous.removeAll(this.alive);
		for (final Unit unit : previous) {
			int type = unit.getType().getID();
			if (type == UnitTypes.Terran_Siege_Tank_Siege_Mode.getID()) {
				type = UnitTypes.Terran_Siege_Tank_Tank_Mode.getID();
			}
			if (this.dead.containsKey(type)) {
				this.dead.put(type, this.dead.get(type).intValue() + 1);
			} else {
				this.dead.put(type, 1);
			}
		}

		int yspace = 0;
		for (final UnitType type : UnitTypes.getAllUnitTypes()) {
			int t = type.getID();
			int livecount = count.containsKey(t) ? count.get(t).intValue() : 0;
			int deadcount = this.dead.containsKey(t) ? this.dead.get(t).intValue() : 0;
			if (livecount > 0 || deadcount > 0) {
				api.drawText(new Position(x, y + 40 + ((yspace) * 10)), BwapiUtility.getName(type), true);
				api.drawText(new Position(x + 160, y + 40 + ((yspace) * 10)), Integer.toString(livecount), true);
				api.drawText(new Position(x + 180, y + 40 + ((yspace++) * 10)), Integer.toString(deadcount), true);
			}
		}
	}

	private void drawAgentCount(JNIBWAPI api) {
		api.drawText(new Position(10, 10, PosType.PIXEL), "Agentcount: " + this.game.getAgentCount(), true);
	}
}