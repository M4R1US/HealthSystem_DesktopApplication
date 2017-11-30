package NetworkThreads;

import Classes.ConsoleOutput;
import SavedVariables.FinalConstants;
import NetworkObjects.UserReference;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * <h2>Created by Marius Baltramaitis on 14/04/2017.</h2>
 *
 * <p>This class is scheduled service for server connection</p>
 */
public class TCPUserDeviceConnection extends ScheduledService<Void> {

    private Stage openedStage;
    private UserReference currentUserReference;
    private Socket clientSocket;
    volatile boolean activeConnection = false;
    volatile int currentAttempts = 0;
    private final int MAX_DISCONNECT_ATTEMPTS = 5;
    public SimpleObjectProperty<Boolean> connectionSensor;
    public SimpleObjectProperty<Integer> reconnectTimeSensor;
    public SimpleObjectProperty<Boolean> statusProperty;
    private int rebootSeconds = 8;

    /**
     * Constructor taking necessary parameters
     * @param dashboardStage window of dashboard
     * @param userReference user reference object with data inside
     * @throws IOException if service couldn't be launched
     * @see UserReference
     */
    public TCPUserDeviceConnection(Stage dashboardStage, UserReference userReference) throws IOException
    {
        openedStage = dashboardStage;
        currentUserReference = userReference;
        connectionSensor = new SimpleObjectProperty<>(false);
        reconnectTimeSensor = new SimpleObjectProperty<>(0);
        statusProperty = new SimpleObjectProperty<>(true);
    }

    /**
     * Sleeping thread
     */
    private void nap()
    {
        try
        {
            Thread.sleep(FinalConstants.SCHEDULED_TASK_SLEEP_MILLISECONDS);
            --rebootSeconds;
            reconnect(rebootSeconds);
        } catch (InterruptedException e)
        {
            ConsoleOutput.print(getClass().getName(),"Nap thread interrupted!");
        }
    }

    /**
     * Method to reconnect
     * @param seconds amount of seconds to delay reconnection
     * @return {@link #connect()}
     */
    private Void reconnect(int seconds)
    {
        if(seconds > 0){
            reconnectTimeSensor.set(seconds);
            nap();
            return null;
        }
        rebootSeconds = 8;
        return connect();

    }


    /**
     * TCP Connection to main server.
     * @see #waitForSignal() 
     * @return {@link #connect()} if connection wasn't established. Otherwise returns {@link #waitForSignal()}
     */
    private Void connect()
    {


        try {
            clientSocket = new Socket(FinalConstants.HOST_DOMAIN_NAME, FinalConstants.TCP_DEVICE_CONNECTION_PORT);
            ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            outToServer.writeObject(currentUserReference);
            activeConnection = true;
            connectionSensor.set(true);
        } catch (IOException e) {

            ConsoleOutput.print(getClass().getName(), e);
            connectionSensor.set(false);
            return reconnect(rebootSeconds);
            //nap();

        }

        return (clientSocket == null) ? connect() : waitForSignal();
    }

    /**
     * Method to abort service
     * @return true if service is canceled, false otherwise
     */
    @Override
    public boolean cancel()
    {
        disconnect();
        activeConnection = false;
        connectionSensor.set(false);
        return super.cancel();
    }

    /**
     *  Scheduled method to wait for signal from server
     * @return {@link #disconnect()} ()} if connection was forced from server to be aborted . Otherwise returns {@link #waitForSignal()}
     */
    private Void waitForSignal()
    {
        try {

            if(clientSocket.isBound() && clientSocket != null && activeConnection)
            {
                ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
                UserReference inputObject = (UserReference)inFromServer.readObject();

                if (inputObject.getAction() == (byte)0)
                {
                    statusProperty.set(false);

                    try { inFromServer.close(); } catch (IOException ioe) { ConsoleOutput.print(getClass().getName(),"Couldn't close input stream"); }
                }
            } else {
                return disconnect();
            }

        } catch (ClassNotFoundException e)
        {
            ConsoleOutput.print(getClass().getName(),"ClassNotFoundException. Error on casting class");
            if(clientSocket.isConnected())
                try { clientSocket.close(); } catch (IOException IOexception) {ConsoleOutput.print(getClass().getName(),"Couldn't close socket");}

        }

        catch (IOException e) {
            ConsoleOutput.print(getClass().getName(), "Server is not answering");
            connectionSensor.set(false);
            //nap();
            return reconnect(rebootSeconds);
        }

        return waitForSignal();
    }

    /**
     * Method safe disconnection [closing sockets etc ]
     * @return null always
     */
    public Void disconnect()
    {
        if(clientSocket != null && clientSocket.isBound() && clientSocket.isConnected() && currentAttempts <= MAX_DISCONNECT_ATTEMPTS)
        {
            try {
                clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(FinalConstants.HOST_DOMAIN_NAME,FinalConstants.TCP_DEVICE_CONNECTION_PORT),2000);
                clientSocket.setSoTimeout(FinalConstants.IMAGE_TRANSFER_TIMEOUT);
                ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
                currentUserReference.setAction((byte)0);
                outToServer.writeObject(currentUserReference);
                outToServer.close();
                clientSocket.close();

            } catch (IOException e) {
                ConsoleOutput.print(getClass().getName(), e);
                currentAttempts++;
                disconnect();
                connectionSensor.set(false);
            }
        }
        return null;
    }


    /**
     * Default method to create task
     * @return {@link #disconnect()} ()} if connection is not active, null otherwise
     */
    @Override
    protected Task<Void> createTask()
    {

        if (!activeConnection) {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    return connect();
                }
            };
        }
        return null;

    }
}
