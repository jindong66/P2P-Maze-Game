

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface GameInterface extends Remote{

	void playerExit(String name) throws RemoteException;

	void ping() throws RemoteException;

	int getIdentity() throws RemoteException;

	GameState playerJoin(String name, GameInterface gameInterface) throws RemoteException;

	GameState sendGameState() throws RemoteException;

	GameState realMove(String name, int command) throws RemoteException;

	void updateGameState(GameState gameState) throws RemoteException;

	void becomeBackupServer(GameState gameState, Map<String, GameInterface> stubs) throws RemoteException;

}
