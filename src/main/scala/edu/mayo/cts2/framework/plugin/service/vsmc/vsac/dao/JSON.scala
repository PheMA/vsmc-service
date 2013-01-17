package edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao

import net.minidev.json.JSONValue
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject

object JSON {
  def parseJSON(s: String) = new ScalaJSON(JSONValue.parse(s))

  implicit def ScalaJSONToString(s: ScalaJSON) = s.toString

  implicit def ScalaJSONToInt(s: ScalaJSON) = s.toInt

  implicit def ScalaJSONToDouble(s: ScalaJSON) = s.toDouble
}

case class JSONException extends Exception

class ScalaJSONIterator(i: java.util.Iterator[java.lang.Object]) extends Iterator[ScalaJSON] {
  def hasNext = i.hasNext()

  def next() = new ScalaJSON(i.next())
}

class ScalaJSON(o: java.lang.Object) extends Seq[ScalaJSON] with Dynamic {
  override def toString: String = if (o == null) null else o.toString

  override def hashCode: Int = if (o == null) 0 else o.hashCode

  override def equals(any: Any): Boolean = if (o == null) false else o.equals(any)

  def toInt: Int = o match {
    case i: Integer => i
    case _ => throw new JSONException
  }

  def toDouble: Double = o match {
    case d: java.lang.Double => d
    case f: java.lang.Float => f.toDouble
    case _ => throw new JSONException
  }

  def apply(key: String): ScalaJSON = o match {
    case m: JSONObject => new ScalaJSON(m.get(key))
    case _ => throw new JSONException
  }

  def apply(idx: Int): ScalaJSON = o match {
    case a: JSONArray => new ScalaJSON(a.get(idx))
    case _ => throw new JSONException
  }

  def length: Int = o match {
    case a: JSONArray => a.size()
    case m: JSONObject => m.size()
    case _ => throw new JSONException
  }

  def iterator: Iterator[ScalaJSON] = o match {
    case a: JSONArray => new ScalaJSONIterator(a.iterator())
    case _ => throw new JSONException
  }

  def selectDynamic(name: String): ScalaJSON = apply(name)

  def applyDynamic(name: String)(arg: Any) = {
    arg match {
      case s: String => apply(name)(s)
      case n: Int => apply(name)(n)
      case u: Unit => apply(name)
    }
  }
}