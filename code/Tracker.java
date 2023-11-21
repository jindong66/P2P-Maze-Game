/*
1. on the first terminal
javac StressTest.java GameInterface.java TrackerInterface.java Tracker.java GameState.java PlayerScore.java Constants.java Chessboard.java RankPanel.java MainGUI.java Game.java

java Tracker 1023 15 10

2. on the another terminal
java StressTest 127.0.0.1 1023 "java Game"

3. later, follow the instructions on the terminal of StressTest, step by step

java Game 127.0.0.1 1023 a1
*/

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tracker implements TrackerInterface{
	
	public static final int NAME_LENGTH = 2;
	private int N;
	private int K;
	private Map<String, GameInterface> playersMap;
	
	
	public Tracker(int N, int K, int port){
        this.N = N;
        this.K = K;
        this.playersMap = new HashMap<>();
    }

	public static void main(String[] args) {
		/*
		 * Get parameters from command-line arguments.
		 * */
		if(args.length != 3){
            System.err.println("The number of parameters are incorrect! Please follow this format: java Tracker [port-number] [N] [K]");
            System.exit(0);
            return;
        }
		
		int port;
		int N;
		int K;
		
		try {
			port = Integer.parseInt(args[0]);
            N = Integer.parseInt(args[1]);
            K = Integer.parseInt(args[2]);
		}catch (NumberFormatException e) {
            System.err.println("Failed to get required integer parameters. Please follow this format: java Tracker [port-number] [N] [K]");
            System.exit(0);
            return;
        }
		
		/*
		 * RMI registry
		 * */
		Registry registry = null;
		TrackerInterface stub = null;
		try {
			Tracker tracker = new Tracker(N, K, port);
			stub = (TrackerInterface) UnicastRemoteObject.exportObject(tracker, port);
			registry = LocateRegistry.createRegistry(port);
			registry.bind("Tracker", stub);
			System.out.println("Tracker ready");
		} catch (Exception e){
			try {
				e.printStackTrace();
				registry.unbind("Tracker");
				registry.bind("Tracker", stub);
				System.err.println("Tracker ready");
			} catch (Exception ee) {
				System.err.println("Server exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}
	
	@Override
	public synchronized Map<String, GameInterface> registerPlayer(String name, GameInterface stub){
        if(!playersMap.containsKey(name) && name.length() == NAME_LENGTH){
        	playersMap.put(name, stub);
            System.out.println(name + " join the game.");
            return playersMap;
        }
        System.out.println(name + " failed to join.");
        return null;
    }
	
	@Override
	public synchronized void removePlayers(List<String> names) {
		for(String name : names) {
			if (playersMap.containsKey(name)) {
				playersMap.remove(name);
				System.out.println(name + " is removed.");
			}
		}
	}
	
	@Override
	public int getN() {
		return N;
	}
	
	@Override
	public int getK() {
		return K;
	}

}
