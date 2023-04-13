package com.xu.environmentInit.enumClass;

import org.apache.commons.lang3.StringUtils;

public enum SourceType {
    //region Values
    JAR("JAR File", "jar", ".jar", "To signal a Jar File to be scanned."),
    APK("APK File", "apk", ".apk", "To signal a APK File to be scanned."),
    WAR("WAR File", "war",".war","To signal a War File to be scanned."),
    DIR(
            "Directory of Source Code",
            "source",
            "dir",
            "To signal the source directory of a Maven/Gradle Project."),
    JAVAFILES("Java File or Files", "java", ".java", "To signal a Java File(s) to be scanned."),
    CLASSFILES("Class File or Files", "class", ".class", "To signal a Class File(s) to be scanned.");
    //endregion

    //region Attributes
    private String name;
    private String flag;
    private String inputExtension;

    private String helpInfo;
    //endregion

    //region Constructor

    /**
     * SourceType类型的基本构造函数
     *
     * @param name - 源文件类型名
     * @param flag - 源文件类型flag
     */
    SourceType(String name, String flag, String extension, String helpInfo) {
        this.name = name;
        this.flag = flag;
        this.inputExtension = extension;
        this.helpInfo = helpInfo;
    }
    //endregion

    //region Getters

    /**
     * 根据flag对源文件类型相关信息进行检索的方法
     *
     * @param flag - the flag used to look for the specified engine type
     * @return - either null if no flag matched or the engine type
     */
    public static SourceType getFromFlag(String flag) {
        for (SourceType type : SourceType.values())
            if (type.flag.equalsIgnoreCase(flag)) {
                return type;
            }
        return null;
    }

    /**
     * The method to automatically retrieve all of the help info for all of the different use cases.
     *
     * @return {@link String} - The full help info for console use
     */
    public static String getHelp() {
        StringBuilder out = new StringBuilder();

        for (SourceType type : SourceType.values())
            out.append(type.getFlag()).append(" : ").append(type.getHelpInfo()).append("\n");

        return out.toString();
    }

    /**
     * retrieveEngineTypeValues.
     *
     * @return a {@link String} object.
     */
    public static String retrieveEngineTypeValues() {
        StringBuilder out = new StringBuilder("[");

        for (SourceType type : SourceType.values())
            if (type == SourceType.JAVAFILES) out.append(type.getFlag()).append("(experimental) ");
            else out.append(type.getFlag()).append(" ");

        return StringUtils.trimToNull(out.toString()) + "]";
    }

    /**
     * The getter for the flag
     *
     * @return string - the flag of the engine type
     */
    public String getFlag() {
        return this.flag;
    }

    /**
     * The getter for the human readable name of the engine type
     *
     * @return string - the name of the engine type
     */
    public String getName() {
        return this.name;
    }

    /**
     * The getter for the extension
     *
     * @return {@link String} - The extension for the engine Type
     */
    public String getInputExtension() {
        return this.inputExtension;
    }

    /**
     * The getter for helpInfo
     *
     * <p>getHelpInfo()
     *
     * @return {@link String} - Returns the HelpInfo field
     */
    public String getHelpInfo() {
        return helpInfo;
    }

    //endregion
}


