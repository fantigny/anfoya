package net.anfoya.javafx.scene.control;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

public class ResetNumericField extends ResetTextField {
	private static final UnaryOperator<Change> DEFAULT = c -> c.getText().matches("[0-9]*")? c: null;

	public ResetNumericField(int value) {
		this(value, DEFAULT);
	}

	public ResetNumericField(int value, UnaryOperator<Change> filter) {
		textProperty().set("" + value);
		setTextFormatter(new TextFormatter<String>(filter));
	}

	public int getNumeric() {
		return Integer.valueOf(textProperty().get());
	}
}
