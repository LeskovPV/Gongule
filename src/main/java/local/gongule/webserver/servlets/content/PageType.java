package local.gongule.webserver.servlets.content;

public enum PageType {

    CONTROL   ("Process control"),
    COURSES   ("Courses list"   ),
    DAYS      ("Days list"      ),
    SETUP     ("System setup"   );

    private String title;

    PageType(String title) {
        this.title = title;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public String getTitle() {
        return title;
    }

    public String getCaption() {
        String caption = getName();
        return Character.toUpperCase(caption.charAt(0)) + caption.substring(1);
    }

    public static PageType getDefault() {
        return CONTROL;
    }

    public static boolean contains(String name) {
        try {
            PageType.valueOf(name.toUpperCase());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static PageType getValueOf(String name) {
        try {
            return PageType.valueOf(name.toUpperCase());
        } catch (Exception exception) {
            return PageType.getDefault();
        }
    }

}
