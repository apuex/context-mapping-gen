package com.github.apuex.ctxmapgen.table

object Main extends App {
  if (args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "generate-table-mapping" => TableMappingGenerator(args.drop(1)(0)).generate()
      case "generate-service-client" => ServiceClientGenerator(args.drop(1)(0)).generate()
      case "generate-project-settings" => ProjectGenerator(args.drop(1)(0)).generate()
      case "generate-json-serializer" => JsonSerializerGenerator(args.drop(1)(0)).generate()
      case "generate-all" => generateAll(args.drop(1)(0))
      case c =>
        println(s"unknown command '${c}'")
    }
  }

  def generateAll(fileName: String): Unit = {
    TableMappingGenerator(fileName).generate()
    ServiceClientGenerator(fileName).generate()
    ProjectGenerator(fileName).generate()
    JsonSerializerGenerator(fileName).generate()
  }
}
