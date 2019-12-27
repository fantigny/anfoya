package net.anfoya.javafx.scene.control;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.StringConverter;
import net.anfoya.java.lang.StringHelper;
import net.anfoya.java.util.concurrent.ThreadPool;
import net.anfoya.java.util.concurrent.ThreadPool.PoolPriority;

public class ComboField<K> extends TextField {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComboField.class);

	private final ObservableList<K> items;

	private final Popup popup;
	private final ListView<K> listView;
	private final BooleanProperty showingProperty;
	private volatile boolean firstShow;

	private Callback<K, K> textFactory;
	private Task<ObservableList<K>> filterTask;
	private int filterTaskId;

	private volatile boolean emptyBackspaceReady;
	private EventHandler<ActionEvent> backspaceHandler;

	private double cellHeight;
	private double cellWidth;

	private Callback<ListView<K>, ListCell<K>> cellFactory;

	private EventHandler<ActionEvent> fieldActionCallback;

	private EventHandler<ActionEvent> listRequestCallback;

	private StringConverter<K> converter;

	public ComboField() {
		super("");
		getStyleClass().add("combofield");
		this.items = FXCollections.observableArrayList();

		listView = new ListView<>();
		listView.setCellFactory(new Callback<ListView<K>, ListCell<K>>() {
			@Override public ListCell<K> call(final ListView<K> listView) {
				final ListCell<K> cell = cellFactory  == null? new ListCell<>(): cellFactory.call(listView);
				if (firstShow) {
					cell.widthProperty().addListener((ov, o, n) -> {
						updateCellSize(cell.getHeight(), cell.getWidth());
					});
				}
				return cell;
			}
		});
		listView.setOnMouseClicked(e -> {
			if (!listView.getSelectionModel().isEmpty()) {
				actionFromListView();
			}
		});
		listView.setOnKeyPressed(e -> {
			if (KeyCode.ENTER == e.getCode()) {
				if (!listView.getSelectionModel().isEmpty()) {
					actionFromListView();
				} else if (!getText().isEmpty()) {
					actionFromTextField();
				}
			}
		});

		popup = new Popup();
		popup.getContent().add(listView);

		firstShow = true;
		popup.setOnShown(e -> firstShow = false);

		showingProperty = new SimpleBooleanProperty(false);

		setContextMenu(new ContextMenu(new MenuItem("", listView)));

		textFactory = null;

		emptyBackspaceReady = true;
		setOnKeyPressed(e -> {
			if (KeyCode.BACK_SPACE == e.getCode()
					&& emptyBackspaceReady
					&& backspaceHandler != null) {
				backspaceHandler.handle(new ActionEvent());
			}
		});

		textProperty().addListener((ov, o, n) -> {
			emptyBackspaceReady = n.isEmpty();
			filter(n);
		});

		focusedProperty().addListener((ov, o, n) -> {
			if (o && !n && isShowing()) {
				hide();
			}
		});

		showingProperty().addListener((ov, o, n) -> {
			if (n) {
				final Point2D pos = localToScene(0, 0);
				popup.show(this
						, getScene().getWindow().getX() + getScene().getX() + pos.getX()
						, getScene().getWindow().getY() + getScene().getY() + pos.getY() + getBoundsInParent().getHeight());
			} else {
				popup.hide();
			}
		});
	}

	public void setFilter(final Callback<K, K> textFactory) {
		this.textFactory = textFactory;
	}

	public void setItems(final Set<K> items) {
		this.items.setAll(items);
		if (isShowing()) {
			filter(getText());
		}
	}

	public void setConverter(StringConverter<K> converter) {
		this.converter = converter;
	}

	public void setFieldValue(K item) {
		setText(converter.toString(item));
	}

	public K getFieldValue() {
		return converter.fromString(getText());
	}

	public ObservableList<K> getItems() {
		return listView.getItems();
	}

	public boolean isShowing() {
		return showingProperty.get();
	}

	public BooleanProperty showingProperty() {
		return showingProperty;
	}

	public void show() {
		showingProperty.set(true);
	}

	public void hide() {
		showingProperty.set(false);
	}

	public void setCellFactory(final Callback<ListView<K>, ListCell<K>> factory) {
		cellFactory =  factory;
	}

	public void setCellSize(final double height, final double width) {
		cellHeight = height;
		cellWidth = width;
	}

	public void setOnFieldAction(EventHandler<ActionEvent> callback) {
		fieldActionCallback = callback;
	}

	public void setOnListRequest(EventHandler<ActionEvent> callback) {
		listRequestCallback = callback;
	}

	public void setOnBackspaceAction(final EventHandler<ActionEvent> handler) {
		backspaceHandler = handler;
	}

	private synchronized void filter(final String n) {
		final long taskId = ++filterTaskId;
		if (filterTask != null && filterTask.isRunning()) {
			filterTask.cancel();
		}

		if (n.isEmpty()) {
			hide();
			LOGGER.debug("cancel filter, text is empty", n);
			return;
		}

		filterTask = new Task<ObservableList<K>>() {
			@Override
			protected ObservableList<K> call() throws Exception {
				return FXCollections.observableArrayList(items.filtered(s ->
				textFactory == null
				? StringHelper.containsIgnoreCase(s.toString(), n)
						: StringHelper.containsIgnoreCase(textFactory.call(s).toString(), n)));
			}
		};
		filterTask.setOnSucceeded(e -> {
			if (taskId != filterTaskId) {
				return;
			}
			final ObservableList<K> filtered = filterTask.getValue();
			if (filtered.isEmpty()) {
				hide();
				LOGGER.debug("no item match");
			} else {
				updatePopup(filtered);
				show();
				LOGGER.debug("filtered {} item(s)", listView.getItems().size());
			}
		});
		filterTask.setOnFailed(e -> LOGGER.error("filtering items with {}", n, e.getSource().getException()));
		ThreadPool.getDefault().submit(PoolPriority.MAX, "filtering items with " + n, filterTask);
	}

	private void updatePopup(final ObservableList<K> items) {
		final double height = Math.max(25, Math.min(200, items.size() * ((cellHeight > 0? cellHeight:24) + 1)));
		final double width = Math.max(100, Math.min(500, cellWidth > 0? cellWidth: 500));

		LOGGER.debug("height {} width {}", height, width);
		listView.setMaxHeight(height);
		listView.setMinHeight(height);
		listView.setPrefHeight(height);
		popup.setHeight(height);
		listView.setMaxWidth(width);
		listView.setMinWidth(width);
		listView.setPrefWidth(width);
		popup.setWidth(width);

		listView.getItems().setAll(items);
	}

	private void updateCellSize(final double height, final double width) {
		if (!firstShow) {
			return;
		}
		if (cellHeight < height) {
			cellHeight = height;
			LOGGER.debug("update cell height {}", height);
		}
		if (cellWidth <=0 && cellWidth < width) {
			cellWidth = width;
			LOGGER.debug("update cell width {}", width);
		}
	}

	private void actionFromTextField() {
		LOGGER.warn("action from textfield, text: {}", getText());
		getOnAction().handle(new ActionEvent());
		if (fieldActionCallback != null) {
			fieldActionCallback.handle(new ActionEvent());
		}
	}

	private void actionFromListView() {
		LOGGER.warn("action from listview, selected item: {}", listView.getSelectionModel().getSelectedItem());
		setText(listView.getSelectionModel().getSelectedItem().toString());
		getOnAction().handle(new ActionEvent());
		hide();
		if (listRequestCallback != null) {
			listRequestCallback.handle(new ActionEvent());
		}
	}
}
