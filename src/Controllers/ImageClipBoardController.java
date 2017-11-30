package Controllers;

import Classes.ConsoleOutput;
import Classes.CustomResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis</h2>
 * <p>Controller of image clipboard. This class is responsible for clipping images, uploading to server, saving them locally</p>
 */
public class ImageClipBoardController implements Initializable, EventHandler<KeyEvent> {

    @FXML
    public Image image;
    public ImageView OriginalImage, CapturedImage,BackImage,NextImage;
    public Rectangle Rectangle;
    public HBox BrowseHBox,ZoomInnHBox,ZoomOutHBox,BrowseFolderHBox;
    public Pane RectAnglePane, ImagePane, ImageClipBoardPane,SwitchPane;
    public Label AngleLabel,XValueLabel, YValueLabel;
    public GridPane RotateLeftPane,RotateRightPane,UploadPane,SavePane,ExitPane;


    private Scene mainScene;
    private double pressed_X, pressed_Y, start_X, start_Y, previous_X, previous_Y, currentXCorner, currentYCorner, currentAngle = 0;
    private int maxImagePaneWidth = 1000;
    private int maxImagePaneHeight = 568;
    private final double resizeRatioPercent = 0.01; // 1%
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private File selectedImage;
    private final int yOffset = 65;
    private final int xOffset = 5;
    private ArrayList<String> imagePaths;
    private Stage currentStage;
    private Consumer<Image> uploadFunction;
    private WritableImage cut;
    private CustomResourceBundle resourceBundle;
    private int currentImageIndex = 0;
    private Timeline timeline;

    /**
     * Initializes local variables from fxml file, also attaches events for corresponding nodes
     * @param location fxml path
     * @param resources CustomResourceBundle object with additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        resourceBundle = (CustomResourceBundle)resources;
        currentStage = (Stage) resourceBundle.getObject("Stage");
        currentStage.addEventHandler(KeyEvent.KEY_PRESSED,this);
        mainScene = currentStage.getScene();
        imagePaths = new ArrayList<>();

        SwitchPane.setLayoutX(910);
        SwitchPane.setLayoutY(10);

        Rectangle.setOnMouseDragged(event -> {
            dragRectangle(event.getX(), event.getY());
            captureImage();
        });

        Rectangle.setOnMousePressed(event -> {
            pressed_X = event.getX();
            pressed_Y = event.getY();
        });

        Rectangle.setOnMouseReleased(event -> {
            start_X = Rectangle.getX();
            start_Y = Rectangle.getY();
        });

        ZoomInnHBox.setOnMouseClicked(event -> zoomInn());

        ZoomOutHBox.setOnMouseClicked(event -> zoomOut());

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Supported image formats [jpg,jpeg,bmp,png,gif] ", "*.bmp", "*.gif", "*.jpeg", "*.png", "*.jpg"));
        fileChooser.setTitle("Please select image");

        BrowseHBox.setOnMouseClicked(event -> {
            selectedImage = fileChooser.showOpenDialog(ZoomOutHBox.getScene().getWindow());
            if (selectedImage != null)
            {
                try {
                    InputStream pictureInputStream = new FileInputStream(selectedImage);
                    setImage(pictureInputStream);
                } catch (IOException e) {

                    e.printStackTrace();
                }

            }
        });


        ImageClipBoardPane.setOnScroll((ScrollEvent scrollEvent) ->{
            if(scrollEvent.getDeltaY() > 0)
                zoomOut();
            if(scrollEvent.getDeltaY() < 0)
                zoomInn();
        });


        BrowseFolderHBox.setOnMouseClicked( event -> {
            directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Please select directory");
            File directory = directoryChooser.showDialog(ZoomOutHBox.getScene().getWindow());
            if(directory != null && directory.isDirectory())
            {
                if(imagePaths != null)
                    imagePaths.clear();

                for (File f : directory.listFiles())
                {
                    if(f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".bmp") || f.getName().endsWith(".png") || f.getName().endsWith(".gif") )
                        imagePaths.add(f.getAbsolutePath());
                }

                imagePaths.forEach(path -> ConsoleOutput.print(getClass().getName() + "Scanning images : " +path));

                if(imagePaths.size() != 0)
                {
                    try {
                        InputStream pictureInputStream = new FileInputStream(imagePaths.get(0));
                        setImage(pictureInputStream);
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }

        });


        NextImage.setOnMouseClicked(event -> selectImage(currentImageIndex+1));
        BackImage.setOnMouseClicked(event -> selectImage(currentImageIndex-1));

        Rectangle.relocate(0, 0);

        RotateLeftPane.setOnMouseClicked(event -> rotate(90));
        RotateRightPane.setOnMouseClicked(event -> rotate(-90));

        SavePane.setOnMouseClicked(event -> saveImageFile(SavePane.getScene()));

        UploadPane.setOnMouseClicked(event -> uploadImage());


        ExitPane.setOnMouseClicked(event -> currentStage.close());

        image = new Image("/Icons/AdditionalImages/intro.png");
        OriginalImage.setImage(image);
        calculateImageSizeProperty();


    }

    /**
     * Switch picture opacity effect
     * @param n node to apply effect
     */
    private void switchPictureEffect(Node n)
    {
        n.setOpacity(0);

        EventHandler opacityEvent = event -> {
            if (n.getOpacity() < 1)
            {
                n.setOpacity(n.getOpacity() + 0.05);
                return;
            }
            timeline.stop();
            captureImage();
        };


        timeline = new Timeline(new KeyFrame(Duration.millis(50), opacityEvent), new KeyFrame(Duration.millis(50)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Selecting image from the list and showing result on the screen
     * @param index image index of selected image
     */
    public void selectImage(int index)
    {
        if(imagePaths == null || imagePaths.size() == 0)
            return;

        if(index > imagePaths.size() -1)
            index = 0;

        if(index < 0)
            index = imagePaths.size() -1;

        try {

            InputStream pictureInputStream = new FileInputStream(imagePaths.get(index));
            setImage(pictureInputStream);
            currentImageIndex = index;

        } catch (IOException e) {

            e.printStackTrace();
        }

    }


    /**
     * Method to save image locally
     * @param parentScene scene of window
     */
    private void saveImageFile(Scene parentScene) {
        if (CapturedImage.getImage() == null)
            return;

        WritableImage writableImage = CapturedImage.snapshot(null, null);
        PixelReader pixelReader = writableImage.getPixelReader();
        cut = new WritableImage(pixelReader, 0, 0, 160, 160);

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(cut, null);
        File outputFile;

        try {

            File dest = fileChooser.showSaveDialog(parentScene.getWindow());
            outputFile = new File("Saved.png");
            ImageIO.write(bufferedImage, "png", outputFile);

            if (dest != null)
                Files.copy(outputFile.toPath(), dest.toPath());


        } catch (IOException e) {   ConsoleOutput.print(getClass().getName(),"Couldn't save image");    }

    }


    /**
     * Upload function
     * @param uploadFunction functional interface with instructions how image should be uploaded to the server
     */
    public void setOnUploadFunction(Consumer<Image> uploadFunction)
    {
        this.uploadFunction =uploadFunction;
    }

    /**
     * Method to set image on the screen
     * @param pictureInputStream inputStream of image
     */
    private void setImage(InputStream pictureInputStream)
    {
        OriginalImage.setOpacity(0);
        image = new Image(pictureInputStream);
        OriginalImage.setImage(image);
        switchPictureEffect(OriginalImage);
        calculateImageSizeProperty();
        captureImage();
    }

    /**
     * Calculation image size property to fit it properly inside pane
     */
    private void calculateImageSizeProperty()
    {
        if(image == null)
            return;

        double originalImageWidth = image.getWidth();
        double originalImageHeight = image.getHeight();

        while (originalImageWidth > maxImagePaneWidth || originalImageHeight > maxImagePaneHeight)
        {
            originalImageHeight-= originalImageHeight*resizeRatioPercent;
            originalImageWidth-= originalImageWidth*resizeRatioPercent;
        }

        OriginalImage.setFitHeight(originalImageHeight);
        OriginalImage.setFitWidth(originalImageWidth);

    }

    /**
     * Image rotation
     * @param angle angle to rotate image
     */
    private void rotate(double angle)
    {
        if(CapturedImage.getImage() == null)
            return;
        currentAngle = CapturedImage.getRotate() + angle;
        CapturedImage.setRotate(currentAngle);
        AngleLabel.setText("Mirror angle : " + currentAngle + "\u00b0");
    }


    /**
     * Rectangle dragging sensor
     * @param xDraggedPosition current x position of dragged mouse
     * @param yDraggedPosition current y position of dragged mouse
     */
    private void dragRectangle(double xDraggedPosition, double yDraggedPosition)
    {

        //setting previous x and y equal to last x and y coordinates
        previous_X = currentXCorner;
        previous_Y = currentYCorner;

        //calculating new x and y coordinates
        currentXCorner =(xDraggedPosition-pressed_X) + start_X;
        currentYCorner = (yDraggedPosition-pressed_Y) + start_Y;

        //if mouse is out of pane rectangle will lost focus

        currentXCorner = (currentXCorner < 0 || currentXCorner > (ImagePane.getWidth() - Rectangle.getWidth())) ? previous_X : currentXCorner;
        currentYCorner = (currentYCorner < 0 || currentYCorner > (ImagePane.getHeight()- Rectangle.getHeight())) ? previous_Y : currentYCorner;

        //otherwise coordinates are valid and rectangle will move
        Rectangle.setX(currentXCorner);
        Rectangle.setY(currentYCorner);
        XValueLabel.setText("X" + "\u2081" + " = " + currentXCorner +"");
        YValueLabel.setText("Y" + "\u2081" + " = " + currentYCorner +"");
    }

    /**
     * Method to zoom inn image if its possible
     */
    private void zoomInn()
    {
        if(!enabledZoomIn())
            return;

        double newImageHeight = OriginalImage.getFitHeight()+ OriginalImage.getFitHeight()*resizeRatioPercent;
        double newImageWidth = OriginalImage.getFitWidth()+ OriginalImage.getFitWidth()*resizeRatioPercent;
        OriginalImage.setFitHeight(newImageHeight);
        OriginalImage.setFitWidth(newImageWidth);
        captureImage();
    }

    /**
     * Method to check if new calculated image size fits properly inside pane
     * @return true if fits, false otherwise
     */
    private boolean enabledZoomIn()
    {
        if(OriginalImage ==null)
            return false;

        return (OriginalImage.getFitWidth() + OriginalImage.getFitWidth()*resizeRatioPercent <= maxImagePaneWidth) && (OriginalImage.getFitHeight() + OriginalImage.getFitHeight()*resizeRatioPercent <= maxImagePaneHeight);
    }

    /**
     * Method to zoom out image if its possible
     */
    private void zoomOut()
    {
        double newImageHeight = OriginalImage.getFitHeight()- OriginalImage.getFitHeight()*resizeRatioPercent;
        double newImageWidth = OriginalImage.getFitWidth()- OriginalImage.getFitWidth()*resizeRatioPercent;

        if(newImageHeight <= 160 || newImageWidth <= 160)
            return;

        OriginalImage.setFitHeight(newImageHeight);
        OriginalImage.setFitWidth(newImageWidth);
        captureImage();
    }

    /**
     * Snapshot of image under rectangle
     */
    private void captureImage()
    {
        WritableImage writableImage;
        PixelReader pixelReader;

        mainScene = currentStage.getScene();

        writableImage = mainScene.snapshot(null);
        pixelReader = writableImage.getPixelReader();
        cut = new WritableImage(pixelReader,(int) Rectangle.getX()+xOffset,(int) Rectangle.getY()+yOffset,160,160);
        CapturedImage.setImage(cut);
    }

    /**
     * Method for uploading image to server
     */
    private void uploadImage()
    {
        if(uploadFunction != null && CapturedImage.getImage() != null)
        {
            WritableImage writableImage;

            writableImage = CapturedImage.snapshot(null, null);
            PixelReader pixelReader = writableImage.getPixelReader();
            cut = new WritableImage(pixelReader, 0, 0, 160, 160);
            uploadFunction.accept(cut);

            exit();
        }

    }

    /**
     * Safe exit method
     */
    private void exit()
    {
        currentStage.setFullScreen(false);
        currentStage.close();
    }

    /**
     * Reaction of various key inputs
     * @param event KeyEvent to react
     */
    @Override
    public void handle(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ESCAPE)
            currentStage.close();


        if(event.isShiftDown() && event.getCode() != null)
        {
            switch (event.getCode())
            {
                case I:
                    zoomInn();
                    break;

                case O:
                    zoomOut();
                    break;

                case U:
                    uploadImage();
                    break;

                case S:
                    saveImageFile(SavePane.getScene());
                    break;

                case LEFT:
                    rotate(90);
                    break;

                case RIGHT:
                    rotate(-90);
                    break;

                default:
                    break;
            }
        }

        if(!event.isShiftDown() && event.getCode() != null)
        {
            switch (event.getCode())
            {
                case LEFT:
                    selectImage(currentImageIndex - 1);
                    break;

                case RIGHT:
                    selectImage(currentImageIndex + 1);
                    break;

                default:
                    break;

            }
        }

    }
}
