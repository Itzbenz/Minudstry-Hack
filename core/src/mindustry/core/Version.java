package mindustry.core;

public class Version{
    /** Build type. 'official' for official releases; 'custom' or 'bleeding edge' are also used. */
    public static final String type = "official";
    /** Build modifier, e.g. 'alpha' or 'release' */
    public static final String modifier = "release";
    /** Number specifying the major version, e.g. '4' */
    public static final int number = 5;
    /** Build number, e.g. '43'. set to '-1' for custom builds. */
    public static final int build = 104;
    /** Revision number. Used for hotfixes. Does not affect server compatibility. */
    public static final int revision = 10;
    /** Whether version loading is enabled. */
    public static boolean enabled = true;

    public static void init(){

    }
}
