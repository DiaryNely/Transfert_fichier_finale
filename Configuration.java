package methode;

import server.*;
import client.*;

import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private static Configuration instance;
    private List<ServerPrimary> primaryServers;
    private ServerIntermediate serverIntermediate;
    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private Configuration() {
        primaryServers = new ArrayList<>();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public List<ServerPrimary> getPrimaryServers() {
        return primaryServers;
    }

    public void setPrimaryServers(List<ServerPrimary> primaryServers) {
        this.primaryServers = primaryServers;
    }

    public ServerIntermediate getServerIntermediate() {
        return serverIntermediate;
    }

    public void setServerIntermediate(ServerIntermediate serverIntermediate) {
        this.serverIntermediate = serverIntermediate;
    }
}