package net.anfoya.java.net.filtered.easylist.proxy;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;

import net.anfoya.java.net.url.filter.Matcher;

public class RequestHandler implements Runnable {

	private final Socket clientSocket;
	private final BufferedReader proxyToClientBr;
	private final BufferedWriter proxyToClientBw;
	private final Matcher matcher;

	public RequestHandler(Socket clientSocket, Matcher matcher) throws IOException {
		this.clientSocket = clientSocket;
		this.clientSocket.setSoTimeout(2000);
		this.matcher = matcher;

		proxyToClientBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		proxyToClientBw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
	}

	@Override
	public void run() {
		String requestString;
		try {
			requestString = proxyToClientBr.readLine();
		} catch (final IOException e) {
			System.out.println("Error reading request from client");
			return;
		}

		String urlString = requestString.substring(requestString.indexOf(' ') + 1);
		urlString = urlString.substring(0, urlString.indexOf(' '));
		if (!urlString.substring(0, 4).equals("http")) {
			final String temp = "http://";
			urlString = temp + urlString;
		}

		if (matcher.matches(urlString)) {
			System.out.println("Blocked site requested : " + urlString);
			blockedSiteRequested();
			return;
		}

		System.out.println("HTTP GET for : " + urlString + "\n");
		handleHttpRequest(urlString);
	}

	private void handleHttpRequest(String urlString) {
		try {
			final int fileExtensionIndex = urlString.lastIndexOf(".");
			String fileExtension = urlString.substring(fileExtensionIndex, urlString.length());
			if (fileExtension.contains("/")) {
				fileExtension = fileExtension.replace("/", "__");
				fileExtension = fileExtension.replace('.', '_');
				fileExtension += ".html";
			}

			if (fileExtension.contains(".png") || fileExtension.contains(".jpg") || fileExtension.contains(".jpeg")
					|| fileExtension.contains(".gif")) {
				final URL remoteURL = new URL(urlString);
				final BufferedImage image = ImageIO.read(remoteURL);
				if (image != null) {
					final String line = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
					proxyToClientBw.write(line);
					proxyToClientBw.flush();
					ImageIO.write(image, fileExtension.substring(1), clientSocket.getOutputStream());
				} else {
					final String error = "HTTP/1.0 404 NOT FOUND\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
					proxyToClientBw.write(error);
					proxyToClientBw.flush();
					return;
				}
			} else {
				final URL remoteURL = new URL(urlString);
				final HttpURLConnection proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
				proxyToServerCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				proxyToServerCon.setRequestProperty("Content-Language", "en-US");
				proxyToServerCon.setUseCaches(false);
				proxyToServerCon.setDoOutput(true);
				try (final BufferedReader proxyToServerBR = new BufferedReader(
						new InputStreamReader(proxyToServerCon.getInputStream()))) {
					String line = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
					proxyToClientBw.write(line);
					while ((line = proxyToServerBR.readLine()) != null) {
						// Send on data to client
						proxyToClientBw.write(line);
					}
					proxyToClientBw.flush();
				}
			}

			if (proxyToClientBw != null) {
				proxyToClientBw.close();
			}
		}

		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void blockedSiteRequested() {
		try {
			final BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(clientSocket.getOutputStream()));
			final String line = "HTTP/1.0 403 Access Forbidden \n" + "User-Agent: ProxyServer/1.0\n" + "\r\n";
			bufferedWriter.write(line);
			bufferedWriter.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
