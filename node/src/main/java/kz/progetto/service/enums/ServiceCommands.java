package kz.progetto.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION(".help"),
    CANCEL("/cancel"),
    START("/start");
    private final String value;


    ServiceCommands(String value){
        this.value =value;
    }

    @Override
    public String toString(){
        return value;
    }

    public static ServiceCommands fromValue(String v) {
        for (ServiceCommands c : ServiceCommands.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }

}
