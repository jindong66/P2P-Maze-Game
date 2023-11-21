

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface TrackerInterface extends Remote{
	Map<String, GameInterface> registerPlayer(String name, GameInterface stub) throws RemoteException;
	void removePlayers(List<String> names) throws RemoteException;
	int getK() throws RemoteException;
    int getN() throws RemoteException;
}
