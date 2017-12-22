package io.github.flylib.seckill.common;

public class Message<T> {

    private Head head;

    private T body;

    public  Message() {

    }

    public Message(Head head, T body) {
        this.head = head;
        this.body = body;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Message(SecKillEnum resultEnum, T body){
        this.head = new Head();
        this.head.setStatusCode(resultEnum.getCode());
        this.head.setStatusMessage(resultEnum.getMessage());
        this.body = body;
    }

    public Message(SecKillEnum resultEnum){
        this.head = new Head();
        this.head.setStatusCode(resultEnum.getCode());
        this.head.setStatusMessage(resultEnum.getMessage());
    }

}
