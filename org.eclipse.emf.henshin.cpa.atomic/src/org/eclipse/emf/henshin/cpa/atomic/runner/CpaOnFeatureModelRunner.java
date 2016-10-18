package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class CpaOnFeatureModelRunner {
	
	// Relative path to the transformations.
	static String TRANSFORMATIONS = "transformations/";
	
	private static Engine engine;// = new EngineImpl();
	
	private static Module module;
	
	private static HenshinResourceSet henshinResourceSet;

	public static void main(String args[]){
		System.out.println("test");
		
		Runner runner = new Runner();
		
		runner.run();
		
		
		
		//get object which represents the workspace
//		IWorkspace workspace = ResourcesPlugin.getWorkspace(); //only possible in running plugins!
		 
		//get location of workspace (java.io.File)
//		File workspaceDirectory = workspace.getRoot().getLocation().toFile();
		
		
//		((IFile) selection).getWorkspace()
//		IPath path = new Path("");
//		workspace.getRoot().getFile(path);
		
//		IProject prj = ResourcePlugin.getWorkspace().getRoot().getProject("project-name");
//		IFile theFile = prj.getFile(sourceFolder + packageName.replace('.','/') + className + ".java");
		
		
		
		//TODO: laden aller Regeln im Unterverzeichnis eines gegeben Pfades.
		
		// Create a Henshin resource set:
//		if(henshinResourceSet == null){			
//			henshinResourceSet = new HenshinResourceSet();
//		}

		// ensure module is loaded!
//		getModule(); //old code trash
		
		// ensure units are loaded!
//		initializeAllUnits();
	}
	


	private static void run() {
		// TODO Auto-generated method stub
		
	}



	public static Module getModule() {
		// Load the module:
		if(module == null){
			module = henshinResourceSet.getModule(TRANSFORMATIONS+"mutations.henshin", false);
		}
		return module;
	}
	


//	private static void initializeAllUnits() {
//		if(anyUnitIsUninitialized()){			
//			moveSelectedFeatureUnit = getModule().getUnit("moveSelectedFeature");
//			joinSelectedClassesUnit = getModule().getUnit("joinSelectedClasses");
//			moveOnceReferencedAttrToItsMethodUnit = getModule().getUnit("moveOnceReferencedAttrToItsMethod");
//			randomSplitClassUnit = getModule().getUnit("randomSplitClass");
//			moveFeatureUnit = getModule().getUnit("moveFeature");
//		}
//	}
//	
	
}
