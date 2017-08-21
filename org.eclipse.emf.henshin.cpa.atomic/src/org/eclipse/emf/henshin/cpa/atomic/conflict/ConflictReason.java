package org.eclipse.emf.henshin.cpa.atomic.conflict;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.henshin.cpa.atomic.PushoutResult;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;

//nur der Vollständigkeit eingeführt und zur Doifferenzierung 
// zur Bildung der entsprechenden Ergebnisse für Abgleiche mit ess. CPA
public class ConflictReason extends InitialConflictReason {
	

	Set<ConflictAtom> additionallyInvolvedConflictAtoms;

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
//		if(true)
//		return false;
		//VORSICHT! durch die Kombination von MCRs und BAs kann es vorkommen, dass zwei CRs trotz unterschiedlicher MCRs den gleichen CR darstellen!
//		if (!super.equals(obj)) {  
//			return false;
//		}
		if (!(obj instanceof ConflictReason)) {
			return false;
		}
		ConflictReason other = (ConflictReason) obj;
		System.err.println("equals() comparison:");
		System.err.println(this.toShortString());
		System.err.println(other.toShortString());
		//superfluous
//		if (!getOuterType().equals(other.getOuterType())) {
//			return false;
//		}
		//VORSICHT! durch die Kombination von MCRs und BAs kann es vorkommen, dass zwei CRs trotz unterschiedlicher MCRs den gleichen CR darstellen!
//		if (additionallyInvolvedConflictAtoms == null) {
//			if (other.additionallyInvolvedConflictAtoms != null) {
//				return false;
//			}
//		} else if (!additionallyInvolvedConflictAtoms.equals(other.additionallyInvolvedConflictAtoms)) {
//			return false;
//		}
		if (graph.getNodes().size() != other.getGraph().getNodes().size()) {
			return false;
		}
		if(additionallyInvolvedConflictAtoms.size() != other.additionallyInvolvedConflictAtoms.size()){
			System.out.println("might help");				
		}
//		if (graph.getEdges().size() != other.getGraph().getEdges().size()) {
//			return false;
//		}
		if (mappingsInRule1.size() != other.getMappingsInRule1().size()) {
			return false;
		}
		if (mappingsInRule2.size() != other.getMappingsInRule2().size()) {
			return false;
		}
		for(Node spanNode : graph.getNodes()){
			/* je Knoten des Graphs über R1 den passenden Knoten aus dem other.Graph identifizieren
			 * dann überprüfen, ob die beiden zugeordneten R2 Knoten die selben (== nicht equals) sind.
			 * sobald dies für einen der Graph Knoten nicht der Fall ist: return true.
			 */
			Mapping mappingFromGraphToRule1 = getMappingIntoRule1(spanNode);
			Node nodeInRule1 = mappingFromGraphToRule1.getImage();
			Mapping mappingOtherGraphToR1 = other.getMappingFromGraphToRule1(nodeInRule1);
			if(mappingOtherGraphToR1 != null){ // if no mapping to the node in R1 exists it might not be a duplicate
				Node otherSpanNode = mappingOtherGraphToR1.getOrigin();  
				Node otherR2Node = other.getMappingIntoRule2(otherSpanNode).getImage(); // could cause a NPE, but would be a bug. (invalide instance)
				Node r2Node = getMappingIntoRule2(spanNode).getImage();
				if(otherR2Node != r2Node){
					System.err.println("ConflictReasons NOT equal");
					return false;
				}
			}else {
				return false;
			}
		}
		return true;
	}

	public ConflictReason(InitialConflictReason initialReason, Node boundaryNodeOfCA, Node usedNodeInLhsOfR2, ConflictAtom additionallyInvolvedConflictAtom) {
//		eigene Kopie des S1 Graph
//		eigene Kopie der Mappings in R1
//		eigene Kopie der Mappings in R1
		super(initialReason); // erledigt alles! 
		
		HenshinFactory henshinFactory = HenshinFactory.eINSTANCE;
		
		//TODO: 
		// lhs boundary node of rule 1
		Node boundaryNodeOfRule1 = additionallyInvolvedConflictAtom.getSpan().getMappingIntoRule1(boundaryNodeOfCA).getImage();
		
		// - hinzufuegen des use-nodes zum graph
		String nameOfNewBoundaryNode = boundaryNodeOfRule1.getName()+"_"+usedNodeInLhsOfR2.getName();
		Node newBoundaryNodeInSpan = henshinFactory.createNode(graph, boundaryNodeOfCA.getType(), nameOfNewBoundaryNode);
		// - mapping erstellen
		Mapping mappingInR1 = henshinFactory.createMapping(newBoundaryNodeInSpan, boundaryNodeOfRule1);
		mappingsInRule1.add(mappingInR1);
		Mapping mappingInR2 = henshinFactory.createMapping(newBoundaryNodeInSpan, usedNodeInLhsOfR2);
		mappingsInRule2.add(mappingInR2);
		// ggf. pruefen, dass es keine zu loeschende Kante gibt und somit kein vollstaendiges atom ist 
		// 		(das waere schon durch die initialReason abgedeckt!!) 
		
		additionallyInvolvedConflictAtoms = new HashSet<ConflictAtom>();
		additionallyInvolvedConflictAtoms.add(additionallyInvolvedConflictAtom);
		//wenn das ursprüngliche "InitialConflictReason initialReason" bereits ein CR ist, 
		// so müssen dessen additionallyInvolvedConflictAtoms auch noch dem neuen CR hinzugefügt werden.
		if(initialReason instanceof ConflictReason){
			additionallyInvolvedConflictAtoms.addAll(((ConflictReason) initialReason).getAdditionallyInvolvedConflictAtoms());
		}
	}

	public ConflictReason(InitialConflictReason initialReason) {
		super(initialReason); // erledigt alles! 
		additionallyInvolvedConflictAtoms = new HashSet<ConflictAtom>();
	}

	public Set<Node> getAllUseNodesOfLhsOfR2OfAllInvolvedConflictAtoms() {
		Set<Node> allUseNodesOfLhsOfR2OfAllInvolvedConflictAtoms = new HashSet<Node>();
		for(Mapping mappingInRule2 : mappingsInRule2){
			allUseNodesOfLhsOfR2OfAllInvolvedConflictAtoms.add(mappingInRule2.getImage());
		}
		return allUseNodesOfLhsOfR2OfAllInvolvedConflictAtoms;
	}

	public Set<Node> getAllActiveInvolvedUseNodesOfLhsOfR2OfAdditionallyInvolvedConflictAtoms() {
		Set<Node> allUseNodesOfLhsOfR2OfAdditionallyInvolvedConflictAtoms = new HashSet<Node>();
		for(ConflictAtom ca : additionallyInvolvedConflictAtoms){
			Set<Mapping> mappingsInRule2 = ca.getSpan().getMappingsInRule2();
			for(Mapping mappingInRule2 : mappingsInRule2){
				System.out.println("bla");
				allUseNodesOfLhsOfR2OfAdditionallyInvolvedConflictAtoms.add(mappingInRule2.getImage());
			}
		}
		return allUseNodesOfLhsOfR2OfAdditionallyInvolvedConflictAtoms;
	}

	public Set<Node> getAllActiveInvolvedUseNodesOfLhsOfR2() {
		Set<Node> allUseNodesOfLhsOfR2 = new HashSet<Node>();
		for(Mapping mappingInR2 : mappingsInRule2){
			allUseNodesOfLhsOfR2.add(mappingInR2.getImage());
		}
		return allUseNodesOfLhsOfR2;
	}

	/**
	 * @return the additionallyInvolvedConflictAtoms
	 */
	public Set<ConflictAtom> getAdditionallyInvolvedConflictAtoms() {
		return additionallyInvolvedConflictAtoms;
	}

	// superfluous
//	private AtomicCoreCPA getOuterType() {
//		return AtomicCoreCPA.this;
//	}

	public PushoutResult getPushoutResult() {
		// TODO vielleicht einführen eines Feldes, womit das einmal erzeugte PoR gehalten wird (anstelle es wiederholt zu erzeugen) 
		// 		- Notwendig für die häufig Nutzung mit dem Comparator
		return new PushoutResult(rule1, this, rule2);
	}
	
}
