package net.anfoya.javafx.util;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.ptr.PointerByReference;

public class WinShell32 {

	public static long setExplicitAppUserModelId(String id) {
		return Shell32
				.INSTANCE
				.SetCurrentProcessExplicitAppUserModelID(new WString(id))
				.longValue();
	}

	public static String getCurrentProcessExplicitAppUserModelID() {
		final PointerByReference pRef = new PointerByReference();
		Shell32.INSTANCE.GetCurrentProcessExplicitAppUserModelID(pRef);

		final Pointer pointer = pRef.getValue();
		final String currentId = pointer.getWideString(0);
		Ole32.INSTANCE.CoTaskMemFree(pointer);

		return currentId;
	}
}
