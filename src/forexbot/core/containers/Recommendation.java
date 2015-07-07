package forexbot.core.containers;

public class Recommendation {
	
	public Recommendation(){
		
	}
	
	public Recommendation(String name, String decision, int certainty) {
		super();
		this.name = name;
		this.decision = decision;
		this.certainty = certainty;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		
		return "["+name+"] recommendation - "+decision+" certainty: "+certainty;
		
	}


	private String name;
	private String decision;
	private int certainty;
}
