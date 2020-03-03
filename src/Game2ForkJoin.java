import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Game2ForkJoin 
{
	
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{

		int size = 10000; 
		
		long time = System.nanoTime();
		
		ForkJoinPool pool = new ForkJoinPool();
		ArrayList<Player> players = new ArrayList<Player>();
		for(int i = 0; i < size; i++)
			players.add(new Player(i));
		
		while(players.size() > 1)
		{
			Game game = new Game(players);
			players = pool.invoke(game);
		}
		System.out.println("Winner: Player " + players.get(0).getPlayerNum());
		System.out.println("\nExecution time(ms): " +(System.nanoTime() - time)/1000000);
	}
}
class Game extends RecursiveTask<ArrayList<Player>>
{

	private static final long serialVersionUID = 9097300832777624240L;
	int maxSize = 100;
	ArrayList<Player> players; 
	
	Game(ArrayList<Player> players)
	{
		this.players = players;
	}
	Game(List<Player> players)
	{
		this.players = new ArrayList<Player>();
		this.players.addAll(players);
	}
	
	@Override
	protected ArrayList<Player> compute() {
		
		if(players.size() < maxSize)
		{
			
			Random rand = new Random();
			while(players.size() > 1)
			{
				Player player1 = players.remove(rand.nextInt(players.size()));
				Player player2 = players.remove(rand.nextInt(players.size()));
				try {
					players.add(playGame(player1, player2));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return players;
		
		}
		//split arrayList into halves and recursively call Game
		else
		{
			int mid = players.size()/2;
			int hi = players.size();
			Game t1 = new Game(players.subList(0, mid));
			Game t2 = new Game(players.subList(mid, hi));
			t1.fork();
			t2.fork();
			ArrayList<Player> finalList = t1.join();
			finalList.addAll(t2.join());
			return finalList;
		}
		
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

