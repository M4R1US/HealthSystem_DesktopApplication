package NetworkThreads;

import Classes.ConsoleOutput;
import SavedVariables.FinalConstants;
import CustomCache.ImageCache;
import CustomCache.ImageCacheRegister;
import NetworkObjects.ImageObject;
import javafx.scene.image.Image;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 05/04/2017.</h2>
 *
 * <p>This thread is designed to request image from server</p>
 */
public class RequestImage extends Thread {

    private ImageObject imageObject;
    private Consumer<Image> onImageReceiveFunction;
    private Socket clientSocket;

    /**
     * Constructor taking necessary parameters
     * @param obj imageObject with user data
     * @param onImageReceiveFunction functional interface with instructions what to do with received image
     * @see ImageObject
     */
    public RequestImage(ImageObject obj, Consumer<Image> onImageReceiveFunction)
    {
        this.imageObject = obj;
        this.onImageReceiveFunction = onImageReceiveFunction;
        clientSocket = new Socket();

    }

    /**
     * Run method for this thread
     */
    @Override
    public void run()
    {
        Image profilePic = null;
        try {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                ConsoleOutput.print(getClass().getName(),e);
            }

            clientSocket.connect(new InetSocketAddress(FinalConstants.HOST_DOMAIN_NAME, FinalConstants.TCP_IMAGE_REQUEST_PORT),1000);
            clientSocket.setSoTimeout(FinalConstants.IMAGE_TRANSFER_TIMEOUT);
            ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            outToServer.writeObject(imageObject);
            ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
            imageObject = (ImageObject) inFromServer.readObject();

            if (imageObject.getPersonImageBytes() != null)
            {
                InputStream inputStream = new ByteArrayInputStream(imageObject.getPersonImageBytes());
                profilePic = new Image(inputStream);
                inputStream.close();
            }

            inFromServer.close();
            outToServer.close();
            clientSocket.close();

        } catch (IOException e) {

            ConsoleOutput.print(getClass().getName(),e);

        } catch (ClassNotFoundException e) {

            ConsoleOutput.print(getClass().getName(),e);


        } finally {

            onImageReceiveFunction.accept(profilePic);
            ImageCacheRegister.insert(new ImageCache(imageObject.getID(),imageObject.getType(),profilePic));
        }

    }

}
