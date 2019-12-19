package net.anfoya.javafx.scene.control;

import com.sun.javafx.PlatformUtil;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;

public class HtmlEditorLinuxEnterFix implements EventHandler<KeyEvent> {
	private static final boolean IS_LINUX = PlatformUtil.isLinux();

	@Override
	public void handle(KeyEvent event) {
		if (!IS_LINUX) {
			return;
		}

		// fix <enter> key ignored on linux
		if (event.getCode() == KeyCode.ENTER) {
			event.consume();
			HTMLEditor editor = (HTMLEditor) event.getSource();
			WebView editorView = (WebView) editor.lookup("wev-view");
			editorView.fireEvent(new KeyEvent(editor, editorView, KeyEvent.KEY_TYPED,
					"\r", "", KeyCode.ENTER,
					false, true, false, false));
		}
	}
}
