package services.session

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ddata.DistributedData
import akka.cluster.ddata.LWWMap
import akka.cluster.ddata.LWWMapKey

/**
 * A distributed key-store map using akka distributed data.
 *
 * This is from one of the examples covered in the akka distributed data section.
 *
 * http://doc.akka.io/docs/akka/current/scala/distributed-data.html
 *
 *
 */
class ReplicatedCache extends Actor {
  // https://github.com/akka/akka-samples/blob/master/akka-sample-distributed-data-scala/src/main/scala/sample/distributeddata/ReplicatedCache.scala
  import akka.cluster.ddata.Replicator._
  import ReplicatedCache._

  private[this] val replicator = DistributedData(context.system).replicator
  private[this] implicit val cluster = Cluster(context.system)

  def dataKey(entryKey: String): LWWMapKey[String, Any] =
    LWWMapKey("cache-" + math.abs(entryKey.hashCode) % 100)

  def receive = {
    case PutInCache(key, value) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteLocal)(_ + (key -> value))
    case Evict(key) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteLocal)(_ - key)
    case GetFromCache(key) =>
      replicator ! Get(dataKey(key), ReadLocal, Some(Request(key, sender())))
    case g @ GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[_, _] => data.asInstanceOf[LWWMap[String, Any]].get(key) match {
          case Some(value) => replyTo ! Cached(key, Some(value))
          case None        => replyTo ! Cached(key, None)
        }
      }
    case NotFound(_, Some(Request(key, replyTo))) =>
      replyTo ! Cached(key, None)
    case _: UpdateResponse[_] => // ok
  }

}

object ReplicatedCache {
  def props: Props = Props[ReplicatedCache]

  private final case class Request(key: String, replyTo: ActorRef)

  final case class PutInCache(key: String, value: Any)
  final case class GetFromCache(key: String)
  final case class Cached(key: String, value: Option[Any])
  final case class Evict(key: String)
}
