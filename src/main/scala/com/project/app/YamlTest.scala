import io.circe.yaml.parser
import io.circe.generic.auto._

case class Foo(x: Int)

object YamlTest extends App {
  val yaml = "x: 42"
  val result = parser.parse(yaml).flatMap(_.as[Foo]).toOption
  println(result)
}