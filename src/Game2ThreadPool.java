import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game2ThreadPool 
{
	
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{

		int size = 1000; 
		
		long time = System.nanoTime();
		
		ArrayList<Player> players = new ArrayList<Player>();
		for(int i = 0; i < size; i++)
			players.add(new Player(i));
		
		Player winner = producer(players);
		System.out.println("Winner: Player " + winner.getPlayerNum());
		
		System.out.println("\nExecution time(ms): " +(System.nanoTime() - time)/1000000);
	}
	
	public static Player producer(ArrayList<Player> players) throws InterruptedException, ExecutionException
	{
		Random rand = new Random();
		int coreCount = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(coreCount);
		ArrayList<Future<Player>> futures = new ArrayList<Future<Player>>();
		
		//loop until one player is left  
		while(true)
		{
			//pick 2 random players from arraylist
			if(players.size() > 1)
			{
				Player player1 = players.remove(rand.nextInt(players.size()));
				Player player2 = players.remove(rand.nextInt(players.size()));
				
				//submit game to threadpool
				Callable<Player> game = () -> {
					return playGame(player1, player2);
				};
				futures.add(pool.submit(game));
			}
			
			
			if(players.size() <= 1)
			{
				//if threads are finished and only one player left
				if(futures.size() == 0)
					break;
		
				//add any game winners back to players, removes their respective future
				ArrayList<Future<Player>> toRemove = new ArrayList<Future<Player>>();
				for(Future<Player> future : futures)
				{
				    if(future.isDone())
				    {
				    	players.add(future.get());
				    	toRemove.add(future);
				    }
				    
				}
				futures.removeAll(toRemove);
				
				//if no players were added back to the arraylist, wait for futures(0) to complete
				if(players.size() <= 1)
				{
					players.add(futures.get(0).get());
					futures.remove(0);
				}
				
			}
			
		}
		pool.shutdown();
		return players.get(0);
	}
	
	//takes two players and plays game of rock paper scissors
	//returns winner of game
	public static Player playGame(Player player1, Player player2) throws Exception
	{
		Random rand = new Random();
		//make players play until there is no ties
		do
		{
			player1.setPlayerChoice(rand.nextInt(3));
			player2.setPlayerChoice(rand.nextInt(3));
		}
		while(player1.getPlayerChoice() == player2.getPlayerChoice());
		
		//Chooses winner of rock paper scissors based on:
		//		rock = 0, paper = 1, scissors = 2
		switch(player1.getPlayerChoice()) 
		{
			case(0):
				if(player2.getPlayerChoice() == 2)
					return player1;
				else if(player2.getPlayerChoice() == 1)
					return player2;
			break;
			case(1):
				if(player2.getPlayerChoice() == 0)
					return player1;
				else if(player2.getPlayerChoice() == 2)
					return player2;
			break;
			case(2):
				if(player2.getPlayerChoice() == 1)
					return player1;
				else if(player2.getPlayerChoice() == 0)
					return player2;
			break;
		}
		
		throw new Exception("playerChoice must be 0, 1, or 2");
	}
}
//class Player
//{
//	private int playerNum;
//	private int playerChoice;
//	
//	Player(int playerNum)
//	{
//		this.playerNum = playerNum;
//	}
//	
//	public int getPlayerNum() {
//		return playerNum;
//	}
//
//	public int getPlayerChoice() {
//		return playerChoice;
//	}
//
//	public void setPlayerChoice(int playerChoice) {
//		this.playerChoice = playerChoice;
//	}

