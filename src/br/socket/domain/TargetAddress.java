package br.socket.domain;


public class TargetAddress {
    private String ip;
    private Integer port;

    public TargetAddress(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public Integer getPort() {
        return port;
    }
}
