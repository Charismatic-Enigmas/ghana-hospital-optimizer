package com.ghdsa.algorithms;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DPRequestSelectionTest {

    @Test
    void normalCase_choosesHighestValueWithinBudget() {
        List<DPRequestSelection.Request> requests = List.of(
            new DPRequestSelection.Request("r1", 10, 60),
            new DPRequestSelection.Request("r2", 20, 100),
            new DPRequestSelection.Request("r3", 30, 120)
        );
        DPRequestSelection.Result result = DPRequestSelection.solve(requests, 50);
        // best combo within 50 minutes is r2+r3 (20+30=50 min, 100+120=220 value)
        assertEquals(220, result.totalValue());
        assertEquals(2, result.chosen().size());
    }

    @Test
    void boundaryCase_zeroBudgetSelectsNothing() {
        List<DPRequestSelection.Request> requests = List.of(
            new DPRequestSelection.Request("r1", 10, 60)
        );
        DPRequestSelection.Result result = DPRequestSelection.solve(requests, 0);
        assertEquals(0, result.totalValue());
        assertTrue(result.chosen().isEmpty());
    }

    @Test
    void boundaryCase_emptyRequestList() {
        DPRequestSelection.Result result = DPRequestSelection.solve(List.of(), 100);
        assertEquals(0, result.totalValue());
        assertTrue(result.chosen().isEmpty());
    }

    @Test
    void invalidInput_singleRequestExceedingBudgetIsExcluded() {
        List<DPRequestSelection.Request> requests = List.of(
            new DPRequestSelection.Request("too-big", 1000, 500)
        );
        DPRequestSelection.Result result = DPRequestSelection.solve(requests, 10);
        assertEquals(0, result.totalValue());
    }
}
