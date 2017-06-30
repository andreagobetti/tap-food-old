package com.lynkteam.tapmanager.printers;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by robertov on 17/08/15.
 */


public class PrinterEpson{
    private Socket socket;

    private PrintStream pstream;

    public PrinterEpson(String IP, int port) throws Exception {
        InetAddress serverAddr = InetAddress.getByName(IP);
        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddr, port), 2000);
    }

    public void close() throws Exception{
        //post: closes the stream, used when printjob ended
        pstream.close();

    }

    public void initialize() throws  Exception{
        //post: returns true iff stream to network printer successfully opened, streams for writing to esc/p printer created

        pstream = new PrintStream(socket.getOutputStream());
        pstream.println((char) 27 + "@");
    }

    public void partialCut(){
        pstream.println((char) 29 + "V" + (char) 65 + (char) 0);
    }

    public void newLine(){
        pstream.println(" ");
    }

    public void textAlignCenter(){
        pstream.println((char)27 + "a" + (char)1);
    }

    public void textAlignLeft(){
        pstream.println((char)27 + "a" + (char)0);
    }

    public void textAlignRight(){
        pstream.println((char)27 + "a" + (char)2);
    }

    public void print(String text) {
        pstream.println(text);
    }

    public void textSizeBig()
    {
        pstream.println((char)29 + "!" + (char)17);
    }

    public void textSizeNormal()
    {
        pstream.println((char)29 + "!" + (char)1);
    }

    public void textSizeSmall(){ pstream.println((char)29 + "!" + (char)0 );}


    public String fillStr(String str, int len, char fillWith) {
        for (int i = str.length(); i < len; i++)
            str = fillWith + str;
        return str;
    }


}
