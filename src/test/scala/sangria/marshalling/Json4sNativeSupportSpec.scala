package sangria.marshalling

import org.scalatest.{Matchers, WordSpec}

import sangria.marshalling.json4s.native._
import sangria.marshalling.testkit._

import org.json4s.JsonAST._

class Json4sNativeSupportSpec extends WordSpec with Matchers with MarshallingBehaviour with InputHandlingBehaviour {
  "Json4s native integration" should {
    behave like `value (un)marshaller` (Json4sNativeResultMarshaller)

    behave like `AST-based input unmarshaller` (json4sNativeFromInput[JValue])
    behave like `AST-based input marshaller` (Json4sNativeResultMarshaller)
  }

  val toRender = JObject(
    "a" → JArray(List(JNull, JInt(123), JArray(List(JObject("foo" → JString("bar")))))),
    "b" → JObject(
      "c" → JBool(true),
      "d" → JNull))

  "InputUnmarshaller" should {
    "throw an exception on invalid scalar values" in {
      an [IllegalStateException] should be thrownBy
          Json4sNativeInputUnmarshaller.getScalarValue(JObject())
    }

    "throw an exception on variable names" in {
      an [IllegalArgumentException] should be thrownBy
          Json4sNativeInputUnmarshaller.getVariableName(JString("$foo"))
    }

    "render JSON values" in {
      val rendered = Json4sNativeInputUnmarshaller.render(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }

  "ResultMarshaller" should {
    "render pretty JSON values" in {
      val rendered = Json4sNativeResultMarshaller.renderPretty(toRender)

      rendered.replaceAll("\r", "") should be (
        """{
          |  "a":[null,123,[{
          |    "foo":"bar"
          |  }]],
          |  "b":{
          |    "c":true,
          |    "d":null
          |  }
          |}""".stripMargin.replaceAll("\r", ""))
    }

    "render compact JSON values" in {
      val rendered = Json4sNativeResultMarshaller.renderCompact(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }
}
