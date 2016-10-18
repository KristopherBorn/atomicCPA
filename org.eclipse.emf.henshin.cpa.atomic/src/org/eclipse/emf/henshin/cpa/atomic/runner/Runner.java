package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
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
	
	private List<String> deactivatedRules;// = new LinkedList<String>();


	public void run() {
		
		
//		ArchitectureCRAPackage.eINSTANCE.eClass();
		
		FeatureModelPackage.eINSTANCE.eClass();

	    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("xmi", new XMIResourceFactoryImpl());
	    
		
		ResourceSet resSet = new ResourceSetImpl();
		
		

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
		 
		
		
		
		
		
		// TODO Auto-generated method stub
		Package package1 = this.getClass().getPackage();
		
		final File f = new File(Runner.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		
		String filePath = f.toString();
//		String shortendPath = filePath.replaceAll("org.eclipse.emf.henshin.cpa.atomic\\bin", "");
		String projectPath = filePath.replaceAll("bin", "");
//		String shortendPath0 = filePath.replaceAll("bin", "");
//		 String shortendPath1 = shortendPath0.substring(0, shortendPath0.length()-1);
//		 String projectPath = shortendPath1.replaceAll("org.eclipse.emf.henshin.cpa.atomic", "");
//		String shortendPath = filePath.replaceAll("bin", "");
		
//		filePath.replaceAll(regex, replacement)
		
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
					if (unit instanceof Rule /*&& numberOfAddedRules<1*/) {
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
		  
		System.out.println("HALT");
		
		
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		
//		Rule singleRule = allEditRulesWithoutAmalgamation.get(0);
//		List<ConflictAtom> singleComputedConflictAtoms = atomicCoreCPA.computeConflictAtoms(singleRule, singleRule);	
//		System.out.println("amount of Results: "+singleComputedConflictAtoms.size());
		
		
		boolean runAtomicAnalysis = true;
		if(runAtomicAnalysis){
			List<String> shortResults = new LinkedList<String>();
			int numberOfAnalysis = 0;
			int numberOfConflictsOverall = 0;
			for(Rule firstRule : allEditRulesWithoutAmalgamation){
//				if(firstRule.getName().contains("deleteGroup_IN_FeatureModel")){
					for(Rule secondRule : allEditRulesWithoutAmalgamation){	
						String ruleCombination = firstRule.getName()+" -> "+secondRule.getName();
						System.out.println("start combination: "+ruleCombination);
						long startTime = System.currentTimeMillis();
						List<ConflictAtom> computeConflictAtoms = atomicCoreCPA.computeConflictAtoms(firstRule, secondRule);	
						long endTime = System.currentTimeMillis();
						long runTime = startTime-endTime;
						System.out.println("executed: "+ruleCombination+" del-use-confl: "+computeConflictAtoms.size()+" in "+runTime+" ms");
						numberOfAnalysis++;
						System.err.println("number of analyses: "+numberOfAnalysis);
						shortResults.add(computeConflictAtoms.size()+" conflicts in "+ruleCombination);
						numberOfConflictsOverall += computeConflictAtoms.size();
					}
//				}
			}
			for (String shortResultString : shortResults) {			
				System.out.println(shortResultString);
			}
			System.out.println("number of conflicts overall: "+numberOfConflictsOverall);
		}
		
		boolean runEssentialCPA = false;
		CPAResult runConflictAnalysis = null;
		if(runEssentialCPA){
			ICriticalPairAnalysis cpa = new CpaByAGG();
			CPAOptions options = new CPAOptions();
			options.setEssential(true);
			try {
				cpa.init(allEditRulesWithoutAmalgamation, options);
				runConflictAnalysis = cpa.runConflictAnalysis();
			} catch (UnsupportedRuleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			
		}
	}




	private void inspectDirectoryForHenshinFiles(File dir) {
		File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	System.out.println("TODO: recursive call of exploration method");
		    	String fileName = child.getName();
		    	if(fileName.endsWith(".henshin")){
		    		pathsToHenshinFiles.add(child.getAbsolutePath());
//		    		addHenshinFile(child.getAbsolutePath());
		    	} else if (!child.getName().contains(".")) {
		    		File subDir = child;
		    		inspectDirectoryForHenshinFiles(subDir);
//					versuchen den Ordner rekursiv zu untersuchen.
//					TODO: ggf. eine Liste "auszuschließender" henshin Regeln anzulegen!
				}
		      // Do something with child
		    }
		  } else {
		    // Handle the case where dir is not really a directory.
		    // Checking dir.isDirectory() above would not be sufficient
		    // to avoid race conditions with another process that deletes
		    // directories.
		  }
	}
	
	


	private void addHenshinFile(String absolutePath) {
//		TODO: henshin files hinzufügen 
		// TODO Auto-generated method stub
		
	}




	class MyResourceVisitor implements IResourceVisitor{

		@Override
		public boolean visit(IResource resource) { 
		if (!(resource.getType() == IResource.FILE)) 
			return true; 
		if (resource.getName().endsWith(".java"))
			System.out.println("process Java file");
	//			processJavaFile((IFile)resource);
		if (resource instanceof IFolder) {
			System.out.println("folder detected: "+resource.getName());
			
		}
		return true; 
		} 
		
	}
}
