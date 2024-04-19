package main.casestudies.casestudy03;

import br.socket.domain.ContainerProxy;
import br.socket.domain.ServerProxy;
import br.socket.domain.Source;
import br.socket.domain.TargetAddress;

import java.util.Arrays;


public class LocalTest_OneServerThreeContainers {

	public static void main(String[] args) throws InterruptedException {

		Integer sourcePort = 5000;
		Integer serverPort = 6000;
		Integer containerPortOne = 7000;
		Integer containerPortTwo = 8000;
		Integer containerPortThree = 9000;

		Integer queueServerMaxSize = 20;
		long arrivalDelay =400;
		Double MRTFromModel = 999.36;

		int numberOfRequests = 200;

		new ContainerProxy("container01", containerPortOne, new TargetAddress("localhost",sourcePort), 1000.0, true).start();
		new ContainerProxy("container02", containerPortTwo, new TargetAddress("localhost",sourcePort), 1000.0, true).start();
		new ContainerProxy("container03", containerPortThree, new TargetAddress("localhost",sourcePort), 1000.0, true).start();
		TargetAddress containerOne = new TargetAddress("localhost", containerPortOne);
		TargetAddress containerTwo = new TargetAddress("localhost", containerPortTwo);
		TargetAddress containerThree = new TargetAddress("localhost", containerPortThree);
		new ServerProxy("server01", serverPort, Arrays.asList(containerOne,containerTwo, containerThree), queueServerMaxSize).start();
		new Source("source", sourcePort, new TargetAddress("localhost",serverPort) ,  numberOfRequests, arrivalDelay, MRTFromModel).start();

	}
}
