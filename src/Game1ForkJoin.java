import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;


//compares playerChoices against all other players and increments or decrements playerResult based
//		on a win or loss, respectively
class CompareGames extends RecursiveTask<ArrayList<Player>>
{
	
	private static final long serialVersionUID = 1376815006696466886L;
	private List<Player> subList;
	private int maxSize = 50;
	private ArrayList<Player> playerList;
	
	CompareGames(ArrayList<Player> playerList, List<Player> subList)
	{
		this.playerList = playerList;
		this.subList = subList;
	}
	
	@Override
	protected ArrayList<Player> compute() 
	{
		if(subList.size() < maxSize)
		{
			subList.forEach((player) ->{
				for(int i = 0; i < playerList.size(); i++)
				{
					switch(player.getPlayerChoice()) 
					{
						case(0):
							if(playerList.get(i).getPlayerChoice() == 2)
								player.incrementResult();
							else if(playerList.get(i).getPlayerChoice() == 1)
								player.decrementResult();
						break;
						case(1):
							if(playerList.get(i).getPlayerChoice() == 0)
								player.incrementResult();
							else if(playerList.get(i).getPlayerChoice() == 2)
								player.decrementResult();
						break;
						case(2):
							if(playerList.get(i).getPlayerChoice() == 1)
								player.incrementResult();
							else if(playerList.get(i).getPlayerChoice() == 0)
								player.decrementResult();
						break;
					}
					
				}
			});
			
			ArrayList<Player> result = new ArrayList<Player>();
			result.addAll(subList);
			return result;
		}
		else
		{
			int mid = subList.size()/2;
			int hi = subList.size();
			CompareGames t1 = new CompareGames(playerList, subList.subList(0, mid));
			CompareGames t2 = new CompareGames(playerList, subList.subList(mid, hi));
			t1.fork();
			t2.fork();
			ArrayList<Player> finalList = t1.join();
			finalList.addAll(t2.join());
			
			return finalList;
		}
		
	}
	
	
}
class PlayGames extends RecursiveTask<ArrayList<Player>>
{
	
	private static final long serialVersionUID = -5523562140509923499L;
	private List<Player> subList;
	private int maxSize = 200;
	private ArrayList<Player> playerList;
	
	PlayGames(ArrayList<Player> playerList, List<Player> subList)
	{
		this.playerList = playerList;
		this.subList = subList;
	}
	
	@Override
	protected ArrayList<Player> compute() 
	{
		if(subList.size() < maxSize)
		{
			Random rand = new Random();
			subList.forEach((player) ->{
				player.setPlayerChoice(rand.nextInt(3));
				player.setPlayerResult(0);
			});
			
			ArrayList<Player> result = new ArrayList<Player>();
			result.addAll(subList);
			return result;
		}
		else
		{
			int mid = subList.size()/2;
			int hi = subList.size();
			PlayGames t1 = new PlayGames(playerList, subList.subList(0, mid));
			PlayGames t2 = new PlayGames(playerList, subList.subList(mid, hi));
			t1.fork();
			t2.fork();
			ArrayList<Player> finalList = t1.join();
			finalList.addAll(t2.join());
			
			return finalList;
		}
		
	}
	
	
}

public class Game1ForkJoin
{
	static ArrayList<Player> players = new ArrayList<Player>();
	//rock = 0, paper = 1, scissors = 2
	public static void main(String[] args) throws InterruptedException
	{	
		int size = 10000;
		
		long time = System.nanoTime();
		
		//populate arraylist of players
		for(int i = 0; i <size; i++)
			players.add(new Player(i));
		
		boolean foundWinner = false;
		ForkJoinPool pool = new ForkJoinPool();
		
		//keep playing games until a single winner remains
		while(!foundWinner) 
		{
			PlayGames p = new PlayGames(players, players);
			players = pool.invoke(p);
			
			CompareGames c = new CompareGames(players, players);
			players = pool.invoke(c);
			
			foundWinner = pickWinners();
		}
		System.out.println("Winner: Player " + players.get(0).getPlayerNum());
		System.out.println("\nExecution time(ms): " +(System.nanoTime() - time)/1000000);
	}
	
	//if runPlay == true then run play() threads, else run compare() threads
	
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

	// sets playerChoice to a random int from 0-3 exclusive representing: rock = 0, paper = 1, scissors = 2
	
	
	public static void printArray()
	{
		for(int i = 0; i < players.size(); i++)
			System.out.print(players.get(i).getPlayerResult() + "\t");
	}
}

