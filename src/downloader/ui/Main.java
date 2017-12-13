package downloader.ui;

import com.sun.glass.events.MouseEvent;
import com.sun.webkit.dom.KeyboardEventImpl;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	public void start(Stage stage) {

		BorderPane global = new BorderPane();
		ScrollPane scroll = new ScrollPane();
		VBox barres = new VBox();
		global.setCenter(scroll);
		scroll.setContent(barres);
		scroll.setFitToWidth(true);

		BorderPane bottom = new BorderPane();
		Button BoutonAdd = new Button("Add");
		TextField text = new TextField();
		text.setPromptText("Enter an URL");
		bottom.setCenter(text);
		bottom.setRight(BoutonAdd);
		global.setBottom(bottom);

		global.setPrefWidth(600);
		global.setPrefHeight(300);
		stage.setScene(new Scene(global));
		stage.setTitle("Downloader");
		stage.show();
		
		for (String url : getParameters().getRaw()) {
			barres.getChildren().add(new DownloadBar(url));
		}
		
		BoutonAdd.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				barres.getChildren().add(new DownloadBar(text.getText()));
				text.clear();
			}
		});
		
		text.setOnAction((event) -> {
			barres.getChildren().add(new DownloadBar(text.getText()));
			text.clear();
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

}
