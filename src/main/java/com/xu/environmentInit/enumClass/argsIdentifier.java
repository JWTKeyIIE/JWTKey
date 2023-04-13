/* Licensed under GPL-3.0 */
package com.xu.environmentInit.enumClass;

/*import frontEnd.MessagingSystem.routing.Listing;
import rule.engine.EngineType;*/

import java.util.ArrayList;
import java.util.List;

/**
 * argsIdentifier class." -o " + tempFileOutApk_Scarf
 * 定义所有命令行参数
 *
 * @author CryptoguardTeam Created on 2/5/19.
 * @version 03.07.01
 * @since 03.00.00
 *     <p>The central point for identifying the arguments and their description.
 */
public enum argsIdentifier {

  //region Values
//  -in参数：标识输入的被分析文件的类型
  FORMAT(
      "in",
      "format",
      "Required: The format of input you want to scan, available styles "
          + SourceType.retrieveEngineTypeValues()
          + ".",
      "format",
      null,
      true),
//  -s参数：标识被分析文件的路径
  SOURCE(
      "s",
      "file/files/*.in/dir/ClassPathString/xargs",
      "Required: The source to be scanned use the absolute path or send all of the source files via the file input.in; ex. find -type f *.java >> input.in.",
      "file(s)/*.in/dir",
      null,
      true),
//  -d参数：标识要被分析的依赖项的相对路径
  DEPENDENCY("d", "dir", "The dependency to be scanned use the relative path.", "dir", null, false),
//  -o参数：标识输出的文件名，如果不设置则默认为项目名
  OUT(
      "o",
      "file",
      "The file to be created with the output default will be the project name.",
      "file",
      null,
      false),
//  -new参数：标识如果要创建的文件已经存在，则覆盖源文件
  NEW(
      "new",
      null,
      "The file to be created with the output if existing will be overwritten.",
      null,
      null,
      false),
  //-t参数：输出内部处理花费的时间
  TIMEMEASURE("t", null, "Output the time of the internal processes.", null, null, false),
//  -m参数：指定输出内容的格式；
  FORMATOUT(
      "m",
      "formatType",
//      "The output format you want to produce, " + Listing.getShortHelp() + ".",
        "The output format you want to produce, ",
        "formatType",
      null,
      true),
  PRETTY("n", null, "Output the analysis information in a 'pretty' format.", null, null, false),
  NOEXIT("X", null, "Upon completion of scanning, don't kill the JVM", null, null, false),
  //EXPERIMENTRESULTS("exp", null, "View the experiment based results.", null, null, false),
  VERSION("v", null, "Output the version number.", null, null, false),
  NOLOGS("VX", null, "Display logs only from the fatal logs", null, null, false),
  VERBOSE("V", null, "Display logs from debug levels", null, null, false),
  VERYVERBOSE("VV", null, "Display logs from trace levels", null, null, false),
  TIMESTAMP("ts", null, "Add a timestamp to the file output.", null, null, false),
  DEPTH("depth", null, "The depth of slicing to go into", "depth", null, false),
  //LOG("L", null, "Enable logging to the console.", null, null, false),
  JAVA(
      "java",
      "envVariable",
      "Directory of Java to be used JDK 7 for JavaFiles/Project and JDK 8 for ClassFiles/Jar",
      "java",
      null,
      false),
  ANDROID("android", "envVariable", "Specify of Android SDK", "android", null, false),
  HEURISTICS(
      "H", null, "The flag determining whether or not to display heuristics.", null, null, false),
  STREAM("st", null, "Stream the analysis to the output file.", null, null, false),
  HELP("h", null, "Print out the Help Information.", null, null, false),
  MAIN(
      "main",
      "className",
      "Choose the main class if there are multiple main classes in the files given.",
      "main",
      null,
      false),
  JWTLIB("J",
          null,
          "The flag determining which jwt lib that should be analysis",
          null,
          null,
          false);
  /*SCONFIG(
      "Sconfig",
      "file",
      "Choose the Scarf property configuration file.",
      "file.properties",
      Listing.ScarfXML,
      false),
  SASSESSFW(
      "Sassessfw", "variable", "The assessment framework", "variable", Listing.ScarfXML, false),
  SASSESSFWVERSION(
      "Sassessfwversion",
      "variable",
      "The assessment framework version",
      "variable",
      Listing.ScarfXML,
      false),
  SASSESSMENTSTARTTS(
      "Sassessmentstartts",
      "variable",
      "The assessment start timestamp",
      "variable",
      Listing.ScarfXML,
      false),
  SBUILDFW("Sbuildfw", "variable", "The build framework", "variable", Listing.ScarfXML, false),
  SBUILDFWVERSION(
      "Sbuildfwversion",
      "variable",
      "The build framework version",
      "variable",
      Listing.ScarfXML,
      false),
  SBUILDROOTDIR(
      "Sbuildrootdir", "dir", "The build root directory", "variable", Listing.ScarfXML, false),
  SPACKAGENAME("Spackagename", "variable", "The package name", "variable", Listing.ScarfXML, false),
  SPACKAGEROOTDIR(
      "Spackagerootdir", "dir", "The package root directory", "variable", Listing.ScarfXML, false),
  SPACKAGEVERSION(
      "Spackageversion", "variable", "The package version", "variable", Listing.ScarfXML, false),
  SPARSERFW("Sparserfw", "variable", "The parser framework", "variable", Listing.ScarfXML, false),
  SPARSERFWVERSION(
      "Sparserfwversion",
      "variable",
      "The parser framework version",
      "variable",
      Listing.ScarfXML,
      false),
  SUUID(
      "Suuid",
      "uuid",
      "The uuid of the current pipeline progress",
      "variable",
      Listing.ScarfXML,
      false);*/
  //endregion

  private String id;
  private String defaultArg;
  private String desc;
  private Listing formatType;
  private String argName;
  private Boolean required;

  argsIdentifier(
      String id, String defaultArg, String desc, String argName, Listing format, Boolean required) {
    this.id = id;
    this.defaultArg = defaultArg;
    this.desc = desc;
    this.argName = argName;
    this.formatType = format;
    this.required = required;
  }

  /**
   * lookup.
   *
   * @param id a {@link String} object.
   * @return a {@link argsIdentifier} object.
   */
  public static argsIdentifier lookup(String id) {
    for (argsIdentifier in : argsIdentifier.values()) if (in.getId().equals(id)) return in;
    return null;
  }

  /**
   * lookup.
   *
   * @param type a {@link Listing} object.
   * @return a {@link List} object.
   */
  public static List<argsIdentifier> lookup(Listing type) {
    List<argsIdentifier> args = new ArrayList<>();

    for (argsIdentifier in : argsIdentifier.values())
      if (in.formatType != null && in.formatType.equals(type)) args.add(in);

    return args;
  }

  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link String} object.
   */
  public String getId() {
    return this.id;
  }

  /**
   * getArg.
   *
   * @return a {@link String} object.
   */
  public String getArg() {
    return "-" + this.id;
  }

  /**
   * Getter for the field <code>desc</code>.
   *
   * @return a {@link String} object.
   */
  public String getDesc() {
    return this.name() + ": " + this.desc;
  }

  /**
   * hasDefaultArg.
   *
   * @return a {@link Boolean} object.
   */
  public Boolean hasDefaultArg() {
    return defaultArg != null;
  }

  /**
   * Getter for the field <code>defaultArg</code>.
   *
   * @return a {@link String} object.
   */
  public String getDefaultArg() {
    return defaultArg;
  }

  /**
   * Getter for the field <code>formatType</code>.
   *
   * @return a {@link Listing} object.
   */
  public Listing getFormatType() {
    return this.formatType;
  }

  /**
   * Getter for the field <code>argName</code>.
   *
   * @return a {@link String} object.
   */
  public String getArgName() {
    return this.argName;
  }

  /**
   * Getter for the field <code>required</code>.
   *
   * @return a {@link Boolean} object.
   */
  public Boolean getRequired() {
    return this.required;
  }
}
