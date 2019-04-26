package com.github.apuex.ctxmapgen.util

import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter

case class ClasspathXmlLoader(fileName: String) {
  val factory = new NoBindingFactoryAdapter
  val xml: Node = factory.load(getClass.getClassLoader.getResourceAsStream(fileName))
}
