package sd.akka.Actor; 

import akka.actor.AbstractActor; 
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.japi.pf.ReceiveBuilder;

import java.time.Duration; 
import java.util.concurrent.CompletionStage;

public class CR_actor extends AbstractActor {
	private ActorRef nextActor; 

	private final int id; 
	private boolean aNeighbor =false; 
	private boolean electionParticipant = false; 
	private int coordinator = -1; 

	private boolean StartedElection = false; 
	private boolean AllowSend = false; 

	private CR_actor (int idActeur) {
		this.id = idActeur; 
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(CreateRing.class, this::createRing)
			.match(StartElection.class, this::startElection)
			.match(ElectionMsg.class, this::msgElec)
			.match(ElectedMsg.class, this::msgElected)
			.build();
	}

	public void createRing(CreateRing msg) {
		if(!aNeighbor){
			this.nextActor = msg.neighbor; 
			aNeighbor = true; 
		}
	}

	public void startElection(StartElection msg) {
		if(!StartedElection && !AllowSend) {
			System.out.println(""); 
			System.out.println("Election is starting !");
			System.out.println("Actor n° " + getSelf().path().name() + " : " + IdRetrieve()); 
			System.out.println(""); 
			PrintRing();
			nextActor.tell(new ElectionMsg((id)), getSelf()); 
			StartedElection = true; 
			AllowSend = true; 
		}
	}


	public void msgElec(ElectionMsg msg) {
		if (msg.IDcandidat > id) {
			nextActor.tell(new ElectionMsg(msg.IDcandidat), getSelf());
			electionParticipant = true;
			System.out.println("Actor n°" + getSelf().path().name() + " (" + id  + ")"
							   + " Received " + msg.IDcandidat
							   + " which is higher. He forwards " + msg.IDcandidat);
		} else if (msg.IDcandidat < id && !electionParticipant) {
			nextActor.tell(new ElectionMsg(id), getSelf());
			electionParticipant = true;
			System.out.println("Actor n°" + getSelf().path().name() + " (" + id  + ")"
							   + " Received " + msg.IDcandidat
							   + " which is lower. He sends his own ID: " + id);
		} else if (msg.IDcandidat == id) {
			nextActor.tell(new ElectedMsg(id), getSelf());
			electionParticipant = true;
			System.out.println("Actor n°" + getSelf().path().name() + " (" + id  + ")"
							   + " Has received : " + msg.IDcandidat
							   + ", which is his own ID. " + getSelf().path().name() + " is elected.");
			AllowSend = false;
		}
	}
	
	public void msgElected(ElectedMsg msg) {
		coordinator = msg.IDWinner;
		electionParticipant = false;
	
		if (msg.IDWinner != id) {
			nextActor.tell(new ElectedMsg(msg.IDWinner), getSelf());
			System.out.println("Actor n°" + getSelf().path().name() + " (" + id  + ")"
							   + " Has received the elected message for actor " 
							   + msg.IDWinner + " and forwards it.");
			getContext().stop(getSelf());
		} else {
			System.out.println("Actor n°" + getSelf().path().name() + " (" + msg.IDWinner + ")"
							   + ", is elected. Therefore, elections are over.");
			getContext().stop(getSelf());
		}
	}
	



	public static Props props(int idActeur){
		return Props.create(CR_actor.class, () -> new CR_actor(idActeur)); 
	}

	private int IdRetrieve() {
		return id; 
	}

	private void PrintRing() {
		System.out.println("Actor n°" + getSelf().path().name() + " (" + id  + "), has as neighbor " 
							+ (nextActor != null ? nextActor.path().name() : " no process. ")); 
	}

	// Définition des messages en inner classes
	public interface Message {}

	public static class CreateRing {
		final ActorRef neighbor; 

		public CreateRing(ActorRef neighbor) {
			this.neighbor = neighbor; 
		}
	}

	public static class StartElection{}

	public static class ElectionMsg {
		public final int IDcandidat; 

		public ElectionMsg(int IDcandidat){
			this.IDcandidat = IDcandidat; 
		}
	}
	
	public static class ElectedMsg {
		public final int IDWinner; 

		public ElectedMsg(int IDWinner){
			this.IDWinner = IDWinner; 
		}
	}

}
