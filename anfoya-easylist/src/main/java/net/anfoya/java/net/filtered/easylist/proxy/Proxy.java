package net.anfoya.java.net.filtered.easylist.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.anfoya.java.net.url.filter.Matcher;
import net.anfoya.java.net.url.filter.RuleSet;
import net.anfoya.java.util.concurrent.ThreadPool;

public class Proxy implements Runnable {

	private final ServerSocket serverSocket;
	private final Matcher matcher;

	private volatile boolean running = true;

	public Proxy(int port, RuleSet ruleSet) throws IOException {
		serverSocket = new ServerSocket(port);
		matcher = new Matcher(ruleSet);
		running = true;
	}

	@Override
	public void run(){
		while(running){
			try {
				// serverSocket.accpet() Blocks until a connection is made
				final Socket socket = serverSocket.accept();
				// Create a new RequestHandler
				ThreadPool.getDefault().mustRun("proxy", new RequestHandler(socket, matcher));
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void stop() {
		running = false;
	}
}
