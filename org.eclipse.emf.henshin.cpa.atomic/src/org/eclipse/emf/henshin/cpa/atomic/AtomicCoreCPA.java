package org.eclipse.emf.henshin.cpa.atomic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.PushoutResult;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Match;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.ModelElement;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.impl.EdgeImpl;
import org.eclipse.emf.henshin.model.impl.GraphImpl;
import org.eclipse.emf.henshin.model.impl.HenshinFactoryImpl;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.NodeImpl;

public class AtomicCoreCPA {
	

	HenshinFactory henshinFactory = new HenshinFactoryImpl();

	public List<ConflictAtom> computeConflictAtoms(Rule rule1, Rule rule2){
		List<ConflictAtom> result = new LinkedList<ConflictAtom>();
		List<Span> candidates = computeCandidates(rule1, rule2);
		for(Span candidate : candidates){
			List<Span> reasons = new LinkedList<>();//
			computeMinReasons(rule1, rule2, candidate, reasons);
			if(reasons.isEmpty()){
				result.add(new ConflictAtom(candidate, reasons));
				//TODO: wieso ein Atom die "reasons" benötigt ist mir noch unklar.
				// Bzw.was die Datenstruktur "Atom" überhaupt umfasst.
			}
		}
		return result;
	}
	
	
//	- elemente sammeln
//	- alle deletion-nodes
//	- alle deletion-edges mit zwei preserve-nodes
//		- vorkommen der elemente in der LHS von R2
//		- erstellen eines Graph (S1) und der Mappings in rule1 und rule2
//	Wie programmatisch instanzen des jeweiligen MM erstellen?
//	Henshins "MappingImpl" Klasse wirklich geeignet? Oder eher MatchImpl?
	public List<Span> computeCandidates(Rule rule1, Rule rule2) {
		List<Span> result = new LinkedList<Span>();
		Action deleteAction = new Action(Action.Type.DELETE);

		//TODO: extract to Method!
		List<ModelElement> atomicDeletionElements = new LinkedList<ModelElement>(rule1.getActionNodes(deleteAction));
		for(Edge deletionEdge : rule1.getActionEdges(deleteAction)){
			Action sourceNodeAction = deletionEdge.getSource().getAction();
			Action targetNodeAction = deletionEdge.getTarget().getAction();
			if(sourceNodeAction.getType().equals(Action.Type.PRESERVE) && targetNodeAction.getType().equals(Action.Type.PRESERVE)){
				atomicDeletionElements.add(deletionEdge);
			}
		}
		for(ModelElement el1 : atomicDeletionElements){
			List<ModelElement> atomicUsageElements = new LinkedList<ModelElement>();
			if(el1 instanceof Node){
				atomicUsageElements.addAll(rule2.getLhs().getNodes(((Node) el1).getType()));
//				EList<Node> nodes = rule2.getLhs().getNodes(((Node) el1).getType());
			}
			if(el1 instanceof Edge){
				atomicUsageElements.addAll(rule2.getLhs().getEdges(((Edge) el1).getType()));
			}
			for(ModelElement el2 : atomicUsageElements){
				if(el2 instanceof Node){
					Node el2Node = (Node) el2;
					
					
					//TODO: noch ist nicht ersichtlich, ob oder warum für S1wirklich ein Graph  notwendig ist, 
					//		oder ob nicht ein Mapping zwischen zwischen den Knoten der beiden LHS ausreicht.
					Graph S1 = henshinFactory.createGraph();
					Node commonNode = henshinFactory.createNode(S1, el2Node.getType(), ""); 
					
					//TODO: maybe add a type check for el1, but since the associated el2 is here a node, el1 should be also.
					Mapping nodeInRule1Mapping = henshinFactory.createMapping(commonNode, (Node)el1);
					//TODO: maybe add a type check for el2, but there had been allready one at the beginning by the if-statement
					Mapping nodeInRule2Mapping = henshinFactory.createMapping(commonNode, (Node) el2);
					
					Span S1span = new Span(nodeInRule1Mapping, S1, nodeInRule2Mapping);
					result.add(S1span);
					
//					EClass type = ((Node) el2).getType();
//					EPackage singleEPackageOfDomainModel = type.getEPackage();
//					EFactory eFactoryInstance = singleEPackageOfDomainModel.getEFactoryInstance();
//					
//					EObject create = eFactoryInstance.create(type);
//					el2.eResolveProxy
				}
				if(el2 instanceof Edge){
					Graph S1 = henshinFactory.createGraph();
					
					List<Mapping> rule1Mappings = new LinkedList<Mapping>();
					List<Mapping> rule2Mappings = new LinkedList<Mapping>();
					
					Node sourceNodeInRule1 = ((Edge) el1).getSource();
					Node sourceNodeInRule2 = ((Edge) el2).getSource();
					Node commonSourceNode = henshinFactory.createNode(S1, sourceNodeInRule1.getType(), ""); 
					Mapping sourceNodeInRule1Mapping = henshinFactory.createMapping(commonSourceNode, sourceNodeInRule1);
					rule1Mappings.add(sourceNodeInRule1Mapping);
					Mapping sourceNodeInRule2Mapping = henshinFactory.createMapping(commonSourceNode, sourceNodeInRule2);
					rule2Mappings.add(sourceNodeInRule2Mapping);

					Node targetNodeInRule1 = ((Edge) el1).getTarget();
					Node targetNodeInRule2 = ((Edge) el2).getTarget();
					Node commonTargetNode = henshinFactory.createNode(S1, targetNodeInRule1.getType(), ""); 
					Mapping targetNodeInRule1Mapping = henshinFactory.createMapping(commonSourceNode, targetNodeInRule1);
					rule1Mappings.add(targetNodeInRule1Mapping);
					Mapping targetNodeInRule2Mapping = henshinFactory.createMapping(commonSourceNode, targetNodeInRule2);
					rule2Mappings.add(targetNodeInRule2Mapping);
					
					henshinFactory.createEdge(commonSourceNode, commonTargetNode, ((Edge) el2).getType());
					
					Span S1span = new Span(rule1Mappings, S1, rule2Mappings);
					result.add(S1span);
				}
			}
		}
		return result;
	}
	
	private void computeMinReasons(Rule rule1, Rule rule2, Span s1, List<Span> reasons) {
		if(isMinReason(rule1, rule2, s1)){
			reasons.add(s1);
			return;
		}
		// is this part of the backtracking?
		List<Span> extendedSpans = findExtensions(rule1, rule2, s1, reasons);
		for(Span extendedSpan : extendedSpans){
			computeMinReasons(rule1, rule2, extendedSpan, reasons);
		}		
	}


	private boolean isMinReason(Rule rule1, Rule rule2, Span s1) {
		// TODO: ist hier der Zusammenhang zwischen C_1 und L_1, also c_1 klar?
		PushoutResult pushoutResult = constructPushout(rule1, rule2, s1); 
		//TODO: wofür wird G benötigt? Vermutlich nur als Ziel der matches, oder?
		// 		Oder ist das nicht normalerweise das minimale Modell?
		boolean isMatchM1 = findDanglingEdges(rule1, pushoutResult.getMappingsOfRule1()).isEmpty(); //TODO: über den jeweiligen match sollte doch die Regel auch "erreichbar" sein. Regel als Parameter daher überflüssig.
		// TODO: wie "findDanglingEdges" funktioniert weiß ich noch nicht!
		boolean isMatchM2 = findDanglingEdges(rule2, pushoutResult.getMappingsOfRule2()).isEmpty();
		return (isMatchM1 && isMatchM2);
	}

	// TODO: bisher nicht weiter spezifiziert!
	/**
	 * Idee: 
	 *  Im prinziep 
	 * @param rule1
	 * @param rule2
	 * @param s1
	 * @return
	 */
	private PushoutResult constructPushout(Rule rule1, Rule rule2, Span s1) {
		PushoutResult pushoutResult = new PushoutResult(rule1, s1, rule2);
		// TODO einfach nur ein Aufruf des PushoutResult Konstruktor? 
//		Da es mutmaßlich nur ein 
//			- kopieren der beiden linken Seiten
//				- dabei aufbauen der mappings
//			- entfernen der "doppelten" Knoten und Kanten aus L2
//			- zusammenfassen der Mengen 
//			ist, kann dies alles auch im Konstruktor ablaufen!
		return pushoutResult;
	}

	private List<Span> findExtensions(Rule rule1, Rule rule2, Span s1, List<Span> reasons) {
		// TODO: bei "isMinReason" wird ebensfalls der Pushout gebildet. 
		// Was ist hier der Unterschied, bzw. es sollte vermieden werden, 
		// dass zweimal die gleichen Pushouts gebildet werden/existieren.
		PushoutResult pushoutResult = constructPushout(rule1, rule2, s1); 
		List<Edge> danglingEdges = new LinkedList<>();
		//TODO: wo sollen hier die matches (match1 und match2) herkommen?
		danglingEdges.addAll(findDanglingEdges(rule1, match1));
		danglingEdges.addAll(findDanglingEdges(rule2, match2));
		List<Edge> fixingEdges = new LinkedList<>();
		for(Edge danglingEdge : danglingEdges){
			//Frage: wofür steht das "e"? Welche Bedeutung hat das?
			//antwort:	vermutlich für die Kante "e", die hier als danglingEdge behoben werden soll.
			//	Frage: handelt es sich bei den "e"s um die zwischen S'_1 und S_1 wie in Definition3?
			//TODO: Ist hier nicht der overlapGraph "G" notwendig?
			//TODO: "fixingEdges" müssen ja irgendwie aus der Menge der Kanten der beiden Regeln stammen und vom Typ her zur danglingEdge passen (bzw. einer der SuperTypen sein)
			List<Edge> fixingEdges_e = findFixingEdges(rule1, rule2, s1, danglingEdge);
			if(!fixingEdges_e.isEmpty()){
				fixingEdges.addAll(fixingEdges_e);
			}
			else{
				// Frage: wird keine Lösung für eine dangling edge gefunden, 
				//		so soll die Suche nach Erweiterungen abgebrochen werden?
				//		Überlegung: was ist überhaupt das Ziel? -> Wenn dangling-edges auftreten,
				//			dann ist es nur eine extension wenn es für diese auch Lösungen gibt.
				return new LinkedList<Span>();// oder NULL?
			}
			// Frage: Was genau ist das, die disjointCombinations?
			List<Span> disjointCombinations = enumerateDisjointCombinations(s1, fixingEdges);
			return disjointCombinations;
		}		
	}


	// TODO: bisher nicht weiter spezifiziert!
	private List<Span> enumerateDisjointCombinations(Span s1, List<Edge> fixingEdges) {
		// TODO Auto-generated method stub
		return null;
	}

	// TODO: bisher nicht weiter spezifiziert!
	//		Funktionalität gibt es sehr wahrscheinlich schon in Henshin. (NEIN! - hier wird mit mappings anstelle von matches gearbeitet!)
	// Spezifikation der MEthode: Gibt die Menge der Kanten aus der Regel zurück, die beim anwenden auf den overlapGraph zu einer dangling edge führen würden!
	public List<Edge> findDanglingEdges(Rule rule, List<Mapping> mappings) {
		List<Edge> danglingEdges = new LinkedList<Edge>();
		
		//TODO: build an initial bidirektional hashmap of the nodes in the rule and in the overlapgraph based on the mappings!
			// teilweise schon durch "mappingsOfOriginNodes" und "mappingsOfImageNodes" geschehen!
		
		
		//TODO: einfach prüfen, ob es Kanten zu den gelöschten Knoten gibt, die durch die Regel nicht mit abgedeckt sind. 
		EList<Node> deletionNodesOfRule = rule.getActionNodes(new Action(Action.Type.DELETE));
		EList<Edge> deletionEdgesOfRule = rule.getActionEdges(new Action(Action.Type.DELETE));
		// Annahme: es gibt ein mapping für jeden Knoten aus der Regel.
		HashMap<Node, Mapping> mappingsOfOriginNodes = new HashMap<Node, Mapping>();	
		for(Mapping mapping : mappings){
			Node originNodeOfMapping = mapping.getOrigin();
			mappingsOfOriginNodes.put(originNodeOfMapping, mapping);
		}
		
		HashMap<Node, Mapping> mappingsOfImageNodes = new HashMap<Node, Mapping>();	
		for(Mapping mapping : mappings){
			Node imageNodeOfMapping = mapping.getImage();
			mappingsOfImageNodes.put(imageNodeOfMapping, mapping);
		}
		
		// für jeden gelöschten Knoten prüfen, dass auch all seine Kanten gelöscht werden. 
		for(Node deleteNode : deletionNodesOfRule){
			Mapping mappingOfDeleteNode = mappingsOfOriginNodes.get(deleteNode);
			Node nodeToBeDeletedInOverlapGraph = mappingOfDeleteNode.getImage();
			EList<Edge> allDeletionEdgesOfDeletionNodeInRule = deleteNode.getAllEdges();
			//TODO: all ascending nodes of deleteNode
			
			EList<Edge> allEdgesToBeCheckedForDeletion = nodeToBeDeletedInOverlapGraph.getAllEdges();
			for(Edge edgeInOverlapToBeCheckedForDeletion : allEdgesToBeCheckedForDeletion){
				EReference typeOfEdgeInOverlapGraph = edgeInOverlapToBeCheckedForDeletion.getType();
				Node sourceOfEdgeInOverlapGraph = edgeInOverlapToBeCheckedForDeletion.getSource();
				Mapping mappingOfSourceOfEdge = mappingsOfImageNodes.get(sourceOfEdgeInOverlapGraph);
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				if(mappingOfSourceOfEdge == null){
					danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
					continue;
				}
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				Node sourceOfEdgeInRule = mappingOfSourceOfEdge.getOrigin();
				
				
				Node targetOfEdgeInOverlapGraph = edgeInOverlapToBeCheckedForDeletion.getTarget();
				Mapping mappingOfTargetOfEdge = mappingsOfImageNodes.get(targetOfEdgeInOverlapGraph);
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				if(mappingOfTargetOfEdge == null){
					danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
					continue;
				}
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				Node targetOfEdgeInRule = mappingOfTargetOfEdge.getOrigin();
				
				
				if(sourceOfEdgeInRule != null && targetOfEdgeInRule !=null){
					
					//TODO: identify second node of Edge
//				Node secondInvolvedNodeOfDeletionEdge = null;
//				if(edgeInOverlapToBeCheckedForDeletion.getSource() == nodeToBeDeletedInOverlapGraph)
//					secondInvolvedNodeOfDeletionEdge = edgeInOverlapToBeCheckedForDeletion.getTarget();
//				if(edgeInOverlapToBeCheckedForDeletion.getTarget() == nodeToBeDeletedInOverlapGraph)
//					secondInvolvedNodeOfDeletionEdge = edgeInOverlapToBeCheckedForDeletion.getSource();
					//TODO: finden des zugehörgen Knotens in der Regel
//				Mapping mapping = mappingsOfImageNodes.get(secondInvolvedNodeOfDeletionEdge);
//				Node originNodeOfEdgeToBeDeleted = mapping.getOrigin();	
					// sollte 
					
					//TODO: prüfen, dass die Kante zwischen den beiden Knoten in der Regel als zu löschende Kante definiert ist.
					// TODO: alle Kanten zwischen beiden Knoten (deleteNode & originNodeOfEdgeToBeDeleted) in der Regel ermitteln
					EList<Edge> outgoingEdgesOfNodeInRule = sourceOfEdgeInRule.getOutgoing();	
					// TODO: Ziel abgleichen
					List<Edge> potentialEdgesInRule = new LinkedList<Edge>();
					for(Edge edge : outgoingEdgesOfNodeInRule){
						if(edge.getTarget() == targetOfEdgeInRule)
							potentialEdgesInRule.add(edge);
					}
					// TODO: type abgleichen
					Edge edgeInRule = new EdgeImpl();//only temporary to prevent NPE
					for(Edge edge : potentialEdgesInRule){
						if(edge.getType().equals(edgeInOverlapToBeCheckedForDeletion.getType()))
							edgeInRule = edge;
					}				
					// TODO: prüfen, dass löschende Kante!
					if(!(edgeInRule.getAction().getType().equals(Action.Type.DELETE))){
						danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
					}
				}
				else{
					danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
				}
			}
		}
		
		return danglingEdges;
	}



	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	private class ConflictAtom{

		// in Algo Zeile 6 wird ein Atom mit den Parametern candidate und reasons initilisiert.
		// dennoch ist die Datenstruktur noch nicht klar!
		public ConflictAtom(Span candidate, List<Span> reasons) {
			// TODO Auto-generated constructor stub
		}
		
	}
	
	
	public Span newSpan(Mapping nodeInRule1Mapping, Graph s1, Mapping nodeInRule2Mapping){
		return new Span(nodeInRule1Mapping, s1, nodeInRule2Mapping);
	}
	
	public Span newSpan(List<Mapping> rule1Mappings, Graph s1, List<Mapping> rule2Mappings){
		return new Span(rule1Mappings, s1, rule2Mappings);
	}
	
	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	
	//Generell: Laut Definition 3 ist ein Span z.B. C1<-incl-S1-match->L2
	//		d.h. drei Graphen verbunden über eine Inklusion und einen Match
	
	public class Span{
		
		List<Mapping> mappingsInRule1;
		List<Mapping> mappingsInRule2;
		
		Graph graph;

		public Span(Mapping nodeInRule1Mapping, Graph s1, Mapping nodeInRule2Mapping) {
			this.graph = s1;
			mappingsInRule1 = new LinkedList<Mapping>();
			mappingsInRule1.add(nodeInRule1Mapping);
			mappingsInRule2 = new LinkedList<Mapping>();
			mappingsInRule2.add(nodeInRule2Mapping);
		}

		public Span(List<Mapping> rule1Mappings, Graph s1, List<Mapping> rule2Mappings) {
			this.mappingsInRule1 = rule1Mappings;
			this.mappingsInRule2 = rule2Mappings;
			this.graph = s1;
		}

		public Graph getGraph() {
			return graph;			
		}

		//TODO use "getMappingWithImage(...)" method
		public Mapping getMappingInRule1(Node node) {
			for(Mapping mapping : mappingsInRule1){
				if(mapping.getOrigin() == node)
					return mapping;
			}
			return null;
		}

		//TODO use "getMappingWithImage(...)" method
		public Mapping getMappingInRule2(Node node) {
			for(Mapping mapping : mappingsInRule2){
				if(mapping.getOrigin() == node)
					return mapping;
			}
			return null;
		}
		
	}

	
	
	public PushoutResult newPushoutResult(Rule rule1, Span span, Rule rule2) {
		return new PushoutResult(rule1, span, rule2);
	}
	
	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	
	// Generell: muss die matches m1 und m2 aus L1 und L2 enthalten und somit auch G.
	// daher kennt es oder referenziert es auch (indirekt?) die beiden Regeln
	public class PushoutResult{
		
		/**
		 * @return the mappingsOfRule1
		 */
		public List<Mapping> getMappingsOfRule1() {
			return mappingsOfRule1;
		}

		/**
		 * @return the mappingsOfRule2
		 */
		public List<Mapping> getMappingsOfRule2() {
			return mappingsOfRule2;
		}
		
		/**
		 * @return the resultGraph
		 */
		public Graph getResultGraph() {
			return resultGraph;
		}

		Graph resultGraph;
		
		private List<Mapping> mappingsOfRule1;
		private List<Mapping> mappingsOfRule2;
		
		//überflüssig!
//		private Mapping mapping1;
//		private Mapping mapping2;

		public PushoutResult(Rule rule1, Span s1, Rule rule2) {

//			Da es mutmaßlich nur ein 
//			- kopieren der beiden linken Seiten
//				- dabei aufbauen der mappings
//			- entfernen der "doppelten" Knoten und Kanten aus L2
//			- zusammenfassen der Mengen 
//			ist, kann dies alles auch im Konstruktor ablaufen!
			
			// EGraph.copy(...) ist ungeeignet, da es nicht ermöglicht die cross references abzurufen.
			Graph lhsOfRule1 = rule1.getLhs();
//			Mapping mapping1 = henshinFactory.createM
			mappingsOfRule1 = new LinkedList<Mapping>();
			
//			if (copies==null) {
			//TODO: extract to Method
				Copier copierForRule1 = new Copier();
				Graph copyOfLhsOfRule1 = (Graph) copierForRule1.copy(lhsOfRule1);
//				copier.copyAll(getRoots());
				copierForRule1.copyReferences();
//				copies = copier;
				for(Node node : lhsOfRule1.getNodes()){
					Node copyResultNode = (Node) copierForRule1.get(node);
					Mapping createdMapping = henshinFactory.createMapping(node, copyResultNode);
					mappingsOfRule1.add(createdMapping);					
				}
				
				Graph lhsOfRule2 = rule2.getLhs();
				mappingsOfRule2 = new LinkedList<Mapping>();
				Copier copierForRule2 = new Copier();
				Graph copyOfLhsOfRule2 = (Graph) copierForRule2.copy(lhsOfRule2);
//				copier.copyAll(getRoots());
				copierForRule2.copyReferences();
//				copies = copier;
				for(Node node : lhsOfRule2.getNodes()){
					Node copyResultNode = (Node) copierForRule2.get(node);
					Mapping createdMapping = henshinFactory.createMapping(node, copyResultNode);
					mappingsOfRule2.add(createdMapping);
				}
				
				/* TODO:
				 * 
				 *  
				 */
				Graph s1Graph = s1.getGraph();
				// replace common nodes in copyOfRule2 with the one in copyOfRule1
				for(Node node : s1Graph.getNodes()){
					// retarget associated edges
						// get associated node and mapping in both copies
					Mapping mappingInRule1 = s1.getMappingInRule1(node); //TODO: deal with NPE!!!
					Mapping mappingInRule2 = s1.getMappingInRule2(node); //TODO: deal with NPE!!!
					//identify node in copy of rule1
					Node nodeInL1 = mappingInRule1.getImage();//TODO: deal with NPE!!!
					Mapping mappingFromL1ToOverlapGraph = getMappingOfOrigin(mappingsOfRule1, nodeInL1);
					Node nodeOfL1PartOfOverlap = mappingFromL1ToOverlapGraph.getImage();
					//identify node in copy of rule2
					Node nodeInL2 = mappingInRule2.getImage();//TODO: deal with NPE!!!
					Mapping mappingFromL2ToOverlapGraph = getMappingOfOrigin(mappingsOfRule2, nodeInL2);
					Node nodeOfL2PartOfOverlap = mappingFromL2ToOverlapGraph.getImage();
					// alle Kanten von nodeInL2 auf nodeInL1 umhängen
					//TODO: replace by improved version based on:
					// http://stackoverflow.com/questions/18448671/how-to-avoid-concurrentmodificationexception-while-removing-elements-from-arr
					List<Edge> incomingEdgesInL2 = new LinkedList<Edge>();
					for(Edge incomingEdge : nodeOfL2PartOfOverlap.getIncoming()){
						incomingEdgesInL2.add(incomingEdge);
					}
					for(Edge incomingEdge : incomingEdgesInL2){
						incomingEdge.setTarget(nodeOfL1PartOfOverlap);
					}

					List<Edge> outgoingEdgesOfL2 = new LinkedList<Edge>();
					for(Edge outgoingEdge : nodeOfL2PartOfOverlap.getOutgoing()){
						outgoingEdgesOfL2.add(outgoingEdge);
					}
					for(Edge outgoingEdge : outgoingEdgesOfL2){
						outgoingEdge.setSource(nodeOfL1PartOfOverlap);
					}					
					//mappingFromOverlapGraphToL2 anpassen - nodeInL1 als neues "image" setzen.
					mappingFromL2ToOverlapGraph.setImage(nodeOfL1PartOfOverlap);
					//prüfen, dass Knoten "nodeInL2" keine ein- oder ausgehenden KAnten mehr hat.
					if(nodeOfL2PartOfOverlap.getAllEdges().size() > 0){
						System.err.println("All Edges of should have been removed, but still "+nodeInL2.getAllEdges().size()+" are remaining!");
					}
					//löschen des Knoten "nodeInL2y"
					Graph graphOfNodeL2 = nodeOfL2PartOfOverlap.getGraph();
					boolean equalGraph = (copyOfLhsOfRule2 == graphOfNodeL2);
					boolean removedNode = graphOfNodeL2.removeNode(nodeOfL2PartOfOverlap);
					System.out.println("removedNode: "+removedNode);
				}
				// move all edges and nodes from copyOfLhsOfRule2 to copyOfLhsOfRule1
//				Iterator<Node> iter = copyOfLhsOfRule2.getNodes().iterator();
//				while (iter.hasNext()) {
//				    Node nodeInCopyOfLhsOfRule2 = iter.next();
//				    nodeInCopyOfLhsOfRule2.setGraph(copyOfLhsOfRule1);
//				}
				List<Node> nodesInCopyOfLhsOfRule2 = new LinkedList<Node>();
				System.out.println("copyOfLhsOfRule2.getNodes(): "+copyOfLhsOfRule2.getNodes().size());
				for(Node nodeInCopyOfLhsOfRule2 : copyOfLhsOfRule2.getNodes()){
					nodesInCopyOfLhsOfRule2.add(nodeInCopyOfLhsOfRule2);
				}
				for(Node nodeInCopyOfLhsOfRule2 : nodesInCopyOfLhsOfRule2){
					nodeInCopyOfLhsOfRule2.setGraph(copyOfLhsOfRule1);
				}
				
				List<Edge> edgesInCopyOfLhsOfRule2 = new LinkedList<Edge>();
				for(Edge edgeInCopyOfLhsOfRule2 : copyOfLhsOfRule2.getEdges()){
					edgesInCopyOfLhsOfRule2.add(edgeInCopyOfLhsOfRule2);
				}
				for(Edge edgeInCopyOfLhsOfRule2 : edgesInCopyOfLhsOfRule2){
					edgeInCopyOfLhsOfRule2.setGraph(copyOfLhsOfRule1);
				}
				// check that NO edges and nodes are remaining in copyOfLhsOfRule2
				if(copyOfLhsOfRule2.getEdges().size() > 0){
					System.err.println(copyOfLhsOfRule2.getEdges().size()+" edges reaming in "+copyOfLhsOfRule2+", but should be 0");
				}
				if(copyOfLhsOfRule2.getNodes().size() > 0){
					System.err.println(copyOfLhsOfRule2.getNodes().size()+" nodes reaming in "+copyOfLhsOfRule2+", but should be 0");
				}
				// check that the number of edges and nodes in edgeInCopyOfLhsOfRule1 is correct
				int numberOfExpectedNodes = (lhsOfRule1.getNodes().size() + lhsOfRule2.getNodes().size() - s1Graph.getNodes().size());
				if(copyOfLhsOfRule1.getNodes().size() != numberOfExpectedNodes){
					System.err.println("Amount of nodes in created result graph of pushout not as expected. Difference: "+ (copyOfLhsOfRule1.getNodes().size()-numberOfExpectedNodes));
				}
				// set copyOfLhsOfRule1 as resultGraph
				resultGraph = copyOfLhsOfRule1;
			
		}

		private Mapping getMappingOfOrigin(List<Mapping> mappingsOfRules, Node origin) {
			for(Mapping mapping : mappingsOfRules){
				if(mapping.getOrigin() == origin)
					return mapping;
			}
			return null;
		}

//		public Match getMatch1() {
//			// TODO even return NULL?
//			return match1;
//		}
//
//		public Match getMatch2() {
//			// TODO even return NULL?
//			return match2;
//		}
		
	}
	

}
