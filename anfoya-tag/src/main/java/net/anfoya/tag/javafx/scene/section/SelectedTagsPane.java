package net.anfoya.tag.javafx.scene.section;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import net.anfoya.java.util.VoidCallback;
import net.anfoya.javafx.scene.control.RemoveLabel;
import net.anfoya.tag.service.Tag;

public class SelectedTagsPane<T extends Tag> extends FlowPane {

	private VoidCallback<T> removeTagCallBack;

	public SelectedTagsPane() {
		getStyleClass().add("label-list-pane");
		setVgap(3);
		setHgap(3);
		setPadding(new Insets(3));
	}

	public void refresh(final Set<T> tags) {
		// sort tags in alphabetical order with system tags first
		getChildren().setAll(Stream.concat(
				tags
					.stream()
					.filter(t -> t.isSystem())
					.sorted()
					.map(t -> createLabel(t)),
				tags
					.stream()
					.filter(t -> !t.isSystem())
					.sorted()
					.map(t -> createLabel(t)))
				.collect(Collectors.toList()));
	}

	private Label createLabel(final T tag) {
		final RemoveLabel label = new RemoveLabel(tag.getName(), "remove label");
		label.setOnRemove(e -> {
			if (removeTagCallBack != null) {
				getChildren().remove(label);
				removeTagCallBack.call(tag);
			}
		});
		return label;
	}

	public void setRemoveTagCallBack(final VoidCallback<T> callback) {
		this.removeTagCallBack = callback;
	}

	public void clear() {
		getChildren().clear();
	}
}
