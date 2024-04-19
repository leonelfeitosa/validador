package main.casestudies.casestudy04;

import br.socket.domain.ContainerProxy;
import br.socket.domain.ServerProxy;
import br.socket.domain.Source;
import br.socket.domain.TargetAddress;

import java.util.Arrays;


public class LocalTest_TwoServersOneContainer {

	public static void main(String[] args) throws InterruptedException {


		Integer queueServerMaxSize = 20;
		long arrivalDelay =400;
		Double MRTFromModel = 23712.0;

		int numberOfRequests = 50;

		Integer sourcePort = 1500;

		Integer serverPort1 = 6000;
		Integer serverPort2 = 2000;


		//***********************************
		Integer containerPortOne1 = 4000;
		new ContainerProxy("container1.1", containerPortOne1, new TargetAddress("localhost",serverPort2), 1000.0, false).start();
		TargetAddress containerOne1 = new TargetAddress("localhost", containerPortOne1);
		new ServerProxy("server01", serverPort1, Arrays.asList(containerOne1), queueServerMaxSize).start();
		//***********************************


		//***********************************
		Integer containerPortOne2 = 7000;
		new ContainerProxy("container2.1", containerPortOne2, new TargetAddress("localhost",sourcePort), 1000.0, true).start();
		TargetAddress containerOne2 = new TargetAddress("localhost", containerPortOne2);
		new ServerProxy("server02", serverPort2, Arrays.asList(containerOne2), queueServerMaxSize).start();
		//***********************************




		new Source("source", sourcePort, new TargetAddress("localhost",serverPort1) ,  numberOfRequests, arrivalDelay, MRTFromModel).start();

	}
}
