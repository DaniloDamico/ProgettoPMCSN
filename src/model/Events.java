package model;

public class Events {
    // TICKET
    public static int ARRIVAL_TICKET = 2;       //the second one is for validation feedbacks
    public static int SERVERS_TICKET = 7;       //DEPARTURE_TICKET events
    public static int END_OF_TICKET = ARRIVAL_TICKET + SERVERS_TICKET;

    // SEARCH
    public static int ARRIVAL_SEARCH = 1;
    public static int SERVERS_SEARCH = 16;       //DEPARTURE_SEARCH events
    public static int END_OF_SEARCH = END_OF_TICKET + ARRIVAL_SEARCH + SERVERS_SEARCH;

    // VALIDATION
    public static int ARRIVAL_VALIDATION = 2; // vip arrivals and normal arrivals
    public static int SERVERS_VALIDATION = 10;   //DEPARTURE_VALIDATION events
    public static int END_OF_VALIDATION = END_OF_SEARCH + ARRIVAL_VALIDATION + SERVERS_VALIDATION /*+ SERVERS_VIP_VALIDATION*/;

    // BACKSTAGE
    public static int ARRIVAL_BACKSTAGE = 1;
    public static int SERVERS_BACKSTAGE = 1;    //DEPARTURE_BACKSTAGE
    public static int END_OF_BACKSTAGE = END_OF_VALIDATION + ARRIVAL_BACKSTAGE + SERVERS_BACKSTAGE;

    public static int ALL_EVENTS = END_OF_BACKSTAGE;
}
