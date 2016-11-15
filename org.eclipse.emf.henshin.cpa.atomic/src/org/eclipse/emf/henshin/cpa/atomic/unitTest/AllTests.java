package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ComputeCandidatesTest.class, ComputeConflictAtomsTest.class, ComputeMinReasonsTest.class,
		EnumerateDisjointCombinationsTest.class, FindDanglingEdgesTest.class, FindFixingEdgesTest.class,
		IntegrationTest.class, PushoutTests.class, RobustnessTest.class })
public class AllTests {

}
