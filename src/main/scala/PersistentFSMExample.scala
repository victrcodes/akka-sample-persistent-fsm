import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect._
import scala.util.Random

final case class SetNumber(num: Integer)
final case class Reset()

sealed trait State extends FSMState
case object Idle extends State {
	override def identifier: String = "Idle"
}
case object Active extends State {
	override def identifier: String = "Active"
}

sealed trait Data {
	def add(number: Integer): Data
	def empty(): Data
}
case object Empty extends Data {
	def add(number: Integer) = Numbers(Vector(number))
	def empty() = this
}
final case class Numbers(queue: Seq[Integer]) extends Data {
	def add(number: Integer) = Numbers(queue :+ number)
	def empty() = Empty
}

sealed trait DomainEvt
case class SetNumberEvt(num: Integer) extends DomainEvt
case class ResetEvt() extends DomainEvt

class Generator extends PersistentFSM[State, Data, DomainEvt] {

	override def applyEvent(domainEvent: DomainEvt, currentData: Data): Data = {
		domainEvent match {
			case SetNumberEvt(num) =>
				val data = currentData.add(num)
				println(data)
				data
			case ResetEvt() => currentData.empty()
		}
	}

	override def persistenceId: String = "generator"

	override def domainEventClassTag: ClassTag[DomainEvt] = classTag[DomainEvt]

	startWith(Idle, Empty)

	when(Idle) {
		case Event(SetNumber(num), _) =>
			println("Starting idle")
			goto(Active) applying SetNumberEvt(num)
		case Event(Reset, _) =>
			println("Reset")
			deleteMessages(1000)
			goto(Active) applying ResetEvt()
	}

	when(Active) {
		case Event(SetNumber(num), numbers: Data) =>
			//println(numbers)
			stay applying SetNumberEvt(num)
		case Event(Reset, _) =>
			println("Reset")
			deleteMessages(1000)
			stay applying ResetEvt() replying "reset done"
	}

	initialize()

}

object PersistentFSMExample extends App {

	val system = ActorSystem()

	val actor = system.actorOf(Props[Generator])

 	implicit val timeout = Timeout(5000 millis)

	val reset: Future[_] = if (args.length > 0 && args(0) == "reset") actor ? Reset else Future("continue")

	reset.onComplete { _ =>
		actor ! SetNumber(Random.nextInt())
		actor ! SetNumber(Random.nextInt())
		actor ! SetNumber(Random.nextInt())
		actor ! SetNumber(Random.nextInt())
		actor ! SetNumber(Random.nextInt())
	}

	Thread.sleep(3000)
	system.terminate()

}