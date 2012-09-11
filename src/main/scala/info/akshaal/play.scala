/** Evgeny Chukreev, 2012. http://akshaal.info */

package info.akshaal.json

import language.experimental.macros
import _root_.play.api.libs.json._
import info.akshaal.clazz
import info.akshaal.clazz._

object play {
    // Writes

    type JsFields[T <: AnyRef] = Fields[T, JsValue, Product]

    class RichFields[T <: AnyRef](val fields: JsFields[T]) {
        def toWrites: Writes[T] = new Writes[T] {
            def writes(t: T): JsValue =
                JsObject(fields map {
                    (field: Field[T, JsValue, _])   =>   field.name -> field.get(t: T)
                })
        }

        def extra[V : Writes](pair: (Symbol, V)) : RichFields[T] =
            Field(pair._1.name, (_: Any) => Json.toJson(pair._2), None) :: fields
    }

    def matchingWrites[T] (f : T => JsValue) : Writes[T] = new Writes[T] {
        def writes(t: T): JsValue = f(t)
    }

    implicit def enrich [T <: AnyRef](fields: Fields[T, JsValue, Product]) : RichFields[T] = new RichFields(fields)
    implicit def toWrites [T <: AnyRef](implicit fields: JsFields[T]) : Writes[T] = fields.toWrites

    def jsonate[T: Writes](t: T, args: Any): JsValue = Json.toJson(t)

    implicit val symbolWrites = new Writes[Symbol] { def writes(t : Symbol) : JsValue = Json.toJson(t.name)}

    // Reads

    class RichFactory[T <: AnyRef](val factory: Factory[JsValue, T]) {
        def toReads: Reads[T] = new Reads[T] {
            def reads(json : JsValue) = json match {
                case obj : JsObject => factory(sym => obj.value.get(sym.name))
                case JsUndefined(_) => factory(sym => None)
                case _ => throw new IllegalArgumentException("Illegal json value: " + json)
            }
        }
    }

    def matchingReads[T] (f : JsValue => T) : Reads[T] = new Reads[T] {
        def reads(js: JsValue): T = f(js)
    }

    def predicatedReads[T <: AnyRef] (cases : (JsValue => Boolean, Factory[JsValue, T])*) = new Reads[T] {
        def reads(js : JsValue) : T = (cases find (_._1(js)) map (_._2) map(toReads(_).reads(js))).get
    }

    implicit def enrich [T <: AnyRef](factory: Factory[JsValue, T]) : RichFactory[T] = new RichFactory(factory)
    implicit def toReads [T <: AnyRef](implicit factory: Factory[JsValue, T]) : Reads[T] = factory.toReads

    def fromJson[T](valueOpt: Option[JsValue], sym: Symbol)(implicit reads : Reads[T]): Option[T] =
        valueOpt map (Json.fromJson(_)(reads))

    def jsHas(sym: Symbol) = (_ : JsValue) match {
        case js : JsObject => js.value contains sym.name
        case _ => false
    }

    def jsHas(pair: (Symbol, Symbol)) = (_ : JsValue) match {
        case js : JsObject => js.value get pair._1.name map (_ == JsString(pair._2.name)) getOrElse false
        case js => false
    }

    // Macros
    def allFields[I <: AnyRef](apply: Symbol) = macro clazz.fieldsImpl[Any, I, JsValue, None.type]
    def annotatedFields[I <: AnyRef, A](apply: Symbol) = macro clazz.fieldsImpl[A, I, JsValue, None.type]
    def factory[I <: AnyRef](apply: Symbol) = macro clazz.factoryImpl[JsValue, I]
}
