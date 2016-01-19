package io.cyberdolphins.recommendations.commons

/**
  * Created by mikwie on 19/01/16.
  */
import scala.util.parsing.combinator._

class CsvParser extends RegexParsers with Serializable {
  override val skipWhitespace = false   // meaningful spaces in CSV

  def COMMA   = ","
  def DQUOTE  = "\""
  def DQUOTE2 = "\"\"" ^^ { case _ => "\"" }  // combine 2 dquotes into 1
  def CRLF    = "\r\n" | "\n"
  def TXT     = "[^\",\r\n]".r
  def SPACES  = "[ \t]+".r

  def file: Parser[List[List[String]]] = repsep(record, CRLF) <~ (CRLF?)

  def record: Parser[List[String]] = repsep(field, COMMA)

  def field: Parser[String] = escaped|nonescaped

  def escaped: Parser[String] = {
    ((SPACES?)~>DQUOTE~>((TXT|COMMA|CRLF|DQUOTE2)*)<~DQUOTE<~(SPACES?)) ^^ {
      case ls => ls.mkString("")
    }
  }

  def nonescaped: Parser[String] = (TXT*) ^^ { case ls => ls.mkString("") }

  def parse(s: String): Seq[String] = parseAll(record, s) match {
    case Success(res, _) => res
    case e => Nil
  }
}

object CsvParser extends CsvParser
