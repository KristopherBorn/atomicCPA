package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.imotep.featuremodel.variability.metamodel.FeatureModel.FeatureModelPackage;

public class CpaOnFeatureModelRunnerWithoutNACsWithoutUpperLimitOnReferencesNoAttrChangeAllMultiRules extends Runner{
	
	// Relative path to the transformations.
//	static String TRANSFORMATIONS = "transformations/"; //überflüssig
	
//	private static Engine engine;// = new EngineImpl(); //überflüssig
	
//	private static Module module; //überflüssig
	
//	private static HenshinResourceSet henshinResourceSet; // überflüssig

	public static void main(String args[]){
		System.out.println("test");
		
		
		FeatureModelPackage.eINSTANCE.eClass();

		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());

		ResourceSetImpl resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		
		List<String> deactivatedRules = new LinkedList<String>();
//		deactivatedRules.add("Refactoring_1-4"); // bringt ähnlich gute ERgebnisse wie die anderen Refactorings, ist daher also bei Bedarf verzichtbar
		deactivatedRules.add("Refactoring_1-9"); // most likely DEADLOCK in Atmoic CPA!
		deactivatedRules.add("deleteGroup_IN_FeatureModel"); // most likely DEADLOCK in Atmoic CPA!
//		deactivatedRules.add("Generalization_2-1"); // most likely DEADLOCK in Atmoic CPA!
		deactivatedRules.add("Generalization_2-2"); // most likely DEADLOCK in Atmoic CPA!
		deactivatedRules.add("Specialization_3-1"); // most likely DEADLOCK in Atmoic CPA!
		deactivatedRules.add("PushDownGroup"); // most likely DEADLOCK in Atmoic CPA!

//		deactivatedRules.add("Specialization_3-6"); // Ram läuft voll.
		
		Set<String> limitedSetOfRulesByRuleNames = new HashSet<String>();
		limitedSetOfRulesByRuleNames.add("Generalization_2-1");
		limitedSetOfRulesByRuleNames.add("Generalization_2-1_");
		
		final File f = new File(Runner.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String filePath = f.toString();

		String projectPath = filePath.replaceAll("bin", "");
		System.out.println(projectPath);
		String subDirectoryPath = "testData\\featureModelingWithoutUpperLimitsOnReferences\\fmedit_noNACs_noAttrChange\\rules\\";
		String fullSubDirectoryPath = projectPath + subDirectoryPath;
		
		Runner runner = new Runner();
		runner.setNoApplicationConditions(true);
		runner.setNoMultirules(true);
		runner.setAnalysisKinds(false, false, true);
		runner.limitSetOfRulesByRuleNames(limitedSetOfRulesByRuleNames);
		runner.run(fullSubDirectoryPath, deactivatedRules);
	}
}
