package org.eclipse.emf.henshin.preprocessing;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.henshin.model.Formula;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class RuleSetModifier {

	public void removeMultiRules(String sourcePath, String targetPath){
		
		File dir = new File(sourcePath);
		List<String> pathsToHenshinFiles = DirectoryUtil.inspectDirectoryForHenshinFiles(dir);
		
		for (String pathToHenshinFiles : pathsToHenshinFiles) {
			HenshinResourceSet henshinResourceSet = new HenshinResourceSet();
			Module module = henshinResourceSet.getModule(pathToHenshinFiles);
			boolean unitContained = false;
			for (Unit unit : module.getUnits()){
				if (unit instanceof Rule){
					Rule rule = (Rule) unit;
					if(rule.getMultiRules().size() > 0){
						List<Rule> multiRules = new LinkedList<Rule>();
						multiRules.addAll(rule.getMultiRules());
						rule.getMultiRules().removeAll(multiRules);
					}
				}
			}
		// Ergebnis speichern
			String resultPath = pathToHenshinFiles.replace(sourcePath, targetPath);
			// save result in FileSystem!
			HenshinResourceSet resultResourceSet = new HenshinResourceSet();
			resultResourceSet.saveEObject(module, resultPath);
		}
	}

	public void removeUnits(String sourcePath, String targetPath){
		
		File dir = new File(sourcePath);
		List<String> pathsToHenshinFiles = DirectoryUtil.inspectDirectoryForHenshinFiles(dir);
		
		for (String pathToHenshinFiles : pathsToHenshinFiles) {
			HenshinResourceSet henshinResourceSet = new HenshinResourceSet();
			Module module = henshinResourceSet.getModule(pathToHenshinFiles);
			boolean unitContained = false;
			Set<Unit> units = new HashSet<Unit>();
			for (Unit unit : module.getUnits()){
				if (!(unit instanceof Rule)){
					units.add(unit);
				}
			}
			module.getUnits().removeAll(units);
//			if(unitContained){
//				numberOfFilesWithUnits++;
//			}
			
		// Ergebnis speichern
			String resultPath = pathToHenshinFiles.replace(sourcePath, targetPath);
			// save result in FileSystem!
			HenshinResourceSet resultResourceSet = new HenshinResourceSet();
			resultResourceSet.saveEObject(module, resultPath);
		}
	}

	public void removeApplicationConditions(String sourcePath, String targetPath) {
		
		File dir = new File(sourcePath);
		List<String> pathsToHenshinFiles = DirectoryUtil.inspectDirectoryForHenshinFiles(dir);
		
		for (String pathToHenshinFiles : pathsToHenshinFiles) {
			HenshinResourceSet henshinResourceSet = new HenshinResourceSet();
			Module module = henshinResourceSet.getModule(pathToHenshinFiles);
			boolean unitContained = false;
			Set<Unit> units = new HashSet<Unit>();
			for (Unit unit : module.getUnits()){
				if (unit instanceof Rule){
					Rule rule = (Rule) unit;
//					// check regarding AC:
					Formula formula = rule.getLhs().getFormula();
					if(formula != null)
						rule.getLhs().setFormula(null);
				}
			}
			module.getUnits().removeAll(units);
		// Ergebnis speichern
			String resultPath = pathToHenshinFiles.replace(sourcePath, targetPath);
			HenshinResourceSet resultResourceSet = new HenshinResourceSet();
			resultResourceSet.saveEObject(module, resultPath);
		}
	}

}
