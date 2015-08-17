package forexbot.modules.evolver;

import java.util.ArrayList;

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
	private final int evolution_base;
	
	public EvolutionaryAlgorithm(CycleController cycle_handle, int evolution_base){
		CYCLE_HANDLE = cycle_handle;
		this.evolution_base = evolution_base;
		
		//14400 - number of listings from entire day
		sandbox = new SandboxController(this, this.evolution_base);//for testing purposes 
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
		 * 
		 * Starting values are randomly generated from set range.
		 */
		ID = 0;	
		
		ForexBot.work_frame.PostLog("[AI] Creating initial generation.");
		ForexBot.log.addLogINFO("[AI] Creating initial generation.");
		
		Genom[] tab = new Genom[20];
		generation_size = 20;
		
		ArrayList<Integer> k,d,s;
		k = new ArrayList<Integer>();
		d = new ArrayList<Integer>();
		s = new ArrayList<Integer>();
		
		for(int i = 0; i < generation_size; i++){
			int kk, dd, ss;
			boolean ch;
			//random k
			do{
				ch = true;
				kk = Genom.randInt(k_min, k_max);
				if(i > 0){
					for(int p : k){
						if(p == kk) ch = false;
					}
				}else{
					k.add(kk);
				}
				
			}while(!ch);
			if(i > 0) k.add(kk);
			
			//random d
			do{
				ch = true;
				dd = Genom.randInt(d_min, d_max);
				if(i > 0){
					for(int p : d){
						if(p == dd) ch = false;
					}
				}else{
					d.add(dd);
				}
				
			}while(!ch);
			if(i > 0) d.add(dd);
			
			//random s
			do{
				ch = true;
				ss = Genom.randInt(slow_min, slow_max);
				if(i == 10){
					s = new ArrayList<Integer>();
				}
				if(i > 0 && i != 10){
					for(int p : s){
						if(p == ss) ch = false;
					}
				}else{
					s.add(ss);
				}
				
			}while(!ch);
			if(i > 0) s.add(ss);
			
			tab[i] = new Genom(getID(), kk, dd, ss);
			
		}
		
		sandbox.addNewGeneration(tab);
	}
	
	public void CreateNewPopulation(Genom[] old_population){
		/*
		 * New population is created from previous one based on tournament selection method,
		 * from array of genoms pairs are selected (array is unsorted) and best one is passed further.
		 * 
		 * Outcome of tournament selection is then sorted and crossed, weakest ones are mutated in order 
		 * to ensure diversity of generation.
		 */
		ForexBot.work_frame.PostLog("[AI] Creating new generation.");
		ForexBot.log.addLogINFO("[AI] Creating new generation.");
		
		ID += 1000000;//designation for next generation (0-gen 1,2,3.. 1-gen 1000001,1000002... )
		generation_number++;
		//--------------------------------------
		//select best genoms using tournament method selection (best of each pair, genoms in array are random)
		int rounds = old_population.length /2;
		Genom[] g = new Genom[rounds];
		
		ArrayList<Genom> tournament = new ArrayList<Genom>();
		for(Genom pom : old_population){
			tournament.add(pom);
		}
		
		
		int n = 0;
		for(int i = 0; i < rounds; i++){
			Genom A,B;
						
			int rnd1 = Genom.randInt(0, tournament.size()-1);
			A = tournament.get(rnd1);
			tournament.remove(rnd1);
			
			int rnd2 = Genom.randInt(0, tournament.size()-1);
			B = tournament.get(rnd2);
			tournament.remove(rnd2);
			
			
			if(A.getEvaluation() > B.getEvaluation()) g[i] = A;
			else g[i] = B;
		}
		//create new genoms out of best from old population
		Genom[] newP = new Genom[generation_size];
		//sort
		for(int i = 0; i < g.length; i++){
			Genom bst = g[i];
			for(int j = i+1; j < g.length; j++){
				if(bst.getEvaluation() < g[j].getEvaluation()){
					bst = g[j];
					g[j] = g[i];
					g[i] = bst;
				}
			}
			
		}
		
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
		/*
		 * This function closes the loop, based on results from processing generation scores
		 * appropriate course of action is taken.
		 * 
		 * There are two sets of boundary conditions that will either break the loop or restart
		 * EA in order to create new initial conditions (ex. in case of no further improvement or
		 * in case of failure to obtain positive score)  
		 */
		
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
				if(best.getEvaluation() >= 1000)	CYCLE_HANDLE.EnableTrade();//at least 1% income in simulation
			}else{
				imp_counter++;
			}
		}else{
			fail_counter++;
		}
		
		if(imp_counter > 10){
			//in case of no improvements for long time - suspend EA
			ForexBot.work_frame.PostLog("[AI] No improvement in 10 simulations - suspending.");
			Restart();			
		}
		else if(fail_counter > 10){
			//in case of failure - end work cycle
			if(fail_generation_counter < 10){
				ForexBot.work_frame.PostLog("[AI] Failed to recive positive score in 20 simulations - restarting.");
				fail_generation_counter++;
				Restart();	
			}else{
				ForexBot.work_frame.PostLog("[AI] Failed to recive positive score in 10 generations - exiting.");
				CYCLE_HANDLE.StopCycle();
				ForexBot.work_frame.LockButtons(false, true, true, false);
				Sleep(true);
			}
		}
	}

	@Override
	public void run() {
		// Main loop 
		
		ForexBot.log.addLogINFO("[AI] Machine learning module started - initializing evolutionary tree...");
		ForexBot.work_frame.PostLog("[AI] Machine learning module started.");
		
		int sleep_counter = 60000;
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
				ForexBot.work_frame.PostLog("[AI] - sleep mode...");
			}
			//------------------------------------------------
			try {
				Thread.sleep(sleep_counter);
			} catch (InterruptedException e1) {
				if(ForexBot.DEBUG) e1.printStackTrace();
			}
			
			if(restart) break;
			
		}while(!ForexBot.GLOBAL_EXIT);
		
		if(restart){
			ForexBot.EvolverRestart(evolution_base +1000);
		}else{
			if(ForexBot.DEBUG) System.out.println("Evolver terminated - global exit!");
		}
		
		
		
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
	
	private static int fail_generation_counter = 0;
	
	private boolean restart;
	
	private int generation_size;
	
	private int ID = 0;	
	private int getID(){
		ID++;
		return ID;
	}
	
}
