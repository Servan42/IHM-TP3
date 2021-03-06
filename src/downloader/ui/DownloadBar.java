package downloader.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import downloader.fc.Downloader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Graphic representation of the download bar.
 * 
 * @author CHARLOT CHANET
 */
public class DownloadBar extends BorderPane {
	private ProgressBar barre;
	private Downloader downloader;
	private Label label;
	private Button BoutonRemove;
	private Button BoutonPause;
	private HBox boutons;
	private boolean estEnPause;

	/**
	 * Creates the download bar and use the URL given as argument
	 * to call the FC methods to begin the download. 
	 * 
	 * @param url URL of the page that is goind to be downloaded.
	 */
	public DownloadBar(String url) {

		try {
			new URL(url);
		} catch (MalformedURLException e) {
			new Alert(Alert.AlertType.ERROR, "Incorrect URL.", ButtonType.CLOSE).showAndWait();
			throw new RuntimeException(e);
		}
		
		estEnPause = false;
		
		this.barre = new ProgressBar();
		barre.setPrefWidth(Double.MAX_VALUE);
		this.setCenter(barre);

		label = new Label(url);
		setTop(label);
		
		BoutonRemove = new Button("X");
		BoutonRemove.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				VBox parent = (VBox) DownloadBar.this.getParent();
				parent.getChildren().remove(DownloadBar.this);
				downloader.setStopDL();
				downloader.play();
			}	
		});
		
		BoutonPause = new Button("Pause");
		BoutonPause.setMinWidth(60);
		BoutonPause.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if(estEnPause){
					BoutonPause.setText("Pause");
					downloader.play();
					estEnPause = false;
				} else {
					BoutonPause.setText("Start");
					downloader.pause();
					estEnPause = true;
				}
				
			}	
		});
		
		boutons = new HBox(BoutonPause,BoutonRemove);
		setRight(boutons);

		new Thread(new Runnable() {
			public void run() {
				download(url);
			}
		}).start();
	}
	
	/**
	 * Animates the download bar and calls the FC to start the download.
	 * 
	 * @param url URL of the page that is goind to be downloaded.
	 */
	private void download(String url) {

		try {
			downloader = new Downloader(url);
		} catch (Exception e) {
			System.err.format("skipping %s %s\n", url, e);
		}

		downloader.progressProperty().addListener((obs, o, n) -> {
			Platform.runLater(() -> {
				barre.setProgress((double) n);
				if((double) n == 1.0){
//					boutons.getChildren().remove(BoutonPause);
					BoutonPause.setDisable(true);
				}
			});
		});

		try {
			downloader.run();
		} catch (Exception e) {
			System.err.println("failed!");
		}

	}

}
