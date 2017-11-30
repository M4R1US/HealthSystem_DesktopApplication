package NetworkThreads;

import Classes.ConsoleOutput;
import SavedVariables.FinalConstants;
import NetworkObjects.ImageObject;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

/**
 * <h2>Created by Marius Baltramaitis on 06/04/2017.</h2>
 *
 * <p>This thread is designed to submit image to server</p>
 */
public class SubmitImage implements Runnable {

    private ImageObject imageObject;
    private Image image;

    /**
     * Constructor taking necessary parameters
     * @param imageObject imageObject with user data
     * @param image image itself
     * @see ImageObject
     */
    public SubmitImage(ImageObject imageObject, Image image)
    {
        this.imageObject = imageObject;
        this.image = image;
    }

    /**
     * Run method for this thread
     */
    @Override
    public void run()
    {
        try {

            Socket clientSocket = new Socket(FinalConstants.HOST_DOMAIN_NAME, FinalConstants.TCP_IMAGE_DELIVER_PORT);
            clientSocket.setSoTimeout(FinalConstants.IMAGE_TRANSFER_TIMEOUT);
            ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            imageObject.setPersonImageBytes(convertToBytes(image));
            outToServer.writeObject(imageObject);
            outToServer.close();
            clientSocket.close();

        } catch (IOException e) {
            ConsoleOutput.print("Couldn't connect to tcp server",e);

        }
    }

    /**
     * Method to decode image to byte array
     * @param image image to convert
     * @return byte array of converted image
     */
    private byte [] convertToBytes(Image image)
    {
        if(image== null)
            return null;

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {

            ImageIO.write(bufferedImage, "gif", byteArrayOutputStream);
            byteArrayOutputStream.close();

        } catch (IOException e) {
            ConsoleOutput.print(getClass().getName(),"Error on reading image file. Abort");
        }

        return byteArrayOutputStream.toByteArray();
    }
}
