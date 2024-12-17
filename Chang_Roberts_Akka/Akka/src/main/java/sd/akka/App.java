package sd.akka; 

import java.time.Duration;
import java.util.concurrent.CompletionStage; 
import java.util.concurrent.ExecutionException; 
import java.util.concurrent.ThreadLocalRandom;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;

import sd.akka.Actor.CR_actor;

public class App {
    public static void main(String[] args){
        ActorSystem actorSystem = ActorSystem.create(); 

        int numActor = 12; 

        int[] alrChoose = new int[numActor]; 
        ActorRef[] actor = new ActorRef[numActor]; 

        for(int i = 0; i < numActor; i++){
            boolean choose; 
            int randn; 

            do {
                randn = ThreadLocalRandom.current().nextInt(numActor); 
				choose = true; 
                System.out.print("We choose " + randn + ". "); 

                
                for(int j = 0; j < i; j++){
                    if(randn == alrChoose[j]){
                        choose = false; 
                        System.out.println("Already choose, we re-select ! "); 
                        break; 
                    }
                }
            } while(!choose); 

            alrChoose[i] = randn; 
            System.out.println("We put : " + randn); 

            actor[i] = actorSystem.actorOf(CR_actor.props(i + 1), String.valueOf(randn));

        }

        for(int i = 0; i < numActor; i++){
            System.out.println("Actor " + (i + 1) + " : " + actor[i].path().name()); 
        }

        actor[0].tell("I am the actor 1", ActorRef.noSender()); 

        int firstIndex = ThreadLocalRandom.current().nextInt(numActor); 

        for(int i = 0; i < numActor; i++){
            ActorRef currentActor = actor[i]; 
            int idNeighbor = (i + 1) % numActor; 
            ActorRef neighbor = actor[idNeighbor]; 
            currentActor.tell(new CR_actor.CreateRing(neighbor), ActorRef.noSender()); 
        }

        actor[firstIndex].tell(new CR_actor.StartElection(), ActorRef.noSender());

        try {
            Thread.sleep(3000); 
        } catch(Exception e) {
            e.printStackTrace(); 
        }

        actorSystem.terminate(); 
    }
}
