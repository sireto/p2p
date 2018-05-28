package com.reversehash.communication.message;

@MessageType(type=1,version=0)
public class TextMessage extends Message{
    public String text;
    public TextMessage(String message){
        text=message;
    }
    @Override
    public void handle() {
        System.out.println("Received new Text messages : "+text);
    }

    @Override
    public byte[] toBytes() {
        return text.getBytes();
    }

    @Override
    public void readBytes(int version,byte[] input) {
        this.text=new String(input);
    }


    // it's kinda static method but allows us to determine messages type at runtime.

}
