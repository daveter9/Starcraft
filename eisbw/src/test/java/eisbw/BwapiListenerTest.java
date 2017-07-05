package eisbw;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import bwapi.AIModule;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eisbw.debugger.DebugWindow;
import eisbw.units.StarcraftUnitFactory;
import eisbw.units.Units;

public class BwapiListenerTest {
	private BwapiListener listener;
	private List<Unit> list;

	@Mock
	private Game game;
	@Mock
	private Units units;
	@Mock
	private Mirror mirror;
	@Mock
	private AIModule module;
	@Mock
	private bwapi.Game bwapi;
	@Mock
	private Unit unit;
	@Mock
	private UnitType unitType;
	@Mock
	private DebugWindow debugwindow;
	@Mock
	private Player self;

	/**
	 * Init mocks.
	 */
	@Before
	public void start() {
		MockitoAnnotations.initMocks(this);
		when(this.unit.getID()).thenReturn(0);
		when(this.unit.getType()).thenReturn(this.unitType);
		when(this.unitType.toString()).thenReturn("Terran Siege Tank Tank Mode");
		when(this.units.getUnitName(0)).thenReturn("unit");
		when(this.units.getUnit("unit")).thenReturn(this.unit);
		when(this.game.getUnits()).thenReturn(this.units);
		this.list = new ArrayList<>(1);
		this.list.add(this.unit);
		when(this.self.getUnits()).thenReturn(this.list);
		when(this.bwapi.self()).thenReturn(this.self);
		when(this.bwapi.getUnit(0)).thenReturn(this.unit);
		this.listener = new BwapiListener(this.game, "", false, false, false, false, 200);
		this.listener.mirror = this.mirror;
		when(this.mirror.getModule()).thenReturn(this.module);
		this.listener.api = this.bwapi;
		this.listener.actionProvider.loadActions(this.bwapi, this.game);
	}

	@Test
	public void isSupportedByEntity_test() {
		assertTrue(this.listener.isSupportedByEntity(new Action("stop"), "unit"));
		eis.iilang.Parameter[] list = new eis.iilang.Parameter[1];
		list[0] = new Identifier("fail");
		assertFalse(this.listener.isSupportedByEntity(new Action("stop", list), "unit"));
		assertFalse(this.listener.isSupportedByEntity(new Action("setRallyPoint", list), "unit"));
		list[0] = new Numeral(1);
		assertFalse(this.listener.isSupportedByEntity(new Action("setRallyPoint", list), "unit"));
		when(this.unitType.isBuilding()).thenReturn(true);
		assertTrue(this.listener.isSupportedByEntity(new Action("setRallyPoint", list), "unit"));
	}

	@Test
	public void unitComplete_test() {
		when(this.units.getUnitName(0)).thenReturn("unit");
		this.listener.onUnitComplete(this.unit);
		verify(this.units, times(1)).addUnit(eq(this.unit), any(StarcraftUnitFactory.class));
		when(this.self.getUnits()).thenReturn(new ArrayList<Unit>(0));
		this.listener.onUnitComplete(this.unit);
		verify(this.units, times(1)).addUnit(eq(this.unit), any(StarcraftUnitFactory.class));
		when(this.units.getUnitName(0)).thenReturn(null);
		this.listener.onUnitComplete(this.unit);
		verify(this.units, times(1)).addUnit(eq(this.unit), any(StarcraftUnitFactory.class));
	}

	@Test
	public void unitMorph_test() {
		when(this.bwapi.self()).thenReturn(this.self);
		when(this.self.getRace()).thenReturn(Race.Zerg);
		when(this.units.getUnitName(0)).thenReturn("unit");
		when(this.units.deleteUnit("unit")).thenReturn(this.unit);
		this.listener.onUnitMorph(this.unit);
		verify(this.units, times(1)).addUnit(eq(this.unit), any(StarcraftUnitFactory.class));
		when(this.self.getUnits()).thenReturn(new ArrayList<Unit>(0));
		this.listener.onUnitMorph(this.unit);
		verify(this.units, times(1)).addUnit(eq(this.unit), any(StarcraftUnitFactory.class));
	}

	@Test
	public void unitDestroy_test() {
		when(this.units.getUnitName(0)).thenReturn(null);
		this.listener.onUnitDestroy(this.unit);
		verify(this.units, times(0)).deleteUnit(any(String.class));
		when(this.units.getUnitName(0)).thenReturn("unit");
		when(this.units.deleteUnit("unit")).thenReturn(this.unit);
		this.listener.onUnitDestroy(this.unit);
		verify(this.units, times(1)).deleteUnit(any(String.class));
	}

	// @Test (FIXME: native)
	public void matchStart_test() {
		this.listener.onStart();
		verify(this.bwapi, times(0)).setLocalSpeed(30);
		verify(this.bwapi, times(1)).setLocalSpeed(5);
		this.listener.onEnd(true);
		verify(this.game, times(1)).clean();
		this.listener.onStart();
		this.listener.debugwindow = this.debugwindow;
		this.listener.onEnd(true);
		verify(this.game, times(2)).clean();
	}

	@Test
	public void matchFrame_test() throws ActException {
		this.listener.pendingActions.add(new BwapiAction(this.unit, new Action("stub1")));
		this.listener.pendingActions.add(new BwapiAction(this.unit, new Action("stub2")));
		this.listener.pendingActions.add(new BwapiAction(this.unit, new Action("stub3")));
		this.listener.pendingActions.add(new BwapiAction(this.unit, new Action("stub4")));
		this.listener.onFrame();
		verify(this.game, times(0)).updateConstructionSites(this.bwapi);
		this.listener.count = 49;
		this.listener.onFrame();
		assertTrue(this.listener.count == 50);
		verify(this.game, times(1)).updateConstructionSites(this.bwapi);
		this.listener.performEntityAction("unit", new Action("stop"));
		eis.iilang.Parameter[] list = new eis.iilang.Parameter[1];
		list[0] = new Identifier("fail");
		this.listener.performEntityAction("unit", new Action("setRallyPoint", list));
		when(this.unit.isBeingConstructed()).thenReturn(true);
		this.listener.performEntityAction("unit", new Action("stop"));
		assertTrue(this.listener.pendingActions.size() == 1);
		this.listener.debugwindow = this.debugwindow;
		this.listener.onFrame();
		assertTrue(this.listener.pendingActions.size() == 0);
		verify(this.debugwindow, times(1)).debug(this.bwapi);
	}
}
