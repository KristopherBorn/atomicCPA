package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.cpa.result.Conflict;
import org.eclipse.emf.henshin.cpa.result.ConflictKind;
import org.eclipse.emf.henshin.cpa.result.CriticalPair;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.MappingList;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.impl.HenshinFactoryImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;


public class Runner {

// options to turn on and off different analyses
	boolean runNormalCPA = true;
	boolean runEssentialCPA = true;
	boolean runAtomicAnalysis = true;
	
	private boolean noApplicationConditions = false;
	private boolean removeAllMultirules = false;
	private Set<String> limitedSetOfRulesByRuleNames;
	


	public void run(String fullSubDirectoryPath, List<String> deactivatedRules) {

		Logger logger = new Logger();

		File dir = new File(fullSubDirectoryPath);
		List<String> pathsToHenshinFiles = inspectDirectoryForHenshinFiles(dir);

		List<Rule> allLoadedRules = loadAllRulesFromFileSystemPaths(pathsToHenshinFiles, deactivatedRules);

		// fix inconsistent dangling options : all rules shall "check dangling"
		for(Rule rule : allLoadedRules){
			rule.setCheckDangling(true);
		}		
		
		if(noApplicationConditions){
			for(Rule rule : allLoadedRules){
				List<NestedCondition> allPACs = new LinkedList<>(rule.getLhs().getPACs());
				for(NestedCondition pac : allPACs)
					rule.getLhs().removeNestedCondition(pac);
				List<NestedCondition> allNACs = new LinkedList<>(rule.getLhs().getNACs());
				for(NestedCondition nac : allNACs)
					rule.getLhs().removeNestedCondition(nac);
			}			
		}
		
		if(removeAllMultirules){
			for(Rule rule : allLoadedRules){
				rule.getMultiRules().clear();
			}			
		}		
		
		// aufgrund der Erkenntnis, dass die nutzende (use) Regel keine löschenden Teile haben darf 
		// wird zu jeder zu analysierenden Regel eine angepasste Regel als zweite nutzende (use) Regel erstellt
		List<Rule> copiesOfRulesWithoutDeletion = buildCopiesOfRulesWithoutDeletion(allLoadedRules);

		
//		Namen müssen dabei adaptiert werden, sonst ist nach dem Export bei der Ansteuerung von agg nicht klar welche Regel die erste und welche die zweite ist.
		// dennoch kann (sollte!) beim loggen der Ergebnisse wieder der original name verwendet werden - DONE!
		

		int numberOfAddedRules = allLoadedRules.size();

		// System.out.println("HALT");

		logger.init(numberOfAddedRules);
		logger.setAddDetailsOnRuleName(true);

//		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();

		logger.setAnalysisKinds(runNormalCPA, runEssentialCPA, runAtomicAnalysis);

		
		// normal CPA setup
		ICriticalPairAnalysis normalCpa = new CpaByAGG();
		CPAOptions normalOptions = new CPAOptions();
		
		
		// essential CPA setup
		ICriticalPairAnalysis essentialCpa = new CpaByAGG();
		CPAOptions essentialOptions = new CPAOptions();
		essentialOptions.setEssential(true);

		// classic CPA setup
		// TODO!!!
		
		int numberOfAllEssentialConflicts = 0;
		int numberOfFilteredEssentialConflicts = 0;
		
		long totalNormalRuntime = 0;
		long totalEssentialRuntime = 0;
		long totalAtomicRuntime = 0;
		
		int totalNumberOfNormalCPs= 0;
		int totalNumberOfEssentialCPs = 0;
		int totalNumberOfAtomicCPs = 0;
		int totalNumberOfConflictAtomCandidates = 0;
		int totalNumberOfMinimalConflictReasons = 0;
		
		int currentRow = 0;
		int firstRowToAnalyse = 0;
		int lastRowToAnalyse = 50;
				

		for (Rule firstRule : allLoadedRules) {
			
			if(ruleIsntLimited(firstRule)){
				
				if(currentRow >= firstRowToAnalyse && currentRow <= lastRowToAnalyse){
					
					for (Rule secondRule : copiesOfRulesWithoutDeletion) {
						
						if(ruleIsntLimited(secondRule)){
							
							StringBuffer runTimesOfRuleCombination = new StringBuffer();
							StringBuffer amountOfDeleteUseConflictsOfRulecombination = new StringBuffer();
							
							if (runNormalCPA) {
								
								long normalStartTime = System.currentTimeMillis();
								List<Rule> firstRuleList = new LinkedList<Rule>();
								firstRuleList.add(firstRule);
								List<Rule> secondRuleList = new LinkedList<Rule>();
								secondRuleList.add(secondRule);
								CPAResult normalResult = null;
//								
								ResultKeeper resultKeeper = new ResultKeeper(firstRuleList, secondRuleList, normalOptions);
								ExecutorService executor = Executors.newSingleThreadExecutor();
								try {
									executor.submit(new CalculateCpaTask(resultKeeper)).get(1, TimeUnit.MINUTES);
								} catch (NullPointerException | InterruptedException | ExecutionException e) {
									System.err.println("Timeout!");
									executor.shutdown();
								} catch (TimeoutException e) {
									System.err.println("TIME OUT!!!");
									e.printStackTrace();
								}
								
								executor.shutdown();
								normalResult = resultKeeper.getResult();
								
								long normalRunTime = resultKeeper.getNormalRunTime();
								
								totalNormalRuntime += normalRunTime;
								
								runTimesOfRuleCombination.append(String.valueOf(normalRunTime));
								runTimesOfRuleCombination.append("/");
								
								if (normalResult != null) {
									List<CriticalPair> filteredDeleteUseConflicts = filterDeleteUseConflicts(normalResult);
									amountOfDeleteUseConflictsOfRulecombination
									.append(String.valueOf(filteredDeleteUseConflicts.size()));
									amountOfDeleteUseConflictsOfRulecombination.append("/");
									
									totalNumberOfNormalCPs += filteredDeleteUseConflicts.size();
								} else {
									System.err.println("normal CPA failed!");
									amountOfDeleteUseConflictsOfRulecombination.append("TO");
									amountOfDeleteUseConflictsOfRulecombination.append("/");
								}
							}
							
							if (runEssentialCPA) {
								
								long essentialStartTime = System.currentTimeMillis();
								List<Rule> firstRuleList = new LinkedList<Rule>();
								firstRuleList.add(firstRule);
								List<Rule> secondRuleList = new LinkedList<Rule>();
								secondRuleList.add(secondRule);;
								CPAResult essentialResult = null;

								ResultKeeper resultKeeper = new ResultKeeper(firstRuleList, secondRuleList, essentialOptions);
								ExecutorService executor = Executors.newSingleThreadExecutor();
								try {
									executor.submit(new CalculateEssentialCpaTask(resultKeeper)).get(1, TimeUnit.MINUTES);
								} catch (NullPointerException | InterruptedException | ExecutionException e) {
									System.err.println("Timeout!");
									executor.shutdown();
								} catch (TimeoutException e) {
									System.err.println("TIME OUT!!!");
									e.printStackTrace();
								}
								long essentialEndTime = System.currentTimeMillis();
								
								executor.shutdown();
								essentialResult = resultKeeper.getResult();
								
								
								long essentialRunTime = essentialEndTime - essentialStartTime;
								totalEssentialRuntime += essentialRunTime;
								
								runTimesOfRuleCombination.append(String.valueOf(essentialRunTime));
								runTimesOfRuleCombination.append("/");
								
								if (essentialResult != null) {
									numberOfAllEssentialConflicts +=  essentialResult.getCriticalPairs().size();
									List<CriticalPair> filteredDeleteUseConflicts = filterDeleteUseConflicts(essentialResult);
									numberOfFilteredEssentialConflicts += filteredDeleteUseConflicts.size();
									System.err.println("delete-use-conflicts: " + filteredDeleteUseConflicts.size());
					
									amountOfDeleteUseConflictsOfRulecombination
									.append(String.valueOf(filteredDeleteUseConflicts.size()));
									amountOfDeleteUseConflictsOfRulecombination.append("/");
									
									totalNumberOfEssentialCPs += filteredDeleteUseConflicts.size();
								} else {
									amountOfDeleteUseConflictsOfRulecombination.append("TO");
									amountOfDeleteUseConflictsOfRulecombination.append("/");
								}
							}
							
							if (runAtomicAnalysis) {
								// List<String> shortResults = new LinkedList<String>();
								int numberOfAnalysis = 0;
								int numberOfConflictsOverall = 0;
								String ruleCombination = firstRule.getName() + " -> " + secondRule.getName();
//						System.out.println("start combination: " + ruleCombination);
								long atomicStartTime = System.currentTimeMillis();
//						List<ConflictAtom> computeConflictAtoms = atomicCoreCPA.computeConflictAtoms(firstRule, secondRule);
								
								
								
								AtomicResultKeeper resultKeeper = new AtomicResultKeeper(firstRule, secondRule);
								ExecutorService executor = Executors.newSingleThreadExecutor();
								try {
									executor.submit(new CalculateAtomicCpaTask(resultKeeper)).get(1, TimeUnit.MINUTES);
								} catch (NullPointerException | InterruptedException | ExecutionException e) {
									System.err.println("Timeout!");
									executor.shutdown();
								} catch (TimeoutException e) {
									System.err.println("TIME OUT!!!");
									e.printStackTrace();
								}
								
								List<ConflictAtom> atomicCoreCpaConflictAtoms = resultKeeper.getConflictAtoms();
								List<Span> atomicCoreCpaCandidates = resultKeeper.getCandidates();
								Set<Span> atomicCoreCpaOverallReasons = resultKeeper.getOverallReasons();
								
								long atomicEndTime = System.currentTimeMillis();
								long atomiRunTime = atomicEndTime - atomicStartTime;
								totalAtomicRuntime += atomiRunTime;
								
								runTimesOfRuleCombination.append(String.valueOf(atomiRunTime));
								
								System.out.println("executed: " + ruleCombination + " del-use-confl: " + atomicCoreCpaConflictAtoms.size()
								+ " in " + atomiRunTime + " ms");
								numberOfAnalysis++;
								System.err.println("number of analysis: " + numberOfAnalysis);
								// shortResults.add(computeConflictAtoms.size()+" conflicts in "+ruleCombination);
								numberOfConflictsOverall += atomicCoreCpaConflictAtoms.size();
								
								amountOfDeleteUseConflictsOfRulecombination.append(String.valueOf(atomicCoreCpaConflictAtoms.size()));
								amountOfDeleteUseConflictsOfRulecombination.append("/");
								amountOfDeleteUseConflictsOfRulecombination.append(String.valueOf(atomicCoreCpaCandidates.size()));
								amountOfDeleteUseConflictsOfRulecombination.append("/");
								amountOfDeleteUseConflictsOfRulecombination.append(String.valueOf(atomicCoreCpaOverallReasons.size()));
								
								totalNumberOfAtomicCPs += atomicCoreCpaConflictAtoms.size();
								totalNumberOfConflictAtomCandidates += atomicCoreCpaCandidates.size();
								totalNumberOfMinimalConflictReasons += atomicCoreCpaOverallReasons.size();
							}
							
							// relies on equal order of original rule and associated copy without deletion! 
							Rule originalRuleOfRule2 = allLoadedRules.get(copiesOfRulesWithoutDeletion.indexOf(secondRule));
							
							logger.addData(firstRule, originalRuleOfRule2, runTimesOfRuleCombination.toString(),
									amountOfDeleteUseConflictsOfRulecombination.toString());
							
							// }
							
						}
					}
					System.gc();
				}
			}
			
//			todo: introduce row limitatio
			currentRow++;
			
		}

		logger.addRunTimes(totalNormalRuntime, totalEssentialRuntime, totalAtomicRuntime);
		
		logger.addTotalResults(totalNumberOfNormalCPs, totalNumberOfEssentialCPs, totalNumberOfAtomicCPs, totalNumberOfConflictAtomCandidates, totalNumberOfMinimalConflictReasons);
		
		System.err.println("numberOfAllEssentialConflicts: "+numberOfAllEssentialConflicts);
		System.err.println("numberOfFilteredEssentialConflicts: "+numberOfFilteredEssentialConflicts);
		System.out.println("HALT");


		logger.exportStoredRuntimeToCSV(fullSubDirectoryPath + File.separator);
		logger.exportStoredConflictsToCSV(fullSubDirectoryPath + File.separator);
	}
	
	
private boolean ruleIsntLimited(Rule ruleToCheck) {
		if(limitedSetOfRulesByRuleNames == null){
			return true;		
		}
		else{
			System.out.println(ruleToCheck.getName());
			if(limitedSetOfRulesByRuleNames.contains(ruleToCheck.getName())){
				return true;
			}
			return false;
		}
	}


	//	TODO: nutzende Regeln erstellen
//	dazu copier für jede REgel verwenden
//	Namen der kopierten REgel anpassen
//	alle mappings löschen
//	dann copier für LHS verwenden
//	ERgebnisgraph der kopie in RHS umbenennen
//	mappings für alle Knoten erstellen!
	private List<Rule> buildCopiesOfRulesWithoutDeletion(List<Rule> allLoadedRules) {
		HenshinFactory henshinFactory = new HenshinFactoryImpl();
		List<Rule> copiesOfRulesWithoutDeletion = new LinkedList<>(); 
		for(Rule ruleToCopy : allLoadedRules){
//		REMOVE	TODO: nutzende Regeln erstellen
//		REMOVE	dazu copier für jede REgel verwenden
				
//		REMOVE!	EObject ruleToCopyObject = (EObject) ruleToCopy;
			Copier copierForRule = new Copier();
			Rule copyOfRule = (Rule) copierForRule.copy(ruleToCopy);
//			Namen der kopierten REgel anpassen
//		DONE	TODO: Erfolg per debug überprüfen!
			copyOfRule.setName(copyOfRule.getName().concat("_"));
	//		copier.copyAll(getRoots()); // nur für ollections!
			copierForRule.copyReferences();
			
			MappingList mappings = copyOfRule.getMappings();
			
//			alle mappings löschen
//		DONE	todo: debugen, dass danach wirklich keine mappings mehr in der Kopie sind!
			mappings.clear();
			//clear all attr:
			for(Node nodeInLhs : copyOfRule.getLhs().getNodes()){
				nodeInLhs.getAttributes().clear();
			}
					
//		REMOVE	// HenshinPackage.RULE__MAPPINGS // überrest - ENTFERNEN!			
//		REMOVE	// new MappingContainmentListImpl(this, HenshinPackage.RULE__MAPPINGS);
			
//			dann copier für LHS verwenden
			// Copier for new RHS based on LHS
			Copier copierForLhsGraph = new Copier();
			// copy of graph
			Graph copiedLhs = (Graph) copierForLhsGraph.copy(copyOfRule.getLhs());
			copierForLhsGraph.copyReferences();
//			ERgebnisgraph der kopie in RHS umbenennen
//			TODO: prüfen ob beschriftung der Konvention entspricht!
			copiedLhs.setName("Rhs");
			copyOfRule.setRhs(copiedLhs);
			
//			mappings für alle Knoten erstellen!
			for(Node nodeInLhsOfCopiedRule : copyOfRule.getLhs().getNodes()){
				Node nodeInNewRhs = (Node) copierForLhsGraph.get(nodeInLhsOfCopiedRule);
				Mapping createdMapping = henshinFactory.createMapping(nodeInLhsOfCopiedRule, nodeInNewRhs);
				mappings.add(createdMapping);					
			}
			
			copiesOfRulesWithoutDeletion.add(copyOfRule);
			
			
			// TODO Auto-generated method stub
				
		}
		return copiesOfRulesWithoutDeletion;
	}

	public void setAnalysisKinds(boolean runNormalCPA, boolean runEssentialCPA, boolean runAtomicAnalysis){
		this.runNormalCPA = runNormalCPA;
		this.runEssentialCPA = runEssentialCPA;
		this.runAtomicAnalysis = runAtomicAnalysis;
	}

	private List<Rule> loadAllRulesFromFileSystemPaths(List<String> pathsToHenshinFiles, List<String> namesOfDeactivatedRules) {
		List<Rule> allEditRulesWithoutAmalgamation = new LinkedList<Rule>();

		for (String pathToHenshinFiles : pathsToHenshinFiles) {
			HenshinResourceSet henshinResourceSet = new HenshinResourceSet();
			Module module = henshinResourceSet.getModule(pathToHenshinFiles);
			for (Unit unit : module.getUnits()) {
				if (unit instanceof Rule /* && numberOfAddedRules<10 */) {
					// rulesAndAssociatedFileNames.put((Rule) unit, fileName);
					boolean deactivatedRule = false;
					for (String deactivatedRuleName : namesOfDeactivatedRules) {
						if (unit.getName().contains(deactivatedRuleName))
							deactivatedRule = true;
					}
					if (!deactivatedRule) {
						allEditRulesWithoutAmalgamation.add((Rule) unit);
					}
				}
			}
		}
		return allEditRulesWithoutAmalgamation;
	}

	private List<CriticalPair> filterDeleteUseConflicts(CPAResult essentialResult) {
		// filter delete-use conflicts:
		if (essentialResult != null) {
			List<CriticalPair> criticalPairs = essentialResult.getCriticalPairs();
			// System.out.println("number of essential CPs: "+criticalPairs.size());
			List<CriticalPair> filteredDeleteUseConflicts = new LinkedList<CriticalPair>();
			for (CriticalPair cp : criticalPairs) {
				if (cp instanceof Conflict) {
					if (((Conflict) cp).getConflictKind().equals(ConflictKind.DELETE_USE_CONFLICT)) {
						filteredDeleteUseConflicts.add(cp);
					}
				}
			}
			return filteredDeleteUseConflicts;
		} else {
			// System.err.println("essentail CPA failed!");
		}
		return new LinkedList<CriticalPair>();
	}

	private List<String> inspectDirectoryForHenshinFiles(File dir) {
		List<String> pathsToHenshinFiles = new LinkedList<String>();
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				System.out.println("TODO: recursive call of exploration method");
				String fileName = child.getName();
				if (fileName.endsWith(".henshin")) {
					pathsToHenshinFiles.add(child.getAbsolutePath());
				} else if (!child.getName().contains(".")) {
					File subDir = child;
					pathsToHenshinFiles.addAll(inspectDirectoryForHenshinFiles(subDir));
				}
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
		return pathsToHenshinFiles;
	}

	public void setNoApplicationConditions(boolean removeAllApplicationConditions) {
		this.noApplicationConditions = removeAllApplicationConditions;
	}


	public void setNoMultirules(boolean removeAllMultirules) {
		this.removeAllMultirules = removeAllMultirules; 
	}


	public void limitSetOfRulesByRuleNames(Set<String> limitedSetOfRulesByRuleNames) {
		this.limitedSetOfRulesByRuleNames = limitedSetOfRulesByRuleNames;
	}

}
