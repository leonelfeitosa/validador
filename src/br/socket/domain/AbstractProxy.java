package br.socket.domain;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public abstract class AbstractProxy extends Thread {

    protected String name;
    protected Integer localPort;

    protected String contentToProcess;

    protected ServerSocket localSocket;

    protected Socket connectionDestinySocket;

    protected TargetAddress targetAddress;

    protected class ReceiverThread extends Thread {

        private Socket newSocketConnection;
        public ReceiverThread(Socket s){
            newSocketConnection = s;
        }

        @Override
        public void run() {
            super.run();
            try {
                receivingMessages(newSocketConnection);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected class ConnectionEstablishmentOriginThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                localSocket = new ServerSocket(localPort);
                while (true) {
                    new ReceiverThread(localSocket.accept()).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    protected class ConnectionEstablishmentDestinyThread extends Thread {
        @Override
        public void run() {
            super.run();

            Boolean connected = false;
            while(!connected) {
                try {
                    Thread.sleep(1L);
                    createConnectionWithDestiny();
                    connected = true;
                } catch (IOException | InterruptedException e) {
//            		throw new RuntimeException(e);
                }
            }
        }
    }



    public synchronized boolean hasSomethingToProcess(){
        return contentToProcess != null;
    }


    public synchronized void setContentToProcess(String contentToProcess) {
        this.contentToProcess = contentToProcess;
    }

    protected abstract void createConnectionWithDestiny() throws IOException;

    protected abstract void receivingMessages(Socket socket) throws IOException, InterruptedException;



    protected boolean isDestinyFree(Socket connectionSocket) throws IOException, ClassNotFoundException {
        DataOutputStream dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
        dataOutputStream.writeBytes("ping\n");
        dataOutputStream.flush();

        ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
        String message = (String) ois.readObject();
        if (message.equals("free")){
            return true;
        }
        return false;
    }


    protected void sendMessageToDestiny(String fileContent) throws IOException{
        sendMessageToDestiny(fileContent, connectionDestinySocket);
    }

    protected void sendMessageToDestiny(String fileContent, Socket connectionSocket) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(connectionSocket.getOutputStream());
        dataOutputStream.writeBytes(fileContent);
        dataOutputStream.flush();
    }


}
