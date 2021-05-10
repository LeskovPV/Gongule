package local.gongule.utils.resources;


/**
 *
 */
public enum ResourceType {

    CFG,
    CSS,
    HTML,
    PNG,
    XML,
    UNKNOWN;

    // Resolve resourceName string as
    // "js/jquery/flot/jquery.flot.js" or "png/logo.png"
    // to JS or PNG value
    static public ResourceType getByName(String resourceName) {
        if (resourceName == null)
            return UNKNOWN;
        int index = resourceName.indexOf("/");
        if (index < 1)
            return UNKNOWN;
        for (ResourceType type : values())
            if (type.toString().equalsIgnoreCase(resourceName.substring(0,index)))
                return type;
        return UNKNOWN;
    }
}
