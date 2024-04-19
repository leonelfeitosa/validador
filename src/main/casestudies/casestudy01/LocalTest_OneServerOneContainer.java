package main.casestudies.casestudy01;

import br.socket.domain.*;
import br.socket.domain.Source;

import java.util.Arrays;


public class LocalTest_OneServerOneContainer {

	public static void main(String[] args) throws InterruptedException {

		Integer sourcePort = 5000;
		Integer serverPort = 6000;
		Integer containerPort = 7000;

		Integer queueServerMaxSize = 20;
		long arrivalDelay =400;
		Double MRTFromModel = 20900.36;

		int numberOfRequests = 200;

		new ContainerProxy("container01", containerPort, new TargetAddress("localhost",sourcePort), 1000.0, true).start();
		new ServerProxy("server01", serverPort, Arrays.asList(new TargetAddress("localhost", containerPort)), queueServerMaxSize).start();
		new Source("source", sourcePort, new TargetAddress("localhost",serverPort) ,  numberOfRequests, arrivalDelay, MRTFromModel).start();

	}
}
