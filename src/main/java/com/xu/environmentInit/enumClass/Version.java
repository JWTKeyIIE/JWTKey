package com.xu.environmentInit.enumClass;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public enum Version {
    /* Licensed under GPL-3.0 */

    //region Values
    ONE(1, 45),
    TWO(2, 46),
    THREE(3, 47),
    FOUR(4, 48),
    FIVE(5, 49),
    SIX(6, 50),
    SEVEN(7, 51),
    EIGHT(8, 52),
    NINE(9, 53),
    TEN(10, 54),
    ELEVEN(11, 55),
    TWELVE(12, 56),
    THIRTEEN(13, 57),
    FOURTEEN(14, 58),
    FIFTEEN(15, 59),
    SIXTEEN(16, 60),
    SEVENTEEN(17, 61);
    //endregion

    //region Attributes
    private int versionNumber;
    private int majorVersion;
    private static Version Supported = Version.EIGHT;
    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(Version.class);
    //endregion

    //region Constructor
    Version(int versionNumber, int majorVersion) {
        this.versionNumber = versionNumber;
        this.majorVersion = majorVersion;
    }
    //endregion

    //region Methods

    //region Overridden Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.valueOf(this.versionNumber);
    }
    //endregion

    /**
     * supported.
     *
     * @return a {@link Boolean} object.
     */
    public Boolean supported() {
        return this.majorVersion == Supported.majorVersion;
    }

    /**
     * retrieveByMajor.
     *
     * @param majorVersion a int.
     * @return a {@link Version} object.
     * @throws ExceptionHandler if any.
     */
    public static Version retrieveByMajor(int majorVersion) throws ExceptionHandler {
        return Arrays.stream(Version.values())
                .filter(v -> v.getMajorVersion() == majorVersion)
                .findFirst()
                .orElseThrow(
                        () ->
                                new ExceptionHandler(
                                        "Major Version: " + majorVersion + " not valid.", ExceptionId.FILE_AFK));
    }

    /**
     * getRunningVersion.
     *
     * @return a {@link Version} object.
     * @throws ExceptionHandler if any.
     */
    public static Version getRunningVersion() throws ExceptionHandler {
        String version = System.getProperty("java.version");

        log.debug("Java Version being used:" + version);

        //Used for Java JRE versions below 9
        if (version.startsWith("1.")) version = version.replaceFirst("1.", "");

        //Getting the major number
        int versionNumber = Integer.parseInt(version.substring(0, version.indexOf(".", 0)));

        return Arrays.stream(Version.values())
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst()
                .orElse(Version.ONE);
    }

    /**
     * supportedByMajor.
     *
     * @param majorVersion a int.
     * @return a boolean.
     * @throws ExceptionHandler if any.
     */
    public static boolean supportedByMajor(int majorVersion) throws ExceptionHandler {
        return retrieveByMajor(majorVersion).getVersionNumber() <= Supported.majorVersion;
    }

    /**
     * supportedFile.
     *
     * @return a boolean.
     */
    public boolean supportedFile() {
        return this.getVersionNumber() <= Supported.majorVersion;
    }

    /**
     * Getter for the field <code>versionNumber</code>.
     *
     * @return a int.
     */
    public int getVersionNumber() {
        return this.versionNumber;
    }

    /**
     * Getter for the field <code>majorVersion</code>.
     *
     * @return a int.
     */
    public int getMajorVersion() {
        return this.majorVersion;
    }
    //endregion
}


