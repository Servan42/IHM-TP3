package downloader.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import downloader.fc.Downloader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DownloadBar extends BorderPane {
	private ProgressBar barre;
	private Downloader downloader;
	private Label label;
	private Button BoutonRemove;

	public DownloadBar(String url) {

		try {
			new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		this.barre = new ProgressBar();
		barre.setPrefWidth(Double.MAX_VALUE);
		this.setCenter(barre);

		label = new Label(url);
		setTop(label);
		
		BoutonRemove = new Button("X");
		setRight(BoutonRemove);
		BoutonRemove.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				VBox parent = (VBox) DownloadBar.this.getParent();
				parent.getChildren().remove(DownloadBar.this);
			}	
		});

		new Thread(new Runnable() {
			public void run() {
				download(url);
			}
		}).start();
	}

	private void download(String url) {

		try {
			downloader = new Downloader(url);
		} catch (Exception e) {
			System.err.format("skipping %s %s\n", url, e);
		}

		downloader.progressProperty().addListener((obs, o, n) -> {
			Platform.runLater(() -> {
				barre.setProgress((double) n);
			});
		});

		String filename = downloader.getFilename();
		try {
			downloader.run();
		} catch (Exception e) {
			System.err.println("failed!");
		}
		System.out.format("into %s/%s\n", Paths.get(".").toAbsolutePath().normalize().toString(), filename);

	}

}