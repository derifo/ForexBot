package forexbot.core.containers;

public class Recommendation {
	
	public Recommendation(){
		
	}
	
	public Recommendation(String decision, int certainty) {
		super();
		this.decision = decision;
		this.certainty = certainty;
	}

	
	public String getDecision() {
		return decision;
	}
	public void setDecision(String decision) {
		this.decision = decision;
	}
	public int getCertainty() {
		return certainty;
	}
	public void setCertainty(int certainty) {
		this.certainty = certainty;
	}
	
	@Override
	public String toString(){
		
		return "Recommendation - "+decision+" certainty: "+certainty;
		
	}

	private String decision;
	private int certainty;
}
