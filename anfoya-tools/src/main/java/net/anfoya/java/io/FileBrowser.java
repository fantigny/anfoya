package net.anfoya.java.io;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import net.anfoya.java.nio.FolderOrganiser;
import net.anfoya.java.util.VoidCallback;

public class FileBrowser extends Application {
	private static final Path SOURCE = Paths.get("/Volumes/movies/dl/");

	private FolderOrganiser organiser;
	private ExecutorService executor;

	@Override
	public void init() throws Exception {
		super.init();

		organiser = new FolderOrganiser(SOURCE);
		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void start(Stage stage) throws Exception {
		final ListView<String> list = new ListView<>();
		list.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2 && !list.getSelectionModel().getSelectedItems().isEmpty()) {
				try {
					Desktop.getDesktop().open(organiser.getPath(list.getSelectionModel().getSelectedItem()).toFile());
				} catch (final IOException ex) {
					new Alert(AlertType.ERROR, ex.getMessage());
				}
			}
		});

		stage.setScene(new Scene(new BorderPane(list)));
		stage.initStyle(StageStyle.UNIFIED);
		stage.setOnCloseRequest(e -> stop(e));
		stage.show();

		loadFileNames(c -> list.getItems().setAll(c));
	}

	private void stop(WindowEvent e) {
		executor.shutdown();
	}

	private void loadFileNames(VoidCallback<Collection<String>> callback) {
		final Task<Collection<String>> task = new Task<Collection<String>>() {
			@Override protected Collection<String> call() throws Exception {
				return organiser
						.reload()
						.organise(20, 4)
						.cleanUp()
						.getFilenames();
			}
		};
		task.setOnSucceeded(e -> callback.call(task.getValue()));
		task.setOnFailed(e -> new Alert(AlertType.ERROR, e.getSource().getException().getMessage()));

		executor.submit(task);
	}
}
