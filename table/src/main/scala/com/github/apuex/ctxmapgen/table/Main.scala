package com.github.apuex.ctxmapgen.table

object Main extends App {
  if (args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "generate-context-mapping" => TableMappingGenerator(args.drop(1)(0)).generate()
      case c =>
        println(s"unknown command '${c}'")
    }
  }
}
