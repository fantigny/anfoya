package net.anfoya.java.net.url.handler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import net.anfoya.java.net.url.connection.EmptyConnection;
import net.anfoya.java.net.url.filter.Matcher;
import sun.net.www.protocol.http.Handler;

public class FilteredHttpHandler extends Handler {
	private final Matcher matcher;

	public FilteredHttpHandler(final Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		if (matcher != null && matcher.matches(url.toString())) {
			return new EmptyConnection();
		}
		return super.openConnection(url);
	}
}
