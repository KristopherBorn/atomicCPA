package org.eclipse.emf.henshin.cpa.atomic;

import java.util.Set;

import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;

// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist,
// oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
// Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
public class DependencyAtom {
	Span span;

	/**
	 * @return the span
	 */
	public Span getSpan() {
		return span;
	}

	/**
	 * @return the reasons
	 */
	public Set<Span> getReasons() {
		return reasons;
	}

	Set<Span> reasons;

	// in Algo Zeile 6 wird ein Atom mit den Parametern candidate und reasons initilisiert.
	// dennoch ist die Datenstruktur noch nicht klar!
	public DependencyAtom(Span candidate, Set<Span> reasons) {
		this.span = candidate;
		this.reasons = reasons;
	}
	
	// bisher werden die "reason's" nicht berücksichtigt, 
	// da auch nicht klar ist wozu diese eigentlich da sind um was es sich dabei handelt. 
	@Override
	public String toString(){
		return span.toString();
	}

	public String toShortString() {
		return span.toShortString();
	}

}
