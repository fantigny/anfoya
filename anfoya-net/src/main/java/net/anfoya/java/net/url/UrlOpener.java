package net.anfoya.java.net.url;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anfoya.java.util.system.OperatingSystem;

public class UrlOpener {
	private static final Logger LOGGER = LoggerFactory.getLogger(UrlOpener.class);

	public void open(final String address) throws IOException {
		LOGGER.info("starting: {}", address);
		switch (OperatingSystem.getInstance().getFamily()) {
		case MAC: {
			final Process process = Runtime.getRuntime().exec(new String[] { "open", address });
			final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				LOGGER.debug(line);
			}
			break;
		}
		case UNX: {
			final Process process = Runtime.getRuntime().exec(new String[] { "xdg-open", address });
			final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				LOGGER.debug(line);
			}
			break;
		}
		case WIN: {
			try {
				Desktop.getDesktop().browse(new URI(address));
			} catch (final URISyntaxException e) {
				final Process process = Runtime.getRuntime().exec(new String[] { "explorer", address });
				final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					LOGGER.debug(line);
				}
			}
			break;
		}
		default:
			LOGGER.error("opening {}", address);
		}
	}
}