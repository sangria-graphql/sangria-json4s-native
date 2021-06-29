package sangria.marshalling.json4s

import org.json4s.native.JsonMethods.{render => jsonRender, pretty, compact}
import org.json4s.JsonAST._
import sangria.marshalling._

object native extends Json4sNativeSupportLowPrioImplicits {
  implicit object Json4sNativeResultMarshaller extends ResultMarshaller {
    type Node = JValue
    type MapBuilder = ArrayMapBuilder[Node]

    def emptyMapNode(keys: Seq[String]) = new ArrayMapBuilder[Node](keys)
    def addMapNodeElem(
        builder: MapBuilder,
        key: String,
        value: Node,
        optional: Boolean): ArrayMapBuilder[Node] =
      builder.add(key, value)

    def mapNode(builder: MapBuilder) = JObject(builder.toList)
    def mapNode(keyValues: Seq[(String, JValue)]) = JObject(keyValues.toList)

    def arrayNode(values: Vector[JValue]) = JArray(values.toList)
    def optionalArrayNodeValue(value: Option[JValue]): Node =
      value match {
        case Some(v) => v
        case None => nullNode
      }

    def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]): Node =
      value match {
        case v: String => JString(v)
        case v: Boolean => JBool(v)
        case v: Int => JInt(v)
        case v: Long => JLong(v)
        case v: Float => JDouble(v)
        case v: Double => JDouble(v)
        case v: BigInt => JInt(v)
        case v: BigDecimal => JDecimal(v)
        case v => throw new IllegalArgumentException("Unsupported scalar value: " + v)
      }

    def enumNode(value: String, typeName: String) = JString(value)

    def nullNode: Node = JNull

    def renderCompact(node: JValue): String = compact(jsonRender(node))
    def renderPretty(node: JValue): String = pretty(jsonRender(node))
  }

  implicit object Json4sNativeMarshallerForType extends ResultMarshallerForType[JValue] {
    val marshaller: Json4sNativeResultMarshaller.type = Json4sNativeResultMarshaller
  }

  implicit object Json4sNativeInputUnmarshaller extends InputUnmarshaller[JValue] {
    def getRootMapValue(node: JValue, key: String) =
      node.asInstanceOf[JObject].obj.find(_._1 == key).map(_._2)

    def isMapNode(node: JValue): Boolean = node.isInstanceOf[JObject]
    def getMapValue(node: JValue, key: String): Option[JValue] =
      node.asInstanceOf[JObject].obj.find(_._1 == key).map(_._2)
    def getMapKeys(node: JValue) = node.asInstanceOf[JObject].obj.map(_._1)

    def isListNode(node: JValue): Boolean = node.isInstanceOf[JArray]
    def getListValue(node: JValue): List[JValue] = node.asInstanceOf[JArray].arr

    def isDefined(node: JValue): Boolean = node != JNull && node != JNothing
    def getScalarValue(node: JValue): Any =
      node match {
        case JBool(b) => b
        case JInt(i) => i
        case JDouble(d) => d
        case JLong(l) => l
        case JDecimal(d) => d
        case JString(s) => s
        case _ => throw new IllegalStateException(s"$node is not a scalar value")
      }

    def getScalaScalarValue(node: JValue): Any = getScalarValue(node)

    def isEnumNode(node: JValue): Boolean = node.isInstanceOf[JString]

    def isScalarNode(node: JValue): Boolean =
      node match {
        case _: JBool | _: JDouble | _: JDecimal | _: JLong | _: JInt | _: JString => true
        case _ => false
      }

    def isVariableNode(node: JValue): Boolean = false
    def getVariableName(node: JValue) =
      throw new IllegalArgumentException("variables are not supported")

    def render(node: JValue): String = compact(jsonRender(node))
  }

  private object Json4sNativeToInput extends ToInput[JValue, JValue] {
    def toInput(value: JValue): (JValue, Json4sNativeInputUnmarshaller.type) =
      (value, Json4sNativeInputUnmarshaller)
  }

  implicit def json4sNativeToInput[T <: JValue]: ToInput[T, JValue] =
    Json4sNativeToInput.asInstanceOf[ToInput[T, JValue]]

  private object Json4sNativeFromInput extends FromInput[JValue] {
    val marshaller: Json4sNativeResultMarshaller.type = Json4sNativeResultMarshaller
    def fromResult(node: marshaller.Node): JValue = node
  }

  implicit def json4sNativeFromInput[T <: JValue]: FromInput[T] =
    Json4sNativeFromInput.asInstanceOf[FromInput[T]]
}

trait Json4sNativeSupportLowPrioImplicits {
  implicit val Json4sNativeInputUnmarshallerJObject: InputUnmarshaller[JObject] =
    native.Json4sNativeInputUnmarshaller.asInstanceOf[InputUnmarshaller[JObject]]
}
