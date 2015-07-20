package forexbot.modules.evolver;

import forexbot.ForexBot;
import forexbot.core.CycleController;
import forexbot.modules.evolver.containers.Genom;

public class EvolutionaryAlgorithm implements Runnable{
	/*
	 * This class is based on principles of artificial intelligence evolutionary algorithm (EA)
	 * by creating initial population using random indicator parameters it's goal is to find
	 * best possible combination of this parameters witch generates most profits
	 * 
	 * Work cycle is as follows:
	 * 	1 create population
	 *  2 evaluate
	 *  3 choose best instances 
	 *  4 generate new population and go back to 2
	 *  
	 *  This works until achieving satisfactory results or proving that this set of indicators can't generate sufficient 
	 *  profit margin.
	 */
	
	
	//Set of min and max values for indicator periods
	//parameters where chosen arbitrarily but with regard to indicator formulas  
	public static final int k_min = 2, k_max = 21, d_min = 2, d_max = 21, slow_min = 1, slow_max = 10;
	
	public EvolutionaryAlgorithm(CycleController cycle_handle){
		CYCLE_HANDLE = cycle_handle;
		
		//14400 - number of listings from entire day
		sandbox = new SandboxController(this, 1000);//for testing purposes 
		generation_number = 0;
		
		cache_downloaded = false;
		first_generation = true;
		sleep = false;
		restart = false;
	}

	public void Sleep(boolean sleep){
		this.sleep = sleep;
		if(sleep = true) ForexBot.log.addLogINFO("[AI] Entering sleep mode...");
		else ForexBot.log.addLogINFO("[AI] Exiting sleep mode...");
	}
	
	public void Restart(){
		this.restart = true;
		
		ForexBot.log.addLogINFO("[AI] Restarting algorithm...");
		ForexBot.work_frame.PostLog("[AI] Restarting algorithm...");
		
	}
	
	//------------------------------------------------------
	public void CreateInitialPopulation(){
		/*
		 * Method creates initial generation for EA using predefined range of numbers
		 * then generation is passed down to sandbox controller
		 */
		ID = 0;	
		
		ForexBot.work_frame.PostLog("[AI] Creating initial generation.");
		ForexBot.log.addLogINFO("[AI] Creating initial generation.");
		
		Genom[] tab = new Genom[20];
		generation_size = 20;
		
		int p = slow_min;
		boolean t = false;
		for(int i = k_min; i <= k_max; i++){
			
			tab[i-k_min] = new Genom(getID(), i, i, p);
			if(t){
				p++;
				t = false;
			}else{
				t = true;
			}

		}
		
		sandbox.addNewGeneration(tab);
	}
	
	public void CreateNewPopulation(Genom[] old_population){
		ForexBot.work_frame.PostLog("[AI] Creating new generation.");
		ForexBot.log.addLogINFO("[AI] Creating new generation.");
		
		ID += 1000000;//designation for next generation (0-gen 1,2,3.. 1-gen 1000001,1000002... )
		generation_number++;
		//--------------------------------------
		//select best genoms using tournament method selection (best of each pair, genoms in array are random)
		int rounds = old_population.length /2;
		Genom[] g = new Genom[rounds];
		
		int n = 0;
		for(int i = 0; i < rounds; i++){
			Genom A,B;
			A = old_population[n];
			n++;
			B = old_population[n];
			n++;
			
			if(A.getEvaluation() > B.getEvaluation()) g[i] = A;
			else g[i] = B;
		}
		//create new genoms out of best from old population
		Genom[] newP = new Genom[generation_size];
		
		//cross
		n = 0;
		for(int j = 0; j < g.length - 1; j++){
			Genom Ap = Genom.Cross(g[j], g[j+1]);
			Ap.FormatGenom(getID());
			newP[n] = Ap;
			n++;
			
			Genom Bp = Genom.Cross(g[j+1], g[j]);
			Bp.FormatGenom(getID());
			newP[n] = Bp;
			n++;
		}
		
		//mutation
		int rnd = Genom.randInt(0, g.length-1);
		Genom m = Genom.Mutate(g[rnd]);
		m.FormatGenom(getID());
		newP[n] = m;
		n++;

		rnd = Genom.randInt(0, g.length-1);
		m = Genom.Mutate(g[rnd]);
		m.FormatGenom(getID());
		newP[n] = m;
		n++;
		
		sandbox.addNewGeneration(newP);
	}

	public Genom[] Evolve(){
		//initialize generation variables
		sandbox.InitiateGeneration();
		//process simulations
		if(sandbox.ProcessGeneration()){
			//evaluate outcome
			Genom[] p = sandbox.EvaluateGeneration();
			//return evaluated
			return p;
		}else{
			
			return null;
		}
	}
	
	public void ProcessOutcome(Genom[] present_generation){
		
		Genom best = present_generation[0];
		//find best specimen in current generation 
		for(Genom g : present_generation){
			if(best.getEvaluation() < g.getEvaluation()) best  = g;
		}
		
		ForexBot.log.addLogINFO("[AI] Generation: #"+generation_number+" Best specimen: "+best.toString());
		
		//assess score
		if(best.getEvaluation() > 0){
			if(CYCLE_HANDLE.getScore() < best.getEvaluation()){
				//pass best result to main cycle and enable trade (for positive score)
				CYCLE_HANDLE.EvolverInput(best);
				if(best.getEvaluation() >= 25)	CYCLE_HANDLE.EnableTrade();//at least 2,5% income in simulation
			}else{
				imp_counter++;
			}
		}else{
			fail_counter++;
		}
		
		if(imp_counter > 10){
			//in case of no improvements for long time - suspend EA
			ForexBot.work_frame.PostLog("[AI] No improvement in 10 simulations - suspending.");
			Sleep(true);
		}
		else if(fail_counter > 20){
			//in case of failure - end work cycle
			ForexBot.work_frame.PostLog("[AI] Failed to recive positive score in 20 simulations - exiting.");
			CYCLE_HANDLE.StopCycle();
			ForexBot.work_frame.LockButtons(false, true, true, false);
			Sleep(true);
		}
	}

	@Override
	public void run() {
		// Main loop 
		
		ForexBot.log.addLogINFO("[AI] Machine learning module started - initializing evolutionary tree...");
		ForexBot.work_frame.PostLog("[AI] Machine learning module started.");
		
		int sleep_counter = 6000;
		Genom[] present_generation = null;
		
		do{
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}
			//before first generation can be processed, listings form database have to be downloaded
			if(!sleep){
				if(first_generation){
					
					//check if listings are downloaded
					if(cache_downloaded){
						//listings cache - ok! Process first generation
						//Create first generation
						CreateInitialPopulation();
						//evolve and set as present
						present_generation = Evolve();
						//process evaluated generation
						if(present_generation != null){
							ProcessOutcome(present_generation);
							//disable first_generation flag
							first_generation = false;
						}
						
					}else{
						//cache not created, attempt downloading
						try {
							sandbox.LoadDataFromDBC();
							cache_downloaded = true;
							sleep_counter = 6000;
						} catch (Exception e) {
							//in case of insufficient records in dbc, exit loop (wait)
							ForexBot.log.addLogINFO("[AI] Not enough listings in database - waiting...");
							sleep_counter = 60000;
						}
						
					}			
					
				}else{
					//first generation was already processed, create and process next generation
					CreateNewPopulation(present_generation);
					//evolve and set as present
					present_generation = Evolve();
					//process evaluated generation
					ProcessOutcome(present_generation);
					
				}			
			}else{
				ForexBot.log.addLogDEBUG("[AI] - sleep mode...");
			}
			//------------------------------------------------
			try {
				Thread.sleep(sleep_counter);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}
			
			//if(generation_number == 10) Sleep(true);//
			
		}while(!ForexBot.GLOBAL_EXIT);
		
		if(ForexBot.DEBUG) System.out.println("Evolver terminated - global exit!");
		
	}
	
	public int getGenerationNUmber(){
		return generation_number;
	}
	
	private CycleController CYCLE_HANDLE;
	private SandboxController sandbox;
	private int generation_number ;
	private boolean cache_downloaded;
	private boolean first_generation;
	private boolean sleep;
	private int imp_counter, fail_counter;
	@SuppressWarnings("unused")//for future use
	private boolean restart;
	
	private int generation_size;
	
	private int ID = 0;	
	private int getID(){
		ID++;
		return ID;
	}
	
}
