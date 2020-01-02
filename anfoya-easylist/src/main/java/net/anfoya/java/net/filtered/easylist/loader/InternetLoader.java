package net.anfoya.java.net.filtered.easylist.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anfoya.java.net.filtered.easylist.EasyListRuleSet;
import net.anfoya.java.net.filtered.easylist.model.Rule;
import net.anfoya.java.net.filtered.easylist.parser.Parser;
import net.anfoya.java.net.filtered.easylist.parser.ParserException;

public class InternetLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(InternetLoader.class);

	private static final Set<URL> URLs;

	static {
		URLs = new HashSet<>();
	}

	private final URL url;

	public InternetLoader(final URL url) {
		this.url = url;
	}

	public EasyListRuleSet load() {
		if (URLs.contains(url)) {
			URLs.remove(url);
			return new EasyListRuleSet(false);
		}
		LOGGER.info("loading {}", url);
		final long start = System.currentTimeMillis();
		EasyListRuleSet easyList;
		// avoid handler factory re-entrance
		URLs.add(url);
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			easyList = new EasyListRuleSet(false);
			final Parser parser = new Parser();
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					final Rule rule = parser.parse(line);
					easyList.add(rule);
				} catch (final ParserException e) {
					LOGGER.error("parsing {}", line, e);
				}
			}
			LOGGER.info("loaded {} rules (in {}ms)", easyList.getRuleCount(), System.currentTimeMillis() - start);
		} catch (final IOException e) {
			LOGGER.error("reading {}", url, e);
			easyList = new EasyListRuleSet(false);
		}

		return easyList;
	}
}