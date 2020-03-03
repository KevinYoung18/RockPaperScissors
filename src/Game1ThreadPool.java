import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Player implements Comparable<Player>
{
	private int playerNum;
	private int playerChoice;
	private int playerResult;
	
	Player(int playerNum)
	{
		this.playerNum = playerNum;
	}
	
	@Override
	public int compareTo(Player o) {
		if(this.playerResult == o.getPlayerResult())
			return 0;
		else if(this.playerResult < o.getPlayerResult())
			return -1;
		else
			return 1;
	}
	
	public void incrementResult() {
		playerResult++;
	}
	public void decrementResult() {
		playerResult--;
	}
	public int getPlayerNum() {
		return playerNum;
	}

	public int getPlayerChoice() {
		return playerChoice;
	}

	public void setPlayerChoice(int playerChoice) {
		this.playerChoice = playerChoice;
	}

	public int getPlayerResult() {
		return playerResult;
	}

	public void setPlayerResult(int playerResult) {
		this.playerResult = playerResult;
	}


	
}

public class Game1ThreadPool
{
	volatile static ArrayList<Player> players = new ArrayList<Player>();
	private static Object lock = new Object();
	
	//rock = 0, paper = 1, scissors = 2
	public static void main(String[] args) throws InterruptedException
	{	
		int size = 10000;
		
		long time = System.nanoTime();
		
		boolean foundWinner = false;
		
		//creates a threadpool with the number of processors
		int coreCount = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(coreCount); 
		
		//run first segment from threadpool that initializes and picks rock paper scissors values for players
		for(int i = 0; i < coreCount; i++)
		{
			int hi = (i + 1)  * (size / coreCount);
			int lo = i * (size / coreCount);
			Runnable runSeg = () -> {
				playFirst(lo, hi);
			};
			pool.execute(runSeg);
		}
		//finish remainder
		Runnable runLast =() -> {
				playFirst((size / coreCount)*coreCount, size);
		};
		pool.execute(runLast);
		pool.shutdown();
		pool.awaitTermination(2, TimeUnit.SECONDS);
		
		//total up scores
		runThreads(false);
		foundWinner = pickWinners();
		
		//keep playing games until a single winner remains
		while(!foundWinner) 
		{
			runThreads(true);
			runThreads(false);
			foundWinner = pickWinners();
		}
		System.out.println("Winner: Player " + players.get(0).getPlayerNum());
		System.out.println("\nExecution time(ms): " +(System.nanoTime() - time)/1000000);
	}
	
	//if runPlay == true then run play() threads, else run compare() threads
	public static void runThreads(boolean runPlay) throws InterruptedException
	{
		int coreCount = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(coreCount); 
		
		for(int i = 0; i < coreCount; i++)
		{
			int hi = (i + 1)  * (players.size() / coreCount);
			int lo = i * (players.size() / coreCount);
			Runnable runSeg = () -> {
				if(runPlay)
					play(lo, hi);
				else
					compare(lo, hi);
			};
			pool.execute(runSeg);
		}
		//finish remainder
		Runnable runLast =() -> {
			if(runPlay)
				play((players.size() / coreCount)*coreCount, players.size());
			else
				compare((players.size() / coreCount)*coreCount, players.size());
		};
		pool.execute(runLast);
		pool.shutdown();
		pool.awaitTermination(2, TimeUnit.SECONDS);
	}
	
	//eliminates lowest player results returns true if only one remains
	public static boolean pickWinners()
	{
		players.sort(null);
		
		int lowestVal = players.get(0).getPlayerResult();
		
		
		while(players.size() > 1)
		{
			if(players.get(0).getPlayerResult() == lowestVal)
				players.remove(0);
			else
				break;
			
		}
		
		if(players.size() == 1)
			return true;
		return false;
	}
	
	//initializes players, populates arraylist and, sets playerChoice to a random 
	//		int from 0-3 exclusive representing: rock = 0, paper = 1, scissors = 2
	public  static void playFirst(int lo, int hi) 
	{
		Random rand = new Random();
		
		for(int i = lo; i < hi; i++)
		{
			synchronized(lock) {
				Player newPlayer =  new Player(i);
				newPlayer.setPlayerChoice(rand.nextInt(3));
				players.add(newPlayer);
			}
		}
	}
	
	// sets playerChoice to a random int from 0-3 exclusive representing: rock = 0, paper = 1, scissors = 2
	public  static void play(int lo, int hi) 
	{
		Random rand = new Random();
		
		for(int i = lo; i < hi; i++)
		{
			synchronized(lock) {
				players.get(i).setPlayerChoice(rand.nextInt(3));
				players.get(i).setPlayerResult(0);
			}
		}
	}
	
	//compares playerChoices against all other players and increments or decrements playerResult based
	//		on a win or loss, respectively
	public static synchronized void compare( int lo, int hi)
	{
		for(int i = lo; i < hi; i++)
		{
			for(int j = 0; j < players.size(); j++)
			{
				if(i != j) 
				{
					switch(players.get(i).getPlayerChoice()) 
					{
						case(0):
							if(players.get(j).getPlayerChoice() == 2)
								players.get(i).incrementResult();
							else if(players.get(j).getPlayerChoice() == 1)
								players.get(i).decrementResult();
						break;
						case(1):
							if(players.get(j).getPlayerChoice() == 0)
								players.get(i).incrementResult();
							else if(players.get(j).getPlayerChoice() == 2)
								players.get(i).decrementResult();
						break;
						case(2):
							if(players.get(j).getPlayerChoice() == 1)
								players.get(i).incrementResult();
							else if(players.get(j).getPlayerChoice() == 0)
								players.get(i).decrementResult();
						break;
					}
				}
			}
		}
	}
}

