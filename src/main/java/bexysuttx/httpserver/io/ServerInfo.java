package bexysuttx.httpserver.io;

public class ServerInfo {

	private  final String name;
	private  final int port;
	private  final int threadCount;

	public String getName() {
		return name;
	} 

	public int getPort() {
		return port;
	}


	public int getThreadCount() {
		return threadCount;
	}


	public ServerInfo(String name, int port, int threadCount) {
		super();
		this.name = name;
		this.port = port;
		this.threadCount = threadCount;
	}

	@Override
	public String toString() {
		return "ServerInfo [name=" + name + ", port=" + port + ", threadCount=" + threadCount + "]";
	}

}
