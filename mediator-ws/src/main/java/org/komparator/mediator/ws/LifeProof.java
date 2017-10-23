package org.komparator.mediator.ws;

import java.util.Date;
import java.util.TimerTask;

import org.komparator.mediator.ws.MediatorEndpointManager.State;

import java.text.SimpleDateFormat;
import java.text.ParseException;


public class LifeProof extends TimerTask{

    private String primaryWsURL;

    private String secondaryWsURL;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private MediatorEndpointManager endpoint;

    private boolean mainIsDead = false;

    private final int TIMEOUT = 5;


    public LifeProof(MediatorEndpointManager endpoint) {
        this.endpoint = endpoint;
    }

    public void run(){
        if (endpoint.getState() == State.MAIN){
            try{
                MediatorPortType client = endpoint.getSecondPort();
                client.imAlive();
                System.out.println("Secondary server, I am alive!");
            } catch (Exception e){
                System.out.println("Secondary server is offline");
            }
        }
        else{
            if (!mainIsDead){
                try {
                    if(endpoint.getAlive() != null){
                        Date lastStamp = dateFormatter.parse(dateFormatter.format(endpoint.getAlive()));
                        Date now = dateFormatter.parse(dateFormatter.format(new Date()));
                        long seconds = (now.getTime()-lastStamp.getTime())/1000;
                        if (seconds >= TIMEOUT){
                            endpoint.publishToUDDI();
                            mainIsDead = true;
                        }
                    }
                }catch(ParseException e){
                    System.out.println("Problem with timestamp");
                } catch(Exception e){
                    System.out.println("Solve me");
                }
            }
        }
    }


}
