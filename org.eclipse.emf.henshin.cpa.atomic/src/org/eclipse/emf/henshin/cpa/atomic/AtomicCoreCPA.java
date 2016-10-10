package org.eclipse.emf.henshin.cpa.atomic;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.henshin.interpreter.Assignment;
import org.eclipse.emf.henshin.interpreter.Match;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Rule;

public class AtomicCoreCPA {

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
	
	public List<Span> computeCandidates(Rule rule1, Rule rule2) {
		List<Span> result = new LinkedList<Span>();
		Action deleteAction = new Action(Action.Type.DELETE);
		rule1.getActionEdges(deleteAction);
		rule1.getActionNodes(deleteAction)
		//TODO:
		- elemente sammeln
		- vorkommen der elemente in der LHS von R2
		- erstellen eines Graph (S1) und der Mappings in rule1 und rule2
			Wie programmatisch instanzen des jeweiligen MM erstellen?
			Henshins "MappingImpl" Klasse wirklich geeignet? Oder eher MatchImpl?
		
		// TODO Auto-generated method stub
		return null;
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
		boolean isMatchM1 = findDanglingEdges(rule1, pushoutResult.getMatch1()).isEmpty(); //TODO: über den jeweiligen match sollte doch die Regel auch "erreichbar" sein. Regel als Parameter daher überflüssig.
		// TODO: wie "findDanglingEdges" funktioniert weiß ich noch nicht!
		boolean isMatchM2 = findDanglingEdges(rule2, pushoutResult.getMatch2()).isEmpty();
		return (isMatchM1 && isMatchM2);
	}

	// TODO: bisher nicht weiter spezifiziert!
	private PushoutResult constructPushout(Rule rule1, Rule rule2, Span s1) {
		// TODO einfach nur ein Aufruf des PushoutResult Konstruktor? 
		return null;
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
	//		Funktionalität gibt es sehr wahrscheinlich schon in Henshin.
	//		Ob diese verfügbar ist, oder gemacht werden kann, oder leicht aufzufinden und zu extrahieren ist muss noch geklärt werden!
	//		Die Klasse "DanglingConstraint" macht zwar den Eindruck, dass sie dieses behandelt, aber ich verstehe es nicht!
	private List<Edge> findDanglingEdges(Rule rule, Match match) {
		// TODO Auto-generated method stub
		return null;
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
	
	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	
	//Generell: Laut Definition 3 ist ein Span z.B. C1<-incl-S1-match->L2
	//		d.h. drei Graphen verbunden über eine Inklusion und einen Match
	
	private class Span{
		
	}
	
	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	
	// Generell: muss die matches m1 und m2 aus L1 und L2 enthalten und somit auch G.
	// daher kennt es oder referenziert es auch (indirekt?) die beiden Regeln
	
	private class PushoutResult{
		
		private Match match1;
		private Match match2;

		public Match getMatch1() {
			// TODO even return NULL?
			return match1;
		}

		public Match getMatch2() {
			// TODO even return NULL?
			return match2;
		}
		
	}
	

}
