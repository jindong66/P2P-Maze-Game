

import java.awt.Point;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game implements GameInterface{
	
	private String name;
	private int identity;
	private GameState gameState;
	private MainGUI gui;
	
	private TrackerInterface tracker = null;
	private Map<String, GameInterface> stubs;
	
	private ScheduledExecutorService executor;
	
	public Game(String name, TrackerInterface tracker) {
		this.name = name;
		this.identity = 0;
		this.gameState = null;
		this.gui = null;
		
		this.tracker = tracker;
		this.stubs = new HashMap<>();
		
		this.executor = null;
	}

	public static void main(String[] args) {
		/*
		 * Get parameters from command-line arguments.
		 * */
		if(args.length != 3){
            System.err.println("The number of parameters are incorrect! Please follow this format: java Game [IP-address] [port-number] [player-id]");
            System.exit(0);
            return;
        }
		
		String trackerIP;
		int trackerPort;
		String playerName;
		
		trackerIP = args[0];
        playerName = args[2];
		try {
			trackerPort = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Failed to get tracker port.");
            System.exit(0);
            return;
        }
		
		/*
		 * Get tracker remote object and registry.
		 * */
		Registry trackerRegistry;
		TrackerInterface tracker = null;
		try {
            trackerRegistry = LocateRegistry.getRegistry(trackerIP, trackerPort);
            tracker = (TrackerInterface) trackerRegistry.lookup("Tracker");
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Failed to locate Tracker");
            System.exit(0);
        }
		
		/*
		 * Create Game and stub.
		 * */
		Game game = new Game(playerName, tracker);
		GameInterface stub = null;
		try {
			stub = (GameInterface) UnicastRemoteObject.exportObject(game, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		/*
		 * Register.
		 * */
		if (!game.register(stub)) {
			System.exit(0);
		}
		
		/*
		 * Join the game.
		 * */
		game.joinGame();
		
		/*
		 * Start the game.
		 * */
		game.startGame();
	}

	private boolean register(GameInterface stub) {
		try {
			stubs = tracker.registerPlayer(name, stub);
			if (stubs == null) {
				System.out.println(name + " failed to register.");
				return false;
			}
			System.out.println(name + " register successfully.");
			return true;
		} catch (RemoteException e) {
			System.out.println(name + " failed to register.");
			e.printStackTrace();
		}
		return false;
	}

	private void joinGame() {
		System.out.println(name + " join game.");
		List<String> removedPlayers = new ArrayList<>();
		GameInterface primaryServerStub = null;
		GameInterface backupServerStub = null;
		for (String key : stubs.keySet()) {
			GameInterface value = stubs.get(key);
			try {
				int status = value.getIdentity();
				System.out.println(key + "'s status is: " + status);
				if (status == 2) {
					primaryServerStub = value;
				} else if (status == 1) {
					backupServerStub = value;
				}
			} catch (RemoteException e) {
				removedPlayers.add(key);
			}
		}
		if (primaryServerStub != null) {
			try {
				gameState = primaryServerStub.playerJoin(name, stubs.get(name));
			} catch (RemoteException e) {
			}
		}
		if (backupServerStub != null) {
			if (gameState == null) {
				try {
					gameState = backupServerStub.playerJoin(name, stubs.get(name));
				} catch (RemoteException e) {
				}
			} else {
				try {
					backupServerStub.playerJoin(name, stubs.get(name));
				} catch (RemoteException e) {
				}
			}
		}
		if (gameState == null) {
			becomePrimaryServer();
		}
		try {
			tracker.removePlayers(removedPlayers);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		showGUI();
	}
	
	private synchronized void  becomePrimaryServer() {
		identity = 2;
		initGameState();
		System.out.println(name + " becomes primary server.");
		/*
		 * Start server thread.
		 * */
		System.out.println(name + " starts server thread.");
		startPrimaryServerThread();
	}

	private synchronized void startPrimaryServerThread() {
		Runnable task = new Runnable() {
	        public void run() {
	            //System.out.println("Primary thread is running.");
	            /*
	             * Ping all players other than self.
	             * */
	            List<String> deadPlayers = new ArrayList<String>();
	            for (String key : stubs.keySet()) {
	            	if (key.equals(name)){
	            		continue;
	            	}
	            	GameInterface value = stubs.get(key);
	            	try {
						value.ping();
					} catch (RemoteException e) {
						deadPlayers.add(key);
					}
	            }
	            
	            /*
	             * Remove dead players on tracker and primary server.
	             * */
	            try {
					tracker.removePlayers(deadPlayers);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            for (String deadPlayer : deadPlayers) {
	            	stubs.remove(deadPlayer);
	            	gameState.playersPosition.remove(deadPlayer);
	            	gameState.playersScore.remove(deadPlayer);
	            	if (deadPlayer.equals(gameState.backupServerName)) {
	            		gameState.backupServerName = "";
	            	}
	            }

	            /*
	             * Check the existence of backup server. 
	             * Reset backup if there is no backup server.
	             * */
	            if (gameState.backupServerName.equals("")) {
	            	for (String player : stubs.keySet()) {
	            		if (!player.equals(name)) {
	            			try {
								stubs.get(player).becomeBackupServer(gameState, stubs);
								gameState.backupServerName = player;
								break;
							} catch (RemoteException e) {
							}
	            		}
	            	}
	            }
	        }
	    };
	    if (executor != null && !executor.isShutdown()) {
	        executor.shutdownNow();
	    }
	    executor = Executors.newScheduledThreadPool(1);
	    executor.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
	}

	private synchronized void startBackupServerThread() {
		Runnable task = new Runnable() {
	        public void run() {
	            //System.out.println("Backup thread is running.");
	            /*
	             * Ping primary server.
	             * */
	            try {
					stubs.get(gameState.primaryServerName).ping();
				} catch (RemoteException e) {
					System.out.println("Primary Server " + gameState.primaryServerName + " is dead.");
					becomeNewPrimary();
					startPrimaryServerThread();
					//showGUI();
				}
	        }
	    };
	    if (executor != null && !executor.isShutdown()) {
	        executor.shutdownNow();
	    }
	    executor = Executors.newScheduledThreadPool(1);
	    executor.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	protected void becomeNewPrimary() {
		identity = 2;
		stubs.remove(gameState.primaryServerName);
		gameState.playersPosition.remove(gameState.primaryServerName);
		gameState.playersScore.remove(gameState.primaryServerName);
		gameState.primaryServerName = name;
		gameState.backupServerName = "";
		for (String key : stubs.keySet()) {
			if (key.equals(name)) continue;
			GameInterface stub = stubs.get(key);
			try {
				stub.updateGameState(gameState);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void showGUI() {
		if (gui == null) {
			gui = new MainGUI(name, gameState);
		}
		gui.refresh(gameState);
	}

	private void initGameState() {
		gameState = new GameState();
		
		LocalTime currentTime = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		gameState.gameStartTime = currentTime.format(formatter);
		try {
			gameState.N = tracker.getN();
			gameState.K = tracker.getK();
			System.out.println("Get N from tracker: " + gameState.N);
			System.out.println("Get K from tracker: " + gameState.K);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		gameState.primaryServerName = name;
		gameState.backupServerName = "";
		gameState.treasures = new HashSet<Point>();
		while (gameState.treasures.size() < gameState.K) {
			gameState.treasures.add(generatePoint(gameState.N));
		}
		gameState.playersPosition = new HashMap<String, Point>();
		while (true) {
			Point point = generatePoint(gameState.N);
			if (!gameState.treasures.contains(point)) {
				gameState.playersPosition.put(name, point);
				break;
			}
		}
		gameState.playersScore = new HashMap<String, Integer>();
		gameState.playersScore.put(name, 0);
		gameState.TimeStamp = currentTime.format(formatter);
	}

	private Point generatePoint(int N) {
		Random random = new Random();
		int randomX = random.nextInt(N);
        int randomY = random.nextInt(N);
        Point randomPoint = new Point(randomX, randomY);
        return randomPoint;
	}

	private void startGame() {
		System.out.println(name + " start game, please input commands.");
		Scanner scanner = new Scanner(System.in);
		while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if("0".equalsIgnoreCase(line)){
            	System.out.println("Refresh gamestate.");
            	localCommand(0);
            }
            else if("1".equalsIgnoreCase(line)){
            	System.out.println("Move left.");
            	localCommand(1);
            }
            else if("2".equalsIgnoreCase(line)){
            	System.out.println("Move below.");
            	localCommand(2);
            }
            else if("3".equalsIgnoreCase(line)){
            	System.out.println("Move right.");
            	localCommand(3);
            }
            else if("4".equalsIgnoreCase(line)){
            	System.out.println("Move up.");
            	localCommand(4);
            }
            else if("9".equalsIgnoreCase(line)){
            	scanner.close();
            	List<String> removedPlayers = new ArrayList<>();
            	removedPlayers.add(name);
            	try {
					tracker.removePlayers(removedPlayers);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
            	for (GameInterface stub : stubs.values()) {
            	    try {
						stub.playerExit(name);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
            	}
                System.exit(0);
            }
        }
	}

	private void localCommand(int command) {
		String primaryServerName = gameState.primaryServerName;
    	String backupServerName = gameState.backupServerName;
    	GameInterface serverStub = stubs.get(primaryServerName);
    	
		if (command==0) {
			try {
				gameState = serverStub.sendGameState();
			} catch (RemoteException e) {
				serverStub = stubs.get(backupServerName);
				try {
					gameState = serverStub.sendGameState();
				} catch (RemoteException e1) {
					System.err.println("Failed to update Game State.");
					e1.printStackTrace();
				}
			}
		} else {
			try {
				gameState = serverStub.realMove(name, command);
			} catch (RemoteException e) {
				serverStub = stubs.get(backupServerName);
				try {
					gameState = serverStub.realMove(name, command);
				} catch (RemoteException e1) {
					System.err.println("Failed to move on server end.");
					e1.printStackTrace();
				}
			}
		}
		showGUI();
	}

	@Override
	public void playerExit(String name) throws RemoteException {
		System.out.println(name + " exit! delete its information and refresh GUI.");
	}

	@Override
	public void ping() throws RemoteException {
	}

	@Override
	public synchronized int getIdentity() throws RemoteException {
		return identity;
	}

	@Override
	public synchronized GameState playerJoin(String name, GameInterface gameInterface) throws RemoteException {
		stubs.put(name, gameInterface);
		while (true) {
			Point point = generatePoint(gameState.N);
			if (!gameState.playersPosition.containsValue(point) && !gameState.treasures.contains(point)) {
				gameState.playersPosition.put(name, point);
				gameState.playersScore.put(name, 0);
				break;
			}
		}
		return gameState;
	}

	@Override
	public GameState sendGameState() throws RemoteException {
		return gameState;
	}

	@Override
	public synchronized GameState realMove(String name, int command) throws RemoteException {
		Point currPoint = gameState.playersPosition.get(name);
		Point nextPoint = currPoint;
		
		switch(command) {
		case 1:
			nextPoint = new Point(currPoint.x, currPoint.y-1); 
			break;
		case 2:
			nextPoint = new Point(currPoint.x+1, currPoint.y);
			break;
		case 3:
			nextPoint = new Point(currPoint.x, currPoint.y+1);
			break;
		case 4:
			nextPoint = new Point(currPoint.x-1, currPoint.y);
			break;
		}
		
		if (gameState.treasures.contains(nextPoint)) {
			gameState.treasures.remove(nextPoint);
			gameState.playersScore.put(name, 1 + gameState.playersScore.get(name));
			gameState.playersPosition.put(name, nextPoint);
			while (true) {
				Point treasure = generatePoint(gameState.N);
				if (!gameState.treasures.contains(treasure) && !gameState.playersPosition.containsValue(treasure)) {
					gameState.treasures.add(treasure);
					break;
				}
			}
		} else if (gameState.playersPosition.containsValue(nextPoint)) {
			System.out.println("Next position has player and failed to move.");
		} else if (nextPoint.x < 0 || nextPoint.x == gameState.N || nextPoint.y < 0 || nextPoint.y == gameState.N) {
			System.out.println("Next position is outside of the grid. Failed to move.");
		} else {
			gameState.playersPosition.put(name, nextPoint);
		}
		
		/*
		 * Update Backup Server
		 * */
		String backup = gameState.backupServerName;
		try {
			GameInterface backupStub = stubs.get(backup);
			backupStub.updateGameState(gameState);
		} catch (Exception e){
			System.out.println("There is no backup server. No need to update game state.");
		}
		return gameState;
	}

	@Override
	public void updateGameState(GameState gameState) throws RemoteException {
		this.gameState = gameState;
	}

	@Override
	public void becomeBackupServer(GameState gameStateRemote, Map<String, GameInterface> stubsRemote) throws RemoteException {
		identity = 1;
		gameStateRemote.backupServerName = name;
		gameState = gameStateRemote;
		startBackupServerThread();
		stubs = stubsRemote;
	}
}
