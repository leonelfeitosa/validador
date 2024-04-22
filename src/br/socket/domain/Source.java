package br.socket.domain;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * O Source apesar de herdar de AbstractProxy funciona apenas como um gerador de dados sintéticos.
 * O Source gera um número de requisições numberOfRequests em um intervalo de tempo pre determinado (arrivalDelay).
 * Em caso desejado o avaliador pode inserir o valor do MRT advindo do modelo para ao final da execução já mostrar a comparação estatística com o experimento.
 *  @author Airton
 */
public class Source extends  AbstractProxy{

    public static void main(String[] args) throws IOException {

        Properties prop = new Properties();

        String currentDir = System.getProperty("user.dir");
        String configPath = currentDir + "/source.properties";

        FileInputStream file = new FileInputStream(configPath);
        prop.load(file);

        System.out.println(prop.getProperty("sourcePort"));

        Integer targetPort = Integer.parseInt(prop.getProperty("sourcePort"));
        String serverIp = prop.getProperty("serverIp");
        Integer serverPort = Integer.parseInt(prop.getProperty("serverPort"));
        long numberOfRequests = Long.parseLong(prop.getProperty("numberOfRequests"));
        long arrivalDelay = Long.parseLong(prop.getProperty("arrivalDelay"));
        Double MRTFromModel = Double.parseDouble(prop.getProperty("MRTFromModel"));

        new Source("source", targetPort, new TargetAddress(serverIp,serverPort) ,  numberOfRequests, arrivalDelay, MRTFromModel).start();
    }


    private Double MRTFromModel;
    private long numberOfRequests;
    private long arrivalDelay;

    private List<String> arrivedMessages;

    public Source(String name, Integer localPort, TargetAddress targetAddress, long numberOfRequests, long arrivalDelay, Double MRTFromModel) {
        super();
        this.name = name;
        this.localPort = localPort;
        this.targetAddress = targetAddress;
        this.numberOfRequests =  numberOfRequests;
        this.arrivalDelay = arrivalDelay;
        arrivedMessages = new ArrayList<>();
        this.MRTFromModel = MRTFromModel;
    }

    public void run() {
        try {

            new ConnectionEstablishmentOriginThread().start();
            new ConnectionEstablishmentDestinyThread().start();
            System.out.println("Starting source");

            String msg = null;

            for (int i = 0; i < numberOfRequests; i++) {
                if ((i + 1) == numberOfRequests){
                    msg = "last" + ";" + System.currentTimeMillis() + ";" + "\n";
                }else{
                    msg = i + ";" + System.currentTimeMillis() + ";" + "\n";
                }

                if (isDestinyFree(connectionDestinySocket)) {
                    sendMessageToDestiny(msg);
                } else{
                    System.err.print("DROPPED IN SOURCE " + msg);
                }

                try {Thread.sleep(arrivalDelay);} catch (InterruptedException e) {e.printStackTrace();}
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String registerMRTAtTheEnd(String receivedMessage) {
        Long last = Long.valueOf(receivedMessage.split(";")[receivedMessage.split(";").length - 2]);
        Long first = Long.valueOf(receivedMessage.split(";")[1]);
        long currentMRT = last - first;
        receivedMessage += "MRT:;"+ currentMRT + ";";
        return receivedMessage;
    }


    protected void receivingMessages(Socket newSocketConnection) {
        try {

            System.err.println(name+" enabled to receive messages.");

            long allMeanTimes = 0;
            Double MRTFromExperiment = 0.0;
            String receivedMessage = null;
            List<Double> mrts = new ArrayList<>();
            List<Double> filteredMrts = new ArrayList<>();
            double sd = 0;

            DataInputStream dataInputStream = new DataInputStream(newSocketConnection.getInputStream());
            while (true) {
                if (dataInputStream.available() > 0) {
                    receivedMessage = dataInputStream.readLine();
                }

                if (receivedMessage!= null){
                    receivedMessage = registerMRTAtTheEnd(receivedMessage);
                    arrivedMessages.add(receivedMessage);
                    System.out.println(receivedMessage);


                    String index = receivedMessage.split(";")[0];
                    boolean amongTheLastOnes = Utils.isNumeric(index) && Integer.parseInt(index) >= (numberOfRequests - 3);
//                    if (index.equals("last") || amongTheLastOnes){
                    if (amongTheLastOnes){
                        for (String a: arrivedMessages) {
                            mrts.add((double) Long.valueOf(a.split(";")[a.split(";").length-1]));
                        }

                        //FILTRAR APENAS DA METADE PARA FRENTE

                        for (int i = mrts.size()/2; i < mrts.size(); i++){
                            filteredMrts.add(mrts.get(i));
                            allMeanTimes += mrts.get(i);
                        }

                        MRTFromExperiment = (double) (allMeanTimes/filteredMrts.size());

                        sd = Utils.calculateStandardDeviation(filteredMrts);

                        System.out.println("MRT From Model: " + MRTFromModel +
                                "; MRT From Experiment: " + MRTFromExperiment +
                                "; SD From Experiment: " + sd +
                                "; Experiment CI: ["+(MRTFromExperiment-sd)+"-"+(MRTFromExperiment+sd)+"]");

                        if ((MRTFromExperiment-sd) <= MRTFromModel && MRTFromModel <=   (MRTFromExperiment+sd)){
                            System.out.println("Validated Model, the model's MRT is inside the Experiment CI !!");
                        }else{
                            System.err.println("Non Validated Model !!");
                        }

                    }


                    receivedMessage = null;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void createConnectionWithDestiny() throws IOException {
        connectionDestinySocket = new Socket(targetAddress.getIp(),targetAddress.getPort());
    }

}
