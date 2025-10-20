import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends Application {
    
    // System constants
    private static final String CORRECT_PASSWORD = "password123";
    private static final int MAX_SHORTCUTS_PER_COLUMN = 5;
    
    // Main stage and scenes
    private Stage primaryStage;
    private Scene loginScene;
    private Scene homeScene;
    
    // Data storage 	
    private Map<String, String> textFiles = new ConcurrentHashMap<>();
    private Map<String, Image> imageFiles = new ConcurrentHashMap<>();
    private List<String> textFileNames = new ArrayList<>();
    private List<String> imageFileNames = new ArrayList<>();
    
    // Home page components
    private GridPane shortcutsGrid;
    private MenuBar taskBar;
    private int currentColumn = 0;
    private int currentRow = 0;
    
    // Media players for cleanup
    private List<Timeline> activeTimelines = new ArrayList<>();
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        // Initialize default files
        initializeDefaultFiles();
        
        // Create scenes
        createLoginScene();
        createHomeScene();
        
        // Setup primary stage
        primaryStage.setTitle("RUindows");
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            cleanupTimelines();
            Platform.exit();
        });
        
        primaryStage.show();
    }
    
    private void initializeDefaultFiles() {
        // Initialize with some default content
        textFiles.put("readme.txt", "Welcome to RUindows!\nThis is a sample text file.");
        textFileNames.add("readme.txt");
    }
    
    private void createLoginScene() {
        StackPane root = new StackPane();
        
        // Background
        try {
            ImageView background = new ImageView();
            // Using a placeholder for nature.png
            background.setImage(new Image("nature.jpg"));
            background.setFitWidth(800);
            background.setFitHeight(600);
            background.setPreserveRatio(false);
            root.getChildren().add(background);
        } catch (Exception e) {
            // Fallback background
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEEB, #98FB98);");
        }
        
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxWidth(300);
        
        // Profile picture
        StackPane profilePane = new StackPane();
        
        ImageView profile = new ImageView();
        profile.setImage(new Image("default_profile_pic.png"));
        profile.setFitWidth(80);
        profile.setFitHeight(80);
        profile.setTranslateY(20);
        
        profilePane.getChildren().add(profile);
        
        // Welcome label
        Label welcomeLabel = new Label("Welcome RU24-2!");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(200);
        passwordField.setStyle("-fx-background-radius: 20; -fx-padding: 10;");
        
        // Login button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20;");
        
        // Error label
        Label errorLabel = new Label("Wrong password!");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
        errorLabel.setVisible(false);
        
        // Login form
        HBox loginForm = new HBox(10);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.getChildren().addAll(passwordField, loginButton);
        
        loginBox.getChildren().addAll(profilePane, welcomeLabel, loginForm, errorLabel);
        
        // Login functionality
        Runnable loginAction = () -> {
            String password = passwordField.getText();
            if (CORRECT_PASSWORD.equals(password)) {
                primaryStage.setScene(homeScene);
                passwordField.clear();
                errorLabel.setVisible(false);
            } else {
                errorLabel.setVisible(true);
                passwordField.clear();
                
                // Shake animation
                TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginForm);
                shake.setFromX(0);
                shake.setToX(10);
                shake.setCycleCount(6);
                shake.setAutoReverse(true);
                shake.play();
            }
        };
        
        loginButton.setOnAction(e -> loginAction.run());
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                loginAction.run();
            }
        });
        
        root.getChildren().add(loginBox);
        loginScene = new Scene(root, 800, 600);
    }
    
    private void createHomeScene() {
        BorderPane root = new BorderPane();
        
        // Background
        try {
            ImageView background = new ImageView();
            background.setImage(new Image("homepage.jpg"));
            background.setFitWidth(800);
            background.setFitHeight(600);
            background.setPreserveRatio(false);
            StackPane backgroundPane = new StackPane(background);
            root.getChildren().add(backgroundPane);
        } catch (Exception e) {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEEB, #98FB98);");
        }
        
        // Shortcuts area
        ScrollPane scrollPane = new ScrollPane();
        shortcutsGrid = new GridPane();
        shortcutsGrid.setHgap(20);
        shortcutsGrid.setVgap(20);
        shortcutsGrid.setPadding(new Insets(20));
        
        scrollPane.setContent(shortcutsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Create default shortcuts
        createDefaultShortcuts();
        
        root.setCenter(scrollPane);
        
        // Task bar
        createTaskBar();
        root.setBottom(taskBar);
        
        homeScene = new Scene(root, 800, 600);
    }
    
    private void createDefaultShortcuts() {
        // Clear existing shortcuts
        shortcutsGrid.getChildren().clear();
        currentColumn = 0;
        currentRow = 0;
        
        // Trash shortcut
        ImageView trash = new ImageView();
        trash.setImage(new Image("trash-icon.png"));
        VBox trashShortcut = createShortcut(trash, "Trash", () -> {
            showAlert("Trash", "Trash functionality not implemented yet.");
        });
        
        // Notepad shortcut
        ImageView notepad = new ImageView();
        notepad.setImage(new Image("notepad-icon.png"));
        VBox notepadShortcut = createShortcut(notepad, "Notepad", () -> {
            openNotepadApplication();
        });
        
        // ChRUme shortcut
        ImageView chrome = new ImageView();
        chrome.setImage(new Image("chrome.png"));
        VBox chromeShortcut = createShortcut(chrome, "ChRUme", () -> {
            openChRUmeApplication();
        });
        
        addShortcutToGrid(trashShortcut);
        addShortcutToGrid(notepadShortcut);
        addShortcutToGrid(chromeShortcut);
        
        // Add existing text files
        for (String fileName : textFileNames) {
        	ImageView notepad1 = new ImageView();
            notepad1.setImage(new Image("notepad-icon.png"));
            VBox textFileShortcut = createShortcut(notepad1, fileName, () -> {
                openTextFileApplication(fileName);
            });
            addShortcutToGrid(textFileShortcut);
        }
        
        // Add existing image files
        for (String fileName : imageFileNames) {
            VBox imageFileShortcut = createImageShortcut(imageFiles.get(fileName), fileName, () -> {
                openPhotoEditorApplication(fileName);
            });
            addShortcutToGrid(imageFileShortcut);
        }
    }
    
    private VBox createShortcut(ImageView icon, String name, Runnable action) {
        VBox shortcut = new VBox(5);
        shortcut.setAlignment(Pos.CENTER);
        shortcut.setPrefSize(80, 100);
        
        ImageView trash = new ImageView();
        trash.setImage(new Image("trash-icon.png"));
        ImageView notepad = new ImageView();
        notepad.setImage(new Image("notepad-icon.png"));
        ImageView chrome = new ImageView();
        chrome.setImage(new Image("chrome.png"));
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        icon.setPreserveRatio(true);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 2;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(80);
        
        shortcut.getChildren().addAll(icon, nameLabel);
        shortcut.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                action.run();
            }
        });
        
        return shortcut;
    }
    
    private VBox createImageShortcut(Image image, String name, Runnable action) {
        VBox shortcut = new VBox(5);
        shortcut.setAlignment(Pos.CENTER);
        shortcut.setPrefSize(80, 100);
        
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 2;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(80);
        
        shortcut.getChildren().addAll(imageView, nameLabel);
        shortcut.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                action.run();
            }
        });
        
        return shortcut;
    }
    
    private void addShortcutToGrid(VBox shortcut) {
        shortcutsGrid.add(shortcut, currentColumn, currentRow);
        currentRow++;
        if (currentRow >= MAX_SHORTCUTS_PER_COLUMN) {
            currentRow = 0;
            currentColumn++;
        }
    }
    
    private void createTaskBar() {
        taskBar = new MenuBar();
        taskBar.setStyle("-fx-background-color: #2C3E50;");
        
        // Window menu
        Menu windowMenu = new Menu();
        ImageView windowIcon = new ImageView();
        windowIcon.setImage(new Image("window-icon.png"));
        windowIcon.setFitWidth(15);
        windowIcon.setFitHeight(15);
        windowIcon.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        windowMenu.setGraphic(windowIcon);
        
        ImageView logoutIcon = new ImageView();
        logoutIcon.setImage(new Image("logout3-icon.png"));
        logoutIcon.setFitWidth(15);
        logoutIcon.setFitHeight(15);
        MenuItem logoutItem = new MenuItem("Log out");
        logoutItem.setGraphic(logoutIcon);
        logoutItem.setOnAction(e -> {
            cleanupTimelines();
            primaryStage.setScene(loginScene);
        });
        
        ImageView shutdownIcon = new ImageView();
        shutdownIcon.setImage(new Image("shutdown3-icon.png"));
        shutdownIcon.setFitWidth(15);
        shutdownIcon.setFitHeight(15);
        MenuItem shutdownItem = new MenuItem("Shutdown");
        shutdownItem.setGraphic(shutdownIcon);
        shutdownItem.setOnAction(e -> {
            cleanupTimelines();
            Platform.exit();
        });
        
        windowMenu.getItems().addAll(logoutItem, shutdownItem);
        
        // Notepad menu
        Menu notepadMenu = new Menu();
        ImageView notepadIcon = new ImageView();
        notepadIcon.setImage(new Image("notepad-icon.png"));
        notepadIcon.setFitWidth(20);
        notepadIcon.setFitHeight(20);
        notepadIcon.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        notepadMenu.setGraphic(notepadIcon);
        notepadMenu.setOnShowing(e -> openNotepadApplication());
        
        taskBar.getMenus().addAll(windowMenu, notepadMenu);
    }
    
    private void openNotepadApplication() {
        Stage notepadStage = new Stage();
        notepadStage.setTitle("Notepad");
        
        BorderPane root = new BorderPane();
        
        // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem saveItem = new MenuItem("Save");
        fileMenu.getItems().add(saveItem);
        menuBar.getMenus().add(fileMenu);
        
        // Text area
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        
        saveItem.setOnAction(e -> saveTextFile(textArea.getText(), notepadStage));
        
        root.setTop(menuBar);
        root.setCenter(textArea);
        
        Scene scene = new Scene(root, 600, 400);
        notepadStage.setScene(scene);
        notepadStage.show();
    }
    
    private void openTextFileApplication(String fileName) {
        Stage textFileStage = new Stage();
        textFileStage.setTitle(fileName);
        
        BorderPane root = new BorderPane();
        
        // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem saveItem = new MenuItem("Save");
        fileMenu.getItems().add(saveItem);
        menuBar.getMenus().add(fileMenu);
        
        // Text area with existing content
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setText(textFiles.getOrDefault(fileName, ""));
        
        saveItem.setOnAction(e -> {
            textFiles.put(fileName, textArea.getText());
            showAlert("Save", "File saved successfully!");
        });
        
        root.setTop(menuBar);
        root.setCenter(textArea);
        
        Scene scene = new Scene(root, 600, 400);
        textFileStage.setScene(scene);
        textFileStage.show();
    }
    
    private void openChRUmeApplication() {
        Stage chromeStage = new Stage();
        chromeStage.setTitle("ChRUme");
        
        BorderPane root = new BorderPane();
        
        // Search bar
        HBox searchBar = new HBox(10);
        searchBar.setPadding(new Insets(10));
        searchBar.setAlignment(Pos.CENTER);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Enter URL...");
        searchField.setPrefWidth(400);
        
        Button searchButton = new Button("Search");
        
        searchBar.getChildren().addAll(searchField, searchButton);
        
        // Content area
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: white;");
        
        // Default empty content
        showEmptyContent(contentArea);
        
        Runnable searchAction = () -> {
            String url = searchField.getText().trim();
            if (url.isEmpty()) {
                showEmptyContent(contentArea);
            } else if ("RUtube.net".equals(url)) {
                showRUtubeContent(contentArea, chromeStage);
            } else if ("RUtify.net".equals(url)) {
                showRUtifyContent(contentArea, chromeStage);
            } else if ("stockimages.net".equals(url)) {
                showStockImagesContent(contentArea);
            } else {
                showDomainNotFoundContent(contentArea, url);
            }
        };
        
        searchButton.setOnAction(e -> searchAction.run());
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchAction.run();
            }
        });
        
        root.setTop(searchBar);
        root.setCenter(contentArea);
        
        Scene scene = new Scene(root, 800, 600);
        chromeStage.setScene(scene);
        chromeStage.setOnCloseRequest(e -> {
            // Stop any timelines in this window
            contentArea.getChildren().clear();
        });
        chromeStage.show();
    }
    
    private void showEmptyContent(StackPane contentArea) {
        contentArea.getChildren().clear();
        Label emptyLabel = new Label("Welcome to ChRUme\nEnter a URL to browse");
        emptyLabel.setStyle("-fx-font-size: 24px; -fx-text-alignment: center;");
        contentArea.getChildren().add(emptyLabel);
    }
    
    private void showDomainNotFoundContent(StackPane contentArea, String domain) {
        contentArea.getChildren().clear();
        VBox errorBox = new VBox(10);
        errorBox.setAlignment(Pos.CENTER);
        
        Label errorTitle = new Label("This site can't be reached");
        errorTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label errorMessage = new Label(domain + " does not exist");
        errorMessage.setStyle("-fx-font-size: 16px;");
        
        errorBox.getChildren().addAll(errorTitle, errorMessage);
        contentArea.getChildren().add(errorBox);
    }
    
    private void showRUtubeContent(StackPane contentArea, Stage parentStage) {
        contentArea.getChildren().clear();
        
        VBox videoBox = new VBox(10);
        videoBox.setAlignment(Pos.CENTER);
        
        // Logo
        HBox logoBox = new HBox();
        logoBox.setAlignment(Pos.TOP_LEFT);
        logoBox.setPadding(new Insets(10));
        ImageView ytIcon = new ImageView();
        ytIcon.setImage(new Image("youtube-logo.png"));
        ytIcon.setFitWidth(25);
        ytIcon.setFitHeight(25);
        Label logo = new Label("RUtube");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: red;");
        logoBox.getChildren().addAll(ytIcon, logo);
        
        // Video placeholder
//        File videoFile = new File("DiamondJack.mp4");
        Media videoMedia = new Media(getClass().getResource("DiamondJack.mp4").toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(videoMedia);
        MediaView viewMedia = new MediaView(mediaPlayer);
        
        viewMedia.setFitHeight(350);
        viewMedia.setFitWidth(350);
        
        StackPane videoPane = new StackPane(viewMedia);
        
        // Controls
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        
        controls.getChildren().addAll(playButton, pauseButton);
        
        videoBox.getChildren().addAll(logoBox, videoPane, controls);
        contentArea.getChildren().add(videoBox);
        
        // Simple play/pause simulation
        playButton.setOnAction(e -> {
        	mediaPlayer.play();
        });
        
        pauseButton.setOnAction(e -> {
        	mediaPlayer.pause();
        });
    }
    
    private void showRUtifyContent(StackPane contentArea, Stage parentStage) {
        contentArea.getChildren().clear();
        
        VBox audioBox = new VBox(20);
        audioBox.setAlignment(Pos.CENTER);
        
        // Logo
        HBox logoBox = new HBox();
        logoBox.setAlignment(Pos.TOP_LEFT);
        logoBox.setPadding(new Insets(10));
        ImageView spotifyIcon = new ImageView();
        spotifyIcon.setImage(new Image("spotify-logo.png"));
        spotifyIcon.setFitWidth(25);
        spotifyIcon.setFitHeight(25);
        Label logo = new Label("RUtify");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: green;");
        logoBox.getChildren().addAll(spotifyIcon, logo);
        
        // Audio controls
        Slider audioSlider = new Slider(0, 100, 0);
        audioSlider.setPrefWidth(300);
        
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        
        controls.getChildren().addAll(playButton, pauseButton);
        
//        File audioFile = new File("PromQueen.mp3");
        Media audioMedia = new Media(getClass().getResource("PromQueen.mp3").toExternalForm());
        MediaPlayer viewAudio = new MediaPlayer(audioMedia);
        
        viewAudio.setOnReady(() -> {
        	Duration duration = viewAudio.getMedia().getDuration();
        	audioSlider.setMaxHeight(duration.toSeconds());
        });
        
        viewAudio.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
        	audioSlider.setValue(newVal.toSeconds());
        });;

        audioBox.getChildren().addAll(logoBox, audioSlider, controls);
        contentArea.getChildren().add(audioBox);
        
        // Simple audio simulation
        Timeline audioProgress = new Timeline(
            new KeyFrame(Duration.millis(100), e -> {
                if (audioSlider.getValue() < 100) {
                    audioSlider.setValue(audioSlider.getValue() + 0.5);
                }
            })
        );
        audioProgress.setCycleCount(Timeline.INDEFINITE);
        activeTimelines.add(audioProgress);
        
        viewAudio.setAutoPlay(true);
        
        playButton.setOnAction(e -> {
            viewAudio.play();
        });
        
        pauseButton.setOnAction(e -> {
            viewAudio.pause();
        });
    }
    
    private void showStockImagesContent(StackPane contentArea) {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        VBox imageBox = new VBox(20);
        imageBox.setPadding(new Insets(20));
        imageBox.setAlignment(Pos.CENTER);
        
        // Create sample images with download buttons
        String[] imageNames = {"orangecat.jpg", "graycat.jpg", "blackcat.jpg", "graycat2.jpg"};
        ImageView cat1 = new ImageView();
        cat1.setImage(new Image(getClass().getResourceAsStream("cat-image1.jpg")));
        cat1.setFitHeight(150);
        cat1.setFitWidth(200);
        ImageView cat2 = new ImageView();
        cat2.setImage(new Image(getClass().getResourceAsStream("cat-image2.jpg")));
        cat2.setFitHeight(150);
        cat2.setFitWidth(200);
        ImageView cat3 = new ImageView();
        cat3.setImage(new Image(getClass().getResourceAsStream("cat-image3.jpeg")));
        cat3.setFitHeight(150);
        cat3.setFitWidth(200);
        ImageView cat4 = new ImageView();
        cat4.setImage(new Image(getClass().getResourceAsStream("cat-image4.jpeg")));
        cat4.setFitHeight(150);
        cat4.setFitWidth(200);
        ImageView[] imageFiles = {cat1, cat2, cat3, cat4};
        
        for (int i = 0; i < imageNames.length; i++) {
            VBox imageContainer = new VBox(10);
            imageContainer.setAlignment(Pos.CENTER);
            
            // Create a colored rectangle as placeholder for cat images
            Rectangle imagePlaceholder = new Rectangle(200, 150);
            Color[] colors = {Color.ORANGE, Color.GRAY, Color.BLACK, Color.LIGHTGRAY};
            imagePlaceholder.setFill(colors[i]);
            
//            Label imageLabel = new Label("[CAT] " + imageFiles[i]);
//            imageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 5;");
            
            StackPane imagePane = new StackPane(imagePlaceholder, imageFiles[i]);
            
            Button downloadButton = new Button("Download");
            final String imageName = imageNames[i];
            final ImageView imageFiles1 = imageFiles[i];
            
            downloadButton.setOnAction(e -> downloadImage(imageName, imageFiles1));
            
            imageContainer.getChildren().addAll(imagePane, downloadButton);
            imageBox.getChildren().add(imageContainer);
        }
        
        scrollPane.setContent(imageBox);
        scrollPane.setFitToWidth(true);
        contentArea.getChildren().add(scrollPane);
    }
    
    private void downloadImage(String defaultName, ImageView imageFiles1) {
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Download Image");
        dialog.setHeaderText("Enter image file name:");
        dialog.setContentText("File name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(userInput -> {
            String fileName = userInput;
            
            if (!isAlphanumeric(fileName.replace(".jpg", ""))) {
                showAlert("Error", "File name must be alphanumeric!");
                return;
            }
            
            if (!fileName.endsWith(".jpg")) {
                fileName += ".jpg";
            }
            
            if (imageFileNames.contains(fileName)) {
                showAlert("Error", "File name must be unique!");
                return;
            }
            
            // Create final variable for lambda access
            final String finalFileName = fileName;
            
            // Create a simple colored image
            Image colorImage = createImage();
            imageFiles.put(finalFileName, colorImage);
            imageFileNames.add(finalFileName);
            
            // Add shortcut to home page
            Platform.runLater(() -> {
                VBox imageShortcut = createImageShortcut(colorImage, finalFileName, () -> {
                    openPhotoEditorApplication(finalFileName);
                });
                addShortcutToGrid(imageShortcut);
            });
            
            showAlert("Success", "Image downloaded: " + finalFileName);
        });
    }
    
    private Image createImage() {
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
    }
    
    private void openPhotoEditorApplication(String fileName) {
        Stage photoStage = new Stage();
        photoStage.setTitle(fileName);
        
        BorderPane root = new BorderPane();
        
        // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu zoomMenu = new Menu("Zoom");
        Menu rotateMenu = new Menu("Rotate");
        
        menuBar.getMenus().addAll(zoomMenu, rotateMenu);
        
        // Image view
        ScrollPane scrollPane = new ScrollPane();
        ImageView imageView = new ImageView(imageFiles.get(fileName));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400);
        imageView.setFitHeight(300);
        
        scrollPane.setContent(imageView);
        
        rotateMenu.setOnAction(e -> {
            imageView.setRotate(90);;
        });
        
        // Zoom slider
        Slider zoomSlider = new Slider(0.1, 3.0, 1.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue();
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
        });
        
        VBox zoomBox = new VBox(5);
        zoomBox.getChildren().addAll(new Label("Zoom:"), zoomSlider);
        zoomBox.setPadding(new Insets(10));
        
        root.setTop(menuBar);
        root.setCenter(scrollPane);
        root.setBottom(zoomBox);
        
        Scene scene = new Scene(root, 600, 500);
        photoStage.setScene(scene);
        photoStage.show();
    }
    
    private void saveTextFile(String content, Stage parentStage) {
        String defaultName = "text" + (textFileNames.size() > 0 ? textFileNames.size() : "") + ".txt";
        
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Save File");
        dialog.setHeaderText("Enter file name:");
        dialog.setContentText("File name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(userInput -> {
            String fileName = userInput;
            
            if (!isAlphanumeric(fileName.replace(".txt", ""))) {
                showAlert("Error", "File name must be alphanumeric!");
                return;
            }
            
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
            }
            
            if (textFileNames.contains(fileName)) {
                showAlert("Error", "File name must be unique!");
                return;
            }
            
            // Create final variable for lambda access
            final String finalFileName = fileName;
            
            textFiles.put(finalFileName, content);
            textFileNames.add(finalFileName);
            
            // Add shortcut to home page
            Platform.runLater(() -> {
            	ImageView notepad = new ImageView();
                notepad.setImage(new Image("notepad-icon.png"));
                VBox textShortcut = createShortcut(notepad, finalFileName, () -> {
                    openTextFileApplication(finalFileName);
                });
                addShortcutToGrid(textShortcut);
            });
            
            showAlert("Success", "File saved: " + finalFileName);
        });
    }
    
    private boolean isAlphanumeric(String str) {
        return str.matches("[a-zA-Z0-9]+");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void cleanupTimelines() {
        for (Timeline timeline : activeTimelines) {
            if (timeline != null) {
                timeline.stop();
            }
        }
        activeTimelines.clear();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}