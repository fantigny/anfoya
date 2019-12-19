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
			HTMLEditor editor = (HTMLEditor) event.getSource();
			WebView view = (WebView) editor.lookup(".web-view");
			KeyEvent enterKeyEvent = new KeyEvent(
					editor, view,
					KeyEvent.KEY_TYPED,
					"\r", "", KeyCode.ENTER,
					false, false, false, false); 
			
			event.consume();
			view.fireEvent(enterKeyEvent);
		}
	}
}
