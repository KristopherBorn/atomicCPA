package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.UnsupportedRuleException;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.cpa.result.Conflict;
import org.eclipse.emf.henshin.cpa.result.ConflictKind;
import org.eclipse.emf.henshin.cpa.result.CriticalPair;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

import de.imotep.featuremodel.variability.metamodel.FeatureModel.FeatureModelPackage;

public class Runner {
	
	private List<String> pathsToHenshinFiles;
	
	private List<String> deactivatedRules;


	public void run() {
		
		FeatureModelPackage.eINSTANCE.eClass();

	    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("xmi", new XMIResourceFactoryImpl());
	    
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", 
		new EcoreResourceFactoryImpl());
		
//		resourceSet.getURIConverter().getURIMap().put(URI.createPlatformPluginURI("/org.eclipse.xsd/",true), URI.createURI(XSDPlugin.INSTANCE.getBaseURL().toExternalForm()));
//		Resource xsdEcoreResource = 
//		resourceSet.getResource(URI.createPlatformPluginURI("/org.eclipse.xsd/model/XSD.ecore", 
//		true), true);
//		xsdEcoreResource.save(System.out, null);
		
		
		deactivatedRules = new LinkedList<String>();
		deactivatedRules.add("Refactoring_1-4");
		deactivatedRules.add("Refactoring_1-9");	
		deactivatedRules.add("deleteGroup_IN_FeatureModel");
		deactivatedRules.add("removeFromGroup_features_Feature");
		deactivatedRules.add("PushDownGroup");
		
		Logger logger = new Logger();
		
		
		final File f = new File(Runner.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		
		String filePath = f.toString();
//		String shortendPath = filePath.replaceAll("org.eclipse.emf.henshin.cpa.atomic\\bin", "");
		String projectPath = filePath.replaceAll("bin", "");
//		String shortendPath0 = filePath.replaceAll("bin", "");
//		 String shortendPath1 = shortendPath0.substring(0, shortendPath0.length()-1);
//		 String projectPath = shortendPath1.replaceAll("org.eclipse.emf.henshin.cpa.atomic", "");
//		String shortendPath = filePath.replaceAll("bin", "");
		
		
		System.out.println(projectPath);
		
		String subDirectoryPath = "testData\\featureModeling\\fmedit_noAmalgamation\\rules\\";
		
		String fullSubDirectoryPath = projectPath+ subDirectoryPath;
		
				
		pathsToHenshinFiles = new LinkedList<String>();
		
		File dir = new File(fullSubDirectoryPath);
		  inspectDirectoryForHenshinFiles(dir);
		  
		  List<Rule> allEditRulesWithoutAmalgamation = new LinkedList<Rule>();
		  
		  int numberOfAddedRules = 0;
		  for(String pathToHenshinFiles : pathsToHenshinFiles){
				HenshinResourceSet henshinResourceSet = new HenshinResourceSet();
				Module module = henshinResourceSet.getModule(pathToHenshinFiles);
				for (Unit unit : module.getUnits()) {
					if (unit instanceof Rule /*&& numberOfAddedRules<10*/) {
//						rulesAndAssociatedFileNames.put((Rule) unit, fileName);
						boolean deactivatedRule = false;
						for(String deactivatedRuleName : deactivatedRules){							
							if(unit.getName().contains(deactivatedRuleName))
								deactivatedRule = true;
						}
						if(!deactivatedRule){							
							allEditRulesWithoutAmalgamation.add((Rule) unit);
							numberOfAddedRules++;
						}
					}
				}
		  }
		  
//		System.out.println("HALT");
		
		
		logger.init(numberOfAddedRules);
		
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		
		// options to turn on and off different analyses
		boolean runClassicCPA = true;
		boolean runEssentialCPA = true;
		boolean runAtomicAnalysis = true;
		logger.setAnalysisKinds(runClassicCPA, runEssentialCPA, runAtomicAnalysis);
		
		// essential CPA setup
		ICriticalPairAnalysis essentialCpa = new CpaByAGG();
		CPAOptions essentialOptions = new CPAOptions();
		essentialOptions.setEssential(true);
		
		// classic CPA setup
		//TODO!!!
		
			for(Rule firstRule : allEditRulesWithoutAmalgamation){
					for(Rule secondRule : allEditRulesWithoutAmalgamation){	
						
						StringBuffer runTimesOfRuleCombination = new StringBuffer();
						StringBuffer amountOfDeleteUseConflictsOfRulecombination = new StringBuffer();
						
						
						if(runClassicCPA){
							runTimesOfRuleCombination.append("?");
							runTimesOfRuleCombination.append("/");
							
							amountOfDeleteUseConflictsOfRulecombination.append("?");
							amountOfDeleteUseConflictsOfRulecombination.append("/");
						}
						
						
						if(runEssentialCPA){
							long essentialStartTime = System.currentTimeMillis();
							CPAResult essentialResult = null;
							try {
								List<Rule> firstRuleList = new LinkedList<Rule>();
								firstRuleList.add(firstRule);
								List<Rule> secondRuleList = new LinkedList<Rule>();
								secondRuleList.add(secondRule);
								essentialCpa.init(firstRuleList, secondRuleList, essentialOptions);
								essentialStartTime = System.currentTimeMillis();
								essentialResult = essentialCpa.runConflictAnalysis();
							} catch (UnsupportedRuleException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							long essentialEndTime = System.currentTimeMillis();
							long essentialRunTime = essentialEndTime-essentialStartTime;
							
							runTimesOfRuleCombination.append(String.valueOf(essentialRunTime));
							runTimesOfRuleCombination.append("/");
							
							if(essentialResult != null){
								List<CriticalPair> filteredDeleteUseConflicts = filterDeleteUseConflicts(essentialResult);
								System.err.println("delete-use-conflicts: "+filteredDeleteUseConflicts.size());
	//							System.out.println("complete runtime: "+completeRunTime);
	
								amountOfDeleteUseConflictsOfRulecombination.append(String.valueOf(filteredDeleteUseConflicts.size()));
								amountOfDeleteUseConflictsOfRulecombination.append("/");
							}else {
								amountOfDeleteUseConflictsOfRulecombination.append("-");
								amountOfDeleteUseConflictsOfRulecombination.append("/");
							}
						}
						
						
						if(runAtomicAnalysis){
//							List<String> shortResults = new LinkedList<String>();
							int numberOfAnalysis = 0;
							int numberOfConflictsOverall = 0;
							String ruleCombination = firstRule.getName()+" -> "+secondRule.getName();
							System.out.println("start combination: "+ruleCombination);
							long atomicStartTime = System.currentTimeMillis();
							List<ConflictAtom> computeConflictAtoms = atomicCoreCPA.computeConflictAtoms(firstRule, secondRule);	
							long atomicEndTime = System.currentTimeMillis();
							long atomiRunTime = atomicEndTime-atomicStartTime;

							runTimesOfRuleCombination.append(String.valueOf(atomiRunTime));
							
							System.out.println("executed: "+ruleCombination+" del-use-confl: "+computeConflictAtoms.size()+" in "+atomiRunTime+" ms");
							numberOfAnalysis++;
							System.err.println("number of analysis: "+numberOfAnalysis);
//							shortResults.add(computeConflictAtoms.size()+" conflicts in "+ruleCombination);
							numberOfConflictsOverall += computeConflictAtoms.size();

							amountOfDeleteUseConflictsOfRulecombination.append(String.valueOf(computeConflictAtoms.size()));
						}

						logger.addData(firstRule, secondRule, runTimesOfRuleCombination.toString(), amountOfDeleteUseConflictsOfRulecombination.toString());
					
//				}
			}

//			long completeEndTime = System.currentTimeMillis();
//			long completeRunTime = completeEndTime-completeStartTime;
//			for (String shortResultString : shortResults) {			
//				System.out.println(shortResultString);
//			}
//			System.out.println("number of conflicts overall: "+numberOfConflictsOverall);
//			System.out.println("complete runtime: "+completeRunTime);
		}
		
		CPAResult runConflictAnalysis = null;
		if(runEssentialCPA){
			ICriticalPairAnalysis cpa = new CpaByAGG();
			CPAOptions options = new CPAOptions();
			options.setEssential(true);
			long completeStartTime = System.currentTimeMillis();
			try {
				cpa.init(allEditRulesWithoutAmalgamation, options);
				completeStartTime = System.currentTimeMillis();
				runConflictAnalysis = cpa.runConflictAnalysis();
			} catch (UnsupportedRuleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long completeEndTime = System.currentTimeMillis();
			long completeRunTime = completeEndTime-completeStartTime;
			if(runConflictAnalysis != null){
				List<CriticalPair> criticalPairs = runConflictAnalysis.getCriticalPairs();
				System.out.println("number of essential CPs: "+criticalPairs.size());
				List<CriticalPair> filteredDeleteUseConflicts = new LinkedList<CriticalPair>();
				for(CriticalPair cp : criticalPairs){
					if (cp instanceof Conflict) {
						if(((Conflict)cp).getConflictKind().equals(ConflictKind.DELETE_USE_CONFLICT)){							
							filteredDeleteUseConflicts.add(cp);
						}
					}
				}
				System.err.println("delete-use-conflicts: "+filteredDeleteUseConflicts.size());
			}else {
				System.err.println("essentail CPA failed!");
			}
			System.out.println("complete runtime: "+completeRunTime);
		}

		logger.exportStoredRuntimeToCSV(projectPath + File.separator);
		logger.exportStoredConflictsToCSV(projectPath + File.separator);
	}




	private List<CriticalPair> filterDeleteUseConflicts(CPAResult essentialResult) {
		//filter delete-use conflicts:
		if(essentialResult != null){
			List<CriticalPair> criticalPairs = essentialResult.getCriticalPairs();
//								System.out.println("number of essential CPs: "+criticalPairs.size());
			List<CriticalPair> filteredDeleteUseConflicts = new LinkedList<CriticalPair>();
			for(CriticalPair cp : criticalPairs){
				if (cp instanceof Conflict) {
					if(((Conflict)cp).getConflictKind().equals(ConflictKind.DELETE_USE_CONFLICT)){							
						filteredDeleteUseConflicts.add(cp);
					}
				}
			}
			return filteredDeleteUseConflicts;
		}else {
//								System.err.println("essentail CPA failed!");
		}
		return new LinkedList<CriticalPair>();
	}




	private void inspectDirectoryForHenshinFiles(File dir) {
		File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	System.out.println("TODO: recursive call of exploration method");
		    	String fileName = child.getName();
		    	if(fileName.endsWith(".henshin")){
		    		pathsToHenshinFiles.add(child.getAbsolutePath());
		    	} else if (!child.getName().contains(".")) {
		    		File subDir = child;
		    		inspectDirectoryForHenshinFiles(subDir);
				}
		    }
		  } else {
		    // Handle the case where dir is not really a directory.
		    // Checking dir.isDirectory() above would not be sufficient
		    // to avoid race conditions with another process that deletes
		    // directories.
		  }
	}
	
	



}
