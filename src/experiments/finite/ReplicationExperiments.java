package experiments.finite;

import experiments.Estimate;
import libraries.Rngs;
import model.Events;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static model.Events.*;


class MsqT {
    double current;                   /* current time                       */
    double next;                      /* next (most imminent) event time    */
}

class MsqSum {                      /* accumulated sums of                */
    double service;                   /*   service times                    */
    double serviceVIP;                /*   service times for the multipriority queue   */
    long   served;                    /*   number served                    */
}

class MsqEvent{                     /* the next-event list    */
    double t;                         /*   next event time      */
    int    x;                         /*   event status, 0 or 1 */
    int isPriority;               /* used for priority queues */
}


class ReplicationExperiments {

    // arrays to store statistics

    // Response time
    static List<Double> ticketResponseTimes = new ArrayList<>();
    static List<Double> searchResponseTimes = new ArrayList<>();
    static List<Double> validationResponseTimes = new ArrayList<>();
    static List<Double> validationVIPResponseTimes = new ArrayList<>();
    static List<Double> backstageResponseTimes = new ArrayList<>();

    // Delay
    static List<Double> ticketDelays = new ArrayList<>();
    static List<Double> searchDelays = new ArrayList<>();
    static List<Double> validationDelays = new ArrayList<>();
    static List<Double> validationVIPDelays = new ArrayList<>();
    static List<Double> backstageDelays = new ArrayList<>();

    // Utilization
    static List<Double> ticketUtilizations = new ArrayList<>();
    static List<Double> searchUtilizations = new ArrayList<>();
    static List<Double> globalValidationUtilizations = new ArrayList<>();
    static List<Double> backstageUtilizations = new ArrayList<>();

    // Population
    static List<Double> ticketPopulations = new ArrayList<>();
    static List<Double> searchPopulations = new ArrayList<>();
    static List<Double> validationPopulations = new ArrayList<>();
    static List<Double> validationVIPPopulations = new ArrayList<>();
    static List<Double> backstagePopulations = new ArrayList<>();

    // Interarrival time
    static List<Double> ticketInterarrival = new ArrayList<>();
    static List<Double> searchInterarrival = new ArrayList<>();
    static List<Double> validationInterarrival = new ArrayList<>();
    static List<Double> validationVIPInterarrival = new ArrayList<>();
    static List<Double> backstageInterarrival = new ArrayList<>();

    // Service Time
    static List<Double> ticketService = new ArrayList<>();
    static List<Double> searchService = new ArrayList<>();
    static List<Double> globalValidationService = new ArrayList<>();
    static List<Double> backstageService = new ArrayList<>();

    // Queue Population
    static List<Double> ticketQueue = new ArrayList<>();
    static List<Double> searchQueue = new ArrayList<>();
    static List<Double> validationQueue = new ArrayList<>();
    static List<Double> validationVIPQueue = new ArrayList<>();
    static List<Double> backstageQueue = new ArrayList<>();

    // Index
    static List<Double> ticketIndex = new ArrayList<>();
    static List<Double> searchIndex = new ArrayList<>();
    static List<Double> validationIndex = new ArrayList<>();
    static List<Double> validationVIPIndex = new ArrayList<>();
    static List<Double> backstageIndex = new ArrayList<>();

    static double START   = 0.0;            /* initial (open the door)        */
    static double STOP    = 3*3600.0;        /* terminal (close the door) time */

    // variables to compute statistics
    static double ticketFirstDeparture;
    static double searchFirstDeparture;
    static double validationFirstDeparture;
    static double validationVIPFirstDeparture;
    static double backstageFirstDeparture;
    static double validationLastDeparture;
    static double validationVIPLastDeparture;

    // simulation data
    /* number in each node           */
    static long   numberTicket;
    static long   numberSearch;
    static long   numberVIPValidation;
    static long   numberValidation;
    static long   numberBackstage;

    static long   arrivalsGenerated;

    /* used to count processed jobs       */
    static long   indexTicket;
    static long   indexSearch;
    static long   indexValidation;
    static long   indexVIPValidation;
    static long   indexBackstage;

    /* time integrated number in the node */
    static double areaTicket;
    static double areaSearch;
    static double areaValidation;
    static double areaVIPValidation;
    static double areaBackstage;

    static double sarrival = START;

    // service times
    static double ticketServiceTime = 20;
    static double searchServiceTime = 15;
    static double validationServiceTime = 10;
    static double backstageServiceTime = 28;

    public static void main(String[] args){
        /*
            esegue n volte la simulazione, la quale salva i propri dati di esecuzione in una lista.
            Alla fine delle simulazioni tutti i dati vengono salvati su file
         */

        int n = 1000;
        long[] seeds = new long[n+1];
        seeds[0] = 456;
        Rngs r = new Rngs();
        for (int i = 0; i < n; i++) {
            seeds[i+1] = computationalModel(seeds[i], r);
            System.out.println("Simulation: " + i);
        }

        // Response Time
        createDatFile(ticketResponseTimes, "simulation_data",  "ticketResponseTimes");
        createDatFile(searchResponseTimes, "simulation_data", "searchResponseTimes");
        createDatFile(validationResponseTimes, "simulation_data", "validationResponseTimes");
        createDatFile(validationVIPResponseTimes, "simulation_data", "validationVIPResponseTimes");
        createDatFile(backstageResponseTimes, "simulation_data", "backstageResponseTimes");

        // Delay
        createDatFile(ticketDelays, "simulation_data", "ticketDelays");
        createDatFile(searchDelays, "simulation_data", "searchDelays");
        createDatFile(validationDelays, "simulation_data", "validationDelays");
        createDatFile(validationVIPDelays, "simulation_data", "validationVIPDelays");
        createDatFile(backstageDelays, "simulation_data", "backstageDelays");

        // Utilization
        createDatFile(ticketUtilizations, "simulation_data", "ticketUtilizations");
        createDatFile(searchUtilizations, "simulation_data", "searchUtilizations");
        createDatFile(globalValidationUtilizations, "simulation_data", "globalValidationUtilizations");
        createDatFile(backstageUtilizations, "simulation_data", "backstageUtilizations");

        // Population
        createDatFile(ticketPopulations, "simulation_data", "ticketPopulations");
        createDatFile(searchPopulations, "simulation_data", "searchPopulations");
        createDatFile(validationPopulations, "simulation_data", "validationPopulations");
        createDatFile(validationVIPPopulations, "simulation_data", "validationVIPPopulations");
        createDatFile(backstagePopulations, "simulation_data", "backstagePopulations");

        // Interarrival time
        createDatFile(ticketInterarrival, "simulation_data", "ticketInterarrival");
        createDatFile(searchInterarrival, "simulation_data", "searchInterarrival");
        createDatFile(validationInterarrival, "simulation_data", "validationInterarrival");
        createDatFile(validationVIPInterarrival, "simulation_data", "validationVIPInterarrival");
        createDatFile(backstageInterarrival, "simulation_data", "backstageInterarrival");

        // Service Time
        createDatFile(ticketService, "simulation_data", "ticketService");
        createDatFile(searchService, "simulation_data", "searchService");
        createDatFile(globalValidationService, "simulation_data", "globalValidationService");
        createDatFile(backstageService, "simulation_data", "backstageService");

        // Queue Population
        createDatFile(ticketQueue, "simulation_data", "ticketQueue");
        createDatFile(searchQueue, "simulation_data", "searchQueue");
        createDatFile(validationQueue, "simulation_data", "validationQueue");
        createDatFile(validationVIPQueue, "simulation_data", "validationVIPQueue");
        createDatFile(backstageQueue, "simulation_data", "backstageQueue");

        // Index
        createDatFile(ticketIndex, "simulation_data", "ticketIndex");
        createDatFile(searchIndex, "simulation_data", "searchIndex");
        createDatFile(validationIndex, "simulation_data", "validationIndex");
        createDatFile(validationVIPIndex, "simulation_data", "validationVIPIndex");
        createDatFile(backstageIndex, "simulation_data", "backstageIndex");

        List<String> filenames = List.of("ticketResponseTimes", "searchResponseTimes", "validationResponseTimes", "validationVIPResponseTimes", "backstageResponseTimes",
                "ticketDelays", "searchDelays", "validationDelays", "validationVIPDelays", "backstageDelays",
                "ticketUtilizations", "searchUtilizations", "globalValidationUtilizations", "backstageUtilizations",
                "ticketPopulations", "searchPopulations", "validationPopulations", "validationVIPPopulations", "backstagePopulations",
                "ticketInterarrival", "searchInterarrival", "validationInterarrival", "validationVIPInterarrival", "backstageInterarrival",
                "ticketService", "searchService", "globalValidationService", "backstageService",
                "ticketQueue", "searchQueue", "validationQueue", "validationVIPQueue", "backstageQueue",
                "ticketIndex", "searchIndex", "validationIndex", "validationVIPIndex", "backstageIndex"
        );

        for (String filename : filenames) {
            Estimate.createEstimate("simulation_data", filename);
        }

    }


    static long computationalModel(long seed, Rngs r) {

        sarrival = START;
        final int CAPACITY = 11500;

        ticketFirstDeparture = 0;
        searchFirstDeparture = 0;
        validationFirstDeparture = 0;
        validationVIPFirstDeparture = 0;
        backstageFirstDeparture = 0;
        validationLastDeparture = 0;
        validationVIPLastDeparture = 0;

        /* number in each node           */
        numberTicket = 0;
        numberSearch = 0;
        numberVIPValidation = 0;
        numberValidation = 0;
        numberBackstage = 0;

        arrivalsGenerated = 0;

        int       e;                   /* next event index                   */
        int       s;                   /* server index                       */

        /* used to count processed jobs       */
        indexTicket  = 0;
        indexSearch  = 0;
        indexValidation  = 0;
        indexVIPValidation  = 0;
        indexBackstage  = 0;

        /* time integrated number in the node */
        areaTicket   = 0.0;
        areaSearch   = 0.0;
        areaValidation   = 0.0;
        areaVIPValidation   = 0.0;
        areaBackstage   = 0.0;

        double service;

        r.plantSeeds(seed);

        MsqEvent [] event = new MsqEvent [Events.ALL_EVENTS + 1];
        MsqSum [] sum = new MsqSum [Events.ALL_EVENTS + 1];
        for (s = 0; s < Events.ALL_EVENTS + 1; s++) {
            event[s] = new MsqEvent();
            sum [s]  = new MsqSum();
        }

        MsqT t = new MsqT();

        t.current             = START;
        event[0].t            = getArrival(r, 23); // generate first ticket arrival
        event[0].x            = 1;
        event[0].isPriority   = 0;
        for (s = 1; s <= Events.ALL_EVENTS; s++) {
            event[s].t          = START;          /* this value is arbitrary because */
            event[s].x          = 0;              /* all servers are initially idle  */
            event[s].isPriority = 0;
            sum[s].service      = 0.0;
            sum[s].serviceVIP   = 0.0;
            sum[s].served       = 0;
        }

        // START
        while ((event[0].x != 0) || (numberTicket + numberSearch + numberValidation + numberVIPValidation + numberBackstage != 0)) {

            //System.out.println("numberTicket: " + numberTicket);
            //System.out.println("numberSearch: " + numberSearch);
            //System.out.println("numberValidation: " + numberValidation);
            //System.out.println("numberVIPValidation: " + numberVIPValidation);
            //System.out.println("numberBackstage: " + numberBackstage);

            e         = nextEvent(event);                /* next event index */
            t.next    = event[e].t;                        /* next event time  */

            //System.out.println("current Event: " + e);

            // update integrals
            areaTicket     += (t.next - t.current) * numberTicket;
            areaSearch     += (t.next - t.current) * numberSearch;
            areaValidation     += (t.next - t.current) * (numberValidation);
            areaVIPValidation     += (t.next - t.current) * (numberVIPValidation);
            areaBackstage     += (t.next - t.current) * numberBackstage;

            t.current = t.next;                            /* advance the clock*/

            // EVENT MANAGEMENT
            // process a ticket arrival
            if (e == 0) {

                /* generate the next arrival */
                event[0].t        = getArrival(r, 46);
                arrivalsGenerated++;
                if (event[0].t > STOP || arrivalsGenerated>=CAPACITY) // fine simulazione, non generare nuovi eventi
                    event[0].x      = 0;

                double ticketThreshold = 0.75;
                boolean goToSearch = underThreshold(ticketThreshold, r);

                if (goToSearch) { // generate search arrival
                    event[Events.END_OF_TICKET].t = t.current;
                    event[Events.END_OF_TICKET].x = 1;

                }else {

                    numberTicket++;

                    /* if there's no queue, put a job on service */
                    if (numberTicket <= Events.SERVERS_TICKET) {
                        service = getService(r, 69, ticketServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                        s = findOneTicket(event);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }

                // feedback from validation
            } else if (e == Events.ARRIVAL_TICKET - 1) {

                numberTicket++;
                event[Events.ARRIVAL_TICKET-1].x = 0;

                /* if there's no queue, put a job on service */

                if (numberTicket <= Events.SERVERS_TICKET) {
                    service = getService(r, 92, ticketServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                    s = findOneTicket(event);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                    event[s].x = 1;
                }

                // search arrival
            } else if (e == Events.END_OF_TICKET + Events.ARRIVAL_SEARCH - 1){

                numberSearch++;
                event[Events.END_OF_TICKET].x = 0;

                /* if there's no queue, put a job on service */
                if (numberSearch <= Events.SERVERS_SEARCH) {
                    service = getService(r, 116, searchServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                    s = findOneSearch(event);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                    event[s].x = 1;
                }

                // search departure
            } else if ((e > Events.END_OF_TICKET + Events.ARRIVAL_SEARCH - 1)&&(e < Events.END_OF_SEARCH)){

                if (searchFirstDeparture == 0)
                    searchFirstDeparture = t.current;

                indexSearch++;                                     /* from server s       */
                numberSearch--;

                // generate validation arrival
                double vipThreshold = 0.0707826;
                boolean vipValidation = underThreshold(vipThreshold, r);

                if (vipValidation) { // generate vip validation arrival

                    event[Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 2].t = t.current;
                    event[Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 2].x = 1;

                }else { // generate validation arrival

                    event[Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 1].t = t.current;
                    event[Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 1].x = 1;

                }


                /* if there's queue, put a job on service on this server */
                s = e;
                if (numberSearch >= Events.SERVERS_SEARCH) {
                    service = getService(r, 139, searchServiceTime);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                } else {
                    /* if there's no queue, deactivate this server */
                    event[s].x = 0;
                }

                // validation arrival
            } else if (e == Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 1 /* normal */|| e == Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 2 /* vip */){


                int isPriority = 0;
                if(e == Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 2){ // VIP arrivals
                    numberVIPValidation++;
                    isPriority++;
                } else{
                    numberValidation++;
                }

                event[e].x = 0;

                /* if there's no queue, put a job on service */
                if (numberVIPValidation + numberValidation <= Events.SERVERS_VALIDATION) {
                    service = getService(r, 162, validationServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                    s = findOneValidation(event);

                    if(isPriority>0)
                        sum[s].serviceVIP += service;
                    else
                        sum[s].service += service;

                    sum[s].served++;
                    event[s].t            = t.current + service;
                    event[s].x            = 1;
                    event[s].isPriority   += isPriority;
                }

                // validation departure
            } else if ((e > Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 1)&&(e < Events.END_OF_VALIDATION)){

                boolean priority = false;
                if(event[e].isPriority>0){
                    numberVIPValidation--;
                    indexVIPValidation++;
                    event[e].isPriority--;
                    priority = true;

                    validationVIPLastDeparture = t.current;
                    if (validationVIPFirstDeparture == 0)
                        validationVIPFirstDeparture = t.current;
                } else{
                    numberValidation--;
                    indexValidation++;

                    validationLastDeparture = t.current;
                    if (validationFirstDeparture == 0)
                        validationFirstDeparture = t.current;
                }

                double invalidThreshold = 0.06;
                boolean goToTicket = underThreshold(invalidThreshold, r);

                if (goToTicket) {
                    // generate ticket arrival
                    event[Events.ARRIVAL_TICKET-1].t = t.current;
                    event[Events.ARRIVAL_TICKET-1].x = 1;

                } else{
                    // generate backstage arrival
                    event[Events.END_OF_VALIDATION].t = t.current;
                    event[Events.END_OF_VALIDATION].x = 1;

                    if(priority)
                        event[Events.END_OF_VALIDATION].isPriority = 1;
                    else
                        event[Events.END_OF_VALIDATION].isPriority = 0;
                }

                /* if there's queue, put a job on service on this server */
                s = e;
                if (numberValidation + numberVIPValidation >= Events.SERVERS_VALIDATION) {
                    // find how many VIP events are on the servers

                    int scheduledVIPs = 0;
                    for (int i = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION-1; i < Events.END_OF_VALIDATION; i++) {         /* now, check the others to find which   */
                        if ((event[i].x == 1))
                            scheduledVIPs += event[i].isPriority;
                    }

                    service = getService(r, 185, validationServiceTime);
                    sum[s].served++;
                    event[s].t = t.current + service;

                    if(scheduledVIPs < numberVIPValidation) {
                        event[s].isPriority++;
                        sum[s].serviceVIP += service;
                    } else{
                        sum[s].service += service;
                    }

                } else {
                    /* if there's no queue, deactivate this server */
                    event[s].x = 0;
                }

                // backstage arrival
            } else if (e == Events.END_OF_VALIDATION){

                event[Events.END_OF_VALIDATION].x = 0;

                double backstageThreshold = 0.0614251;
                boolean goToBackstage = underThreshold(backstageThreshold, r);

                if (goToBackstage && event[END_OF_VALIDATION].isPriority>0) {

                    numberBackstage++;
                    event[e].isPriority=0;

                    /* if there's no queue, put a job on service */
                    if (numberBackstage <= Events.SERVERS_BACKSTAGE) {
                        service = getService(r, 208, backstageServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                        s = findOneBackstage(event);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }

                // backstage departure
            } else if ((e > Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE - 1)&&(e < Events.ALL_EVENTS)){

                if (backstageFirstDeparture == 0)
                    backstageFirstDeparture = t.current;

                indexBackstage++;                                     /* from server s       */
                numberBackstage--;

                /* if there's queue, put a job on service on this server */
                s = e;
                if (numberBackstage >= Events.SERVERS_BACKSTAGE) {
                    service = getService(r, 231, backstageServiceTime);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                } else {
                    /* if there's no queue, deactivate this server */
                    event[s].x = 0;
                }

            }

            // process a ticket departure
            else {

                if (ticketFirstDeparture == 0)
                    ticketFirstDeparture = t.current;

                indexTicket++;                                     /* from server s       */
                numberTicket--;

                // generate search arrival
                event[Events.END_OF_TICKET].t = t.current;
                event[Events.END_OF_TICKET].x = 1;

                /* if there's queue, put a job in queue on service on this server */
                s = e;
                if (numberTicket >= Events.SERVERS_TICKET) { // tutti i server sono occupati
                    service = getService(r, 255, ticketServiceTime);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                } else {
                    /* if there's no queue, deactivate this server */
                    event[s].x = 0;
                }


            }
        }

        //ticket statistics
        double lastTicket = Math.max(event[0].t, event[1].t);
        ticketInterarrival.add(lastTicket / indexTicket);
        ticketResponseTimes.add(areaTicket / indexTicket);

        double ticketFinalTime = 0;
        for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)    {
            if (event[s].t > ticketFinalTime)
                ticketFinalTime = event[s].t;
        }
        ticketFinalTime = ticketFinalTime - ticketFirstDeparture;

        ticketPopulations.add(areaTicket / ticketFinalTime);

        for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)          /* adjust area to calculate */
            areaTicket -= sum[s].service;              /* averages for the queue   */

        ticketDelays.add(areaTicket / indexTicket);
        ticketQueue.add(areaTicket / ticketFinalTime);

        double sumUtilizations = 0.0;
        double sumServices = 0.0;
        double sumServed = 0.0;

        for (s = 1; s <= SERVERS_TICKET; s++) {
            sumUtilizations += sum[s].service / ticketFinalTime;
            sumServices += sum[s].service;
            sumServed += sum[s].served;
        }

        ticketUtilizations.add(sumUtilizations / SERVERS_TICKET);
        ticketService.add(sumServices / sumServed);
        ticketIndex.add((double)indexTicket);

        //search statistics
        searchInterarrival.add(event[Events.END_OF_TICKET].t / indexSearch);
        searchResponseTimes.add(areaSearch / indexSearch);

        double searchFinalTime = 0;
        for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++) {
            if (event[s].t > searchFinalTime)
                searchFinalTime = event[s].t;
        }
        searchFinalTime = searchFinalTime - searchFirstDeparture;

        searchPopulations.add(areaSearch / searchFinalTime);

        for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++)
            areaSearch -= sum[s].service;

        searchDelays.add(areaTicket / indexTicket);
        searchQueue.add(areaTicket / searchFinalTime);

        sumUtilizations = 0.0;
        sumServices = 0.0;
        sumServed = 0.0;

        for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++){
            sumUtilizations += sum[s].service / searchFinalTime;
            sumServices += sum[s].service;
            sumServed += sum[s].served;
        }

        searchUtilizations.add(sumUtilizations / SERVERS_SEARCH);
        searchService.add(sumServices / sumServed);
        searchIndex.add((double)indexSearch);

        //validation statistics
        validationInterarrival.add(validationLastDeparture / indexValidation);
        validationResponseTimes.add(areaValidation / indexValidation);

        double validationFinalTime = 0;
        for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++) {
            if (event[s].t > validationFinalTime)
                validationFinalTime = event[s].t;
        }
        validationFinalTime = validationFinalTime - validationFirstDeparture;

        validationPopulations.add(areaValidation / validationFinalTime);

        for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++)
            areaValidation -= sum[s].service;

        validationDelays.add(areaValidation / indexValidation);
        validationQueue.add(areaValidation / validationFinalTime);

        //vip
        validationVIPInterarrival.add(validationVIPLastDeparture / indexVIPValidation);
        validationVIPResponseTimes.add(areaVIPValidation / indexVIPValidation);

        double validationVIPFinalTime = 0;
        for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++) {
            if (event[s].t > validationVIPFinalTime)
                validationVIPFinalTime = event[s].t;
        }
        validationVIPFinalTime = validationVIPFinalTime - validationVIPFirstDeparture;

        validationVIPPopulations.add(areaVIPValidation / validationVIPFinalTime);

        for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++)
            areaVIPValidation -= sum[s].serviceVIP;

        validationVIPDelays.add(areaVIPValidation / indexVIPValidation);
        validationVIPQueue.add(areaVIPValidation / validationVIPFinalTime);

        sumUtilizations = 0.0;
        sumServices = 0.0;
        sumServed = 0.0;

        double globalValidationTime = Math.max(validationVIPLastDeparture, validationLastDeparture) - Math.min(validationFirstDeparture, validationVIPFirstDeparture);

        for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++){
            sumUtilizations += (sum[s].service + sum[s].serviceVIP)/ globalValidationTime;
            sumServices += sum[s].service + sum[s].serviceVIP;
            sumServed += sum[s].served;
        }

        globalValidationUtilizations.add(sumUtilizations / SERVERS_VALIDATION);
        globalValidationService.add(sumServices / sumServed);
        validationIndex.add((double)indexValidation);
        validationVIPIndex.add((double)indexVIPValidation);

        //backstage statistics
        backstageInterarrival.add(event[Events.END_OF_VALIDATION].t / indexBackstage);
        backstageResponseTimes.add(areaBackstage / indexBackstage);

        double backstageFinalTime = 0;
        for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++) {
            if (event[s].t > backstageFinalTime)
                backstageFinalTime = event[s].t;
        }
        backstageFinalTime = backstageFinalTime - backstageFirstDeparture;

        backstagePopulations.add(areaBackstage / backstageFinalTime);

        for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++)
            areaBackstage -= sum[s].service;

        backstageDelays.add(areaBackstage / indexBackstage);
        backstageQueue.add(areaBackstage / backstageFinalTime);

        sumUtilizations = 0.0;
        sumServices = 0.0;
        sumServed = 0.0;

        for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++){
            sumUtilizations += sum[s].service / backstageFinalTime;
            sumServices += sum[s].service;
            sumServed += sum[s].served;
        }

        backstageUtilizations.add(sumUtilizations / SERVERS_BACKSTAGE);
        backstageService.add(sumServices / sumServed);
        backstageIndex.add((double)indexBackstage);

        r.selectStream(255);
        return r.getSeed();
    }
    static double exponential(double m, Rngs r) {
        /* ---------------------------------------------------
         * generate an Exponential random variate, use m > 0.0
         * ---------------------------------------------------
         */
        return (-m * Math.log(1.0 - r.random()));
    }

    static boolean underThreshold(double threshold, Rngs r) {
        /* ---------------------------------------------------
         * Decide whether to skip the next queue
         * ---------------------------------------------------
         */
        return r.random()<=threshold;
    }

    static double getArrival(Rngs r, int stream) {
        /* --------------------------------------------------------------
         * generate the next arrival time, with rate 1/2
         * --------------------------------------------------------------
         */
        double LAMBDA = 1.06481;

        r.selectStream(stream);
        sarrival += exponential(1.0/LAMBDA, r);
        return (sarrival);
    }


    static double getService(Rngs r, int stream, double serviceTime) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
        r.selectStream(stream);
        return (exponential(serviceTime, r));
    }

    static int nextEvent(MsqEvent [] event) {
        /* ---------------------------------------
         * return the index of the next event type
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */
        e = i;
        while (i < Events.ALL_EVENTS-1) {         /* now, check the others to find which  */
            i++;                        /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
                e = i;
        }
        return (e);
    }

    static int findOneTicket(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available ticket server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = Events.ARRIVAL_TICKET;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < Events.SERVERS_TICKET + Events.ARRIVAL_TICKET) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }

    static int findOneSearch(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available search server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < Events.END_OF_TICKET + Events.SERVERS_SEARCH) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }

    static int findOneValidation(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available validation server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < Events.END_OF_SEARCH + Events.SERVERS_VALIDATION) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }

    static int findOneBackstage(MsqEvent [] event) {
        /* -----------------------------------------------------
         * return the index of the available backstage server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }

    public static void createDatFile(List<Double> list, String directoryName, String filename) {
        File directory = new File(directoryName);
        BufferedWriter bw = null;
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, filename + ".dat");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            bw = new BufferedWriter(writer);

            for (int i = 0; i < list.size(); i++) {
                bw.append(String.valueOf(list.get(i)));
                bw.append("\n");
                bw.flush();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                bw.flush();
                bw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
