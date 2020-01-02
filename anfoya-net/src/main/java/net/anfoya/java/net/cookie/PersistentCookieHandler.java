package net.anfoya.java.net.cookie;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import net.anfoya.java.io.JsonFile;

public class PersistentCookieHandler extends CookieHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PersistentCookieHandler.class);
	private static final String COOKIE_FILEPATH = System.getProperty("java.io.tmpdir") + File.separatorChar + "cookies.json";

	private final JsonFile<Map<URI, Map<String, List<String>>>> file;

	private Map<URI, Map<String, List<String>>> cookieMap;

	public PersistentCookieHandler() {
		cookieMap = new TreeMap<>();
		file = new JsonFile<>(COOKIE_FILEPATH);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> save()));
	}

	public void load() {
		if (!file.exists()) {
			cookieMap = new TreeMap<>();
			return;
		}
		try {
			cookieMap = file.load(new TypeToken<Map<URI, Map<String, List<String>>>>(){}.getType());
			LOGGER.info("loaded {} cookies", cookieMap.values().size());
		} catch (final Exception e) {
			LOGGER.warn("reading {}", this, e);
		}
	}

	private void save() {
		LOGGER.info("saving {} cookies", cookieMap.values().size());
		try {
			file.save(cookieMap);
		} catch (final IOException e) {
			LOGGER.error("writing {}", this, e);
		}
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
		if (!cookieMap.containsKey(uri)) {
			cookieMap.put(uri, new HashMap<String, List<String>>());
		}
		return cookieMap.get(uri);
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		cookieMap.put(uri, responseHeaders);
	}
}
