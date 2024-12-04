package scc2425.impl.rest;

import jakarta.ws.rs.core.Application;
import utils.IP;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class TukanoBlobService extends Application {
	final private static Logger Log = Logger.getLogger(TukanoBlobService.class.getName());

	static final String INETADDR_ANY = "0.0.0.0";
	static String SERVER_BASE_URI = "http://%s:%s/rest";

	public static final int PORT = 8080;

	public static String serverURI;

	private Set<Object> singletons = new HashSet<>();
	private Set<Class<?>> resources = new HashSet<>();

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

	public TukanoBlobService() {
		serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT);

		singletons.add(new RestBlobsResource());
	}

	public static void main(String[] args) { }
}
