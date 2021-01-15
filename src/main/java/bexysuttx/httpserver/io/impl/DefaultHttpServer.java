package bexysuttx.httpserver.io.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bexysuttx.httpserver.io.HttpServer;
import bexysuttx.httpserver.io.config.HttpServerConfig;
import bexysuttx.httpserver.io.exception.HttpServerException;


class DefaultHttpServer implements HttpServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpServer.class);
	private final HttpServerConfig httpServerConfig;
	private final ServerSocket serverSocket;
	private final ExecutorService executorService;
	private final Thread mainServerThread;
	private volatile boolean serverStopped;

	protected DefaultHttpServer(HttpServerConfig httpServerConfig) {
		super();
		this.httpServerConfig = httpServerConfig;
		this.serverSocket = createServerSocket();
		this.executorService = createExecutorService();
		this.mainServerThread = createMainServerThread(createServerRunnable());
		this.serverStopped = false;
	}

	private Thread createMainServerThread(Runnable r) {
		Thread th = new Thread(r, "Main server thread");
		th.setDaemon(false);
		th.setPriority(Thread.MAX_PRIORITY);
		return th;
	}

	private Runnable createServerRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				while (!mainServerThread.isInterrupted()) {
					try {
						Socket clientSocket = serverSocket.accept();
						executorService.submit(httpServerConfig.buildNewHttpClientSocketHandler(clientSocket));
					} catch (IOException e) {
						if (!serverSocket.isClosed()) {
								LOGGER.error("Can't accept client socket: " + e.getMessage(), e);
						}
						destroyHttpServer();
						break;
					}
				}
				System.exit(0);
			}

		};
	}

	private ExecutorService createExecutorService() {
		ThreadFactory threadFactory = httpServerConfig.getWorkerThreadFactory();
		int th = httpServerConfig.getServerInfo().getThreadCount();
		if (th > 0) {
			return Executors.newFixedThreadPool(th, threadFactory);
		} else {
			return Executors.newCachedThreadPool(threadFactory);
		}
	}

	private ServerSocket createServerSocket() {
		try {
			ServerSocket serverSocket = new ServerSocket(httpServerConfig.getServerInfo().getPort());
			serverSocket.setReuseAddress(false);
			return serverSocket;
		} catch (IOException e) {
			throw new HttpServerException(
					"Can't create server socket with port= " + httpServerConfig.getServerInfo().getPort());

		}
	}

	@Override
	public void start() {
		if (mainServerThread.getState() != Thread.State.NEW) {
			throw new HttpServerException(
					"Current web server already started or stopped! Please create a new http server instance");
		}
		Runtime.getRuntime().addShutdownHook(getShutdownHook());
		mainServerThread.start();
		LOGGER.info("Server started: " + httpServerConfig.getServerInfo());

	}

	private Thread getShutdownHook() {
		return new Thread(new Runnable() {

			@Override
			public void run() {
				if (!serverStopped) {
					destroyHttpServer();
				}
			}

		}, "ShutdownHook");
	}

	protected void destroyHttpServer() {
		try {
			httpServerConfig.close();
		} catch (Exception e) {
			LOGGER.error("Close httpServerConfig failed: " + e.getMessage(), e);
		}
		executorService.shutdownNow();
		LOGGER.info("Server stopped");
		serverStopped = true;
	}

	@Override
	public void stop() {
		LOGGER.info("Detected stop cmd");
		mainServerThread.interrupt();
		try {
			serverSocket.close();
		} catch (IOException e) {
			LOGGER.warn("Error during close server socket: " + e.getMessage(), e);
		}

	}

}
