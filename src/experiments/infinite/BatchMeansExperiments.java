package experiments.infinite;

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


class BatchMeansExperiments {
    static double START   = 0.0;            /* initial (open the door)        */
    static double STOP    = Double.POSITIVE_INFINITY;        /* terminal (close the door) time */

    /* number of servers              */

    static double sarrival = START;

    static double ticketServiceTime = 20;
    static double searchServiceTime = 15;
    static double validationServiceTime = 10;
    static double backstageServiceTime = 28;


    public static void main(String[] args) {

        /* number in each node           */
        long   numberTicket = 0;
        long   numberSearch = 0;
        long   numberVIPValidation = 0;
        long   numberValidation = 0;
        long   numberBackstage = 0;

        int       e;                   /* next event index                   */
        int       s;                   /* server index                       */

        /* used to count processed jobs       */
        long   indexTicket  = 0;
        long   indexSearch  = 0;
        long   indexValidation  = 0;
        long   indexVIPValidation  = 0;
        long   indexBackstage  = 0;

        /* time integrated number in the node */
        double areaTicket   = 0.0;
        double areaSearch   = 0.0;
        double areaValidation   = 0.0;
        double areaVIPValidation   = 0.0;
        double areaBackstage   = 0.0;

        double service;

        BatchMeansExperiments m = new BatchMeansExperiments();
        Rngs r = new Rngs();
        r.plantSeeds(0);

        int k = 64;
        int b = 256;

        double ticketFirstArrival = 0.0;
        double searchFirstArrival = 0.0;
        double validationFirstArrival = 0.0;
        double validationVIPFirstArrival = 0.0;
        double backstageFirstArrival = 0.0;
        double validationLastDeparture = 0.0;
        double validationVIPLastDeparture = 0.0;

        // arrays to store statistics

        // Response time
        List<Double> ticketResponseTimes = new ArrayList<>();
        List<Double> searchResponseTimes = new ArrayList<>();
        List<Double> validationResponseTimes = new ArrayList<>();
        List<Double> validationVIPResponseTimes = new ArrayList<>();
        List<Double> backstageResponseTimes = new ArrayList<>();

        // Delay
        List<Double> ticketDelays = new ArrayList<>();
        List<Double> searchDelays = new ArrayList<>();
        List<Double> validationDelays = new ArrayList<>();
        List<Double> validationVIPDelays = new ArrayList<>();
        List<Double> backstageDelays = new ArrayList<>();

        // Utilization
        List<Double> ticketUtilizations = new ArrayList<>();
        List<Double> searchUtilizations = new ArrayList<>();
        List<Double> globalValidationUtilizations = new ArrayList<>();
        List<Double> backstageUtilizations = new ArrayList<>();

        // Population
        List<Double> ticketPopulations = new ArrayList<>();
        List<Double> searchPopulations = new ArrayList<>();
        List<Double> validationPopulations = new ArrayList<>();
        List<Double> validationVIPPopulations = new ArrayList<>();
        List<Double> backstagePopulations = new ArrayList<>();

        // Interarrival time
        List<Double> ticketInterarrival = new ArrayList<>();
        List<Double> searchInterarrival = new ArrayList<>();
        List<Double> validationInterarrival = new ArrayList<>();
        List<Double> validationVIPInterarrival = new ArrayList<>();
        List<Double> backstageInterarrival = new ArrayList<>();

        // Service Time
        List<Double> ticketService = new ArrayList<>();
        List<Double> searchService = new ArrayList<>();
        List<Double> globalValidationService = new ArrayList<>();
        List<Double> backstageService = new ArrayList<>();

        // Queue Population
        List<Double> ticketQueue = new ArrayList<>();
        List<Double> searchQueue = new ArrayList<>();
        List<Double> validationQueue = new ArrayList<>();
        List<Double> validationVIPQueue = new ArrayList<>();
        List<Double> backstageQueue = new ArrayList<>();

        long batchCounter = 0;

        MsqEvent [] event = new MsqEvent [Events.ALL_EVENTS + 1];
        MsqSum [] sum = new MsqSum [Events.ALL_EVENTS + 1];
        for (s = 0; s < Events.ALL_EVENTS + 1; s++) {
            event[s] = new MsqEvent();
            sum [s]  = new MsqSum();
        }

        MsqT t = new MsqT();

        t.current             = START;
        event[0].t            = m.getArrival(r, 23); // generate first ticket arrival
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
        while (event[0].x != 0) {

            // aggiorna le statistiche
            if(indexTicket != 0 && indexTicket % b == 0){
                batchCounter++;

                //ticket statistics
                double lastTicket = Math.max(event[0].t, event[1].t) -ticketFirstArrival;
                ticketInterarrival.add(lastTicket / indexTicket);
                ticketResponseTimes.add(areaTicket / indexTicket);

                double ticketFinalTime = 0;
                for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)    {
                    if (event[s].t > ticketFinalTime)
                        ticketFinalTime = event[s].t;
                }
                ticketFinalTime = ticketFinalTime - ticketFirstArrival;

                ticketPopulations.add(areaTicket / ticketFinalTime);

                for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)          /* adjust area to calculate */
                    areaTicket -= sum[s].service;              /* averages for the queue   */

                ticketDelays.add(areaTicket / indexTicket);
                ticketQueue.add(areaTicket / ticketFinalTime);

                double sumUtilizations = 0.0;
                double sumServices = 0.0;
                double sumServed = 0.0;

                for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)   {
                    sumUtilizations += sum[s].service / ticketFinalTime;
                    sumServices += sum[s].service;
                    sumServed += sum[s].served;
                }

                ticketUtilizations.add(sumUtilizations / SERVERS_TICKET);
                ticketService.add(sumServices / sumServed);

                // clean variables for the next batch
                areaTicket = 0;
                indexTicket = 0;

                for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)   {
                    sum[s].served = 0;
                    sum[s].service = 0;
                    sum[s].serviceVIP = 0;
                }

                //search statistics
                searchInterarrival.add((event[Events.END_OF_TICKET].t-searchFirstArrival) / indexSearch);
                searchResponseTimes.add(areaSearch / indexSearch);

                double searchFinalTime = 0;
                for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++) {
                    if (event[s].t > searchFinalTime)
                        searchFinalTime = event[s].t;
                }
                searchFinalTime = searchFinalTime - searchFirstArrival;

                searchPopulations.add(areaSearch / searchFinalTime);

                for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++)
                    areaSearch -= sum[s].service;

                searchDelays.add(areaSearch / indexSearch);
                searchQueue.add(areaSearch / searchFinalTime);

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

                // clean variables for the next batch
                areaSearch = 0;
                indexSearch = 0;

                for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++){
                    sum[s].served = 0;
                    sum[s].service = 0;
                    sum[s].serviceVIP = 0;
                }

                //validation statistics
                validationInterarrival.add((validationLastDeparture-validationFirstArrival) / indexValidation);
                validationResponseTimes.add(areaValidation / indexValidation);

                double validationFinalTime = 0;
                for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++) {
                    if (event[s].t > validationFinalTime)
                        validationFinalTime = event[s].t;
                }
                validationFinalTime = validationFinalTime - validationFirstArrival;

                validationPopulations.add(areaValidation / validationFinalTime);

                for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++)
                    areaValidation -= sum[s].service;

                validationDelays.add(areaValidation / indexValidation);
                validationQueue.add(areaValidation / validationFinalTime);

                //vip
                validationVIPInterarrival.add((validationVIPLastDeparture-validationVIPFirstArrival) / indexVIPValidation);
                validationVIPResponseTimes.add(areaVIPValidation / indexVIPValidation);

                double validationVIPFinalTime = 0;
                for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++) {
                    if (event[s].t > validationVIPFinalTime)
                        validationVIPFinalTime = event[s].t;
                }
                validationVIPFinalTime = validationVIPFinalTime - validationVIPFirstArrival;

                validationVIPPopulations.add(areaVIPValidation / validationVIPFinalTime);

                for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++)
                    areaVIPValidation -= sum[s].serviceVIP;

                validationVIPDelays.add(areaVIPValidation / indexVIPValidation);
                validationVIPQueue.add(areaVIPValidation / validationVIPFinalTime);

                sumUtilizations = 0.0;
                sumServices = 0.0;
                sumServed = 0.0;

                double globalValidationTime = Math.max(validationVIPLastDeparture, validationLastDeparture) - Math.min(validationFirstArrival, validationVIPFirstArrival);

                for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++){
                    sumUtilizations += (sum[s].service + sum[s].serviceVIP)/ globalValidationTime;
                    sumServices += sum[s].service + sum[s].serviceVIP;
                    sumServed += sum[s].served;
                }

                globalValidationUtilizations.add(sumUtilizations / SERVERS_VALIDATION);
                globalValidationService.add(sumServices / sumServed);

                // clean variables for the next batch
                areaValidation = 0;
                areaVIPValidation = 0;
                indexValidation = 0;
                indexVIPValidation = 0;

                for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++){
                    sum[s].served = 0;
                    sum[s].service = 0;
                    sum[s].serviceVIP = 0;
                }

                //backstage statistics
                if(indexBackstage == 0)
                    backstageInterarrival.add(0.0);
                else
                    backstageInterarrival.add((event[Events.END_OF_VALIDATION].t-backstageFirstArrival) / indexBackstage);

                if(areaBackstage == 0 || indexBackstage == 0)
                    backstageResponseTimes.add(0.0);
                else
                    backstageResponseTimes.add(areaBackstage / indexBackstage);

                double backstageFinalTime = 0;
                for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++) {
                    if (event[s].t > backstageFinalTime)
                        backstageFinalTime = event[s].t;
                }
                backstageFinalTime = backstageFinalTime - backstageFirstArrival;

                backstagePopulations.add(areaBackstage / backstageFinalTime);

                for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++)
                    areaBackstage -= sum[s].service;

                if(indexBackstage == 0)
                    backstageDelays.add(0.0);
                else
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
                if(sumServed==0)
                    backstageService.add(0.0);
                else
                    backstageService.add(sumServices / sumServed);

                // clean variables for the next batch
                areaBackstage = 0;
                indexBackstage = 0;

                for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++){
                    sum[s].served = 0;
                    sum[s].service = 0;
                    sum[s].serviceVIP = 0;
                }

                ticketFirstArrival= Math.min(event[0].t, event[1].t);
                searchFirstArrival = event[END_OF_TICKET].t;
                validationFirstArrival = event[END_OF_SEARCH+1].t;
                validationVIPFirstArrival = event[END_OF_SEARCH].t;
                backstageFirstArrival = event[END_OF_VALIDATION].t;
            }

            if (batchCounter == k)
                break;

            e         = m.nextEvent(event);                /* next event index */
            t.next    = event[e].t;                        /* next event time  */

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
                event[0].t        = m.getArrival(r, 46);
                if (event[0].t > STOP) // fine simulazione, non generare nuovi eventi
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
                        service = m.getService(r, 69, ticketServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                        s = m.findOneTicket(event);
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
                    service = m.getService(r, 92, ticketServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                    s = m.findOneTicket(event);
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
                    service = m.getService(r, 116, searchServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                    s = m.findOneSearch(event);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                    event[s].x = 1;
                }

                // search departure
            } else if ((e > Events.END_OF_TICKET + Events.ARRIVAL_SEARCH - 1)&&(e < Events.END_OF_SEARCH)){

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
                    service = m.getService(r, 139, searchServiceTime);
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
                    service = m.getService(r, 162, validationServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                    s = m.findOneValidation(event);

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
                } else{
                    numberValidation--;
                    indexValidation++;
                    priority = false;

                    validationLastDeparture = t.current;
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
                if (numberValidation + numberVIPValidation >= Events.SERVERS_VALIDATION) { // come faccio a sapere se ci sono vip da schedulare?
                    // find how many VIP events are on the servers

                    int scheduledVIPs = 0;
                    for (int i = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION-1; i < Events.END_OF_VALIDATION; i++) {         /* now, check the others to find which   */
                        if ((event[i].x == 1))
                            scheduledVIPs += event[i].isPriority;
                    }

                    service = m.getService(r, 185, validationServiceTime);
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
            } else if (e == Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE - 1){

                event[Events.END_OF_VALIDATION].x = 0;

                double backstageThreshold = 0.0614251;
                boolean goToBackstage = underThreshold(backstageThreshold, r);

                if (goToBackstage && event[e].isPriority>0) {

                    numberBackstage++;
                    event[e].isPriority=0;

                    /* if there's no queue, put a job on service */
                    if (numberBackstage <= Events.SERVERS_BACKSTAGE) {
                        service = m.getService(r, 208, backstageServiceTime); // ci inventiamo qui i dati per il servizio dell'evento
                        s = m.findOneBackstage(event);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }

                // backstage departure
            } else if ((e > Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE - 1)&&(e < Events.ALL_EVENTS)){

                indexBackstage++;                                     /* from server s       */
                numberBackstage--;

                /* if there's queue, put a job on service on this server */
                s = e;
                if (numberBackstage >= Events.SERVERS_BACKSTAGE) {
                    service = m.getService(r, 231, backstageServiceTime);
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

                indexTicket++;                                     /* from server s       */
                numberTicket--;

                // generate search arrival
                event[Events.END_OF_TICKET].t = t.current;
                event[Events.END_OF_TICKET].x = 1;

                /* if there's queue, put a job in queue on service on this server */
                s = e;
                if (numberTicket >= Events.SERVERS_TICKET) { // tutti i server sono occupati
                    service = m.getService(r, 255, ticketServiceTime);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                } else {
                    /* if there's no queue, deactivate this server */
                    event[s].x = 0;
                }


            }
        }

        // final statistics
        System.out.println(batchCounter +" batches");

        // Response Time
        createDatFile(ticketResponseTimes, "infinite_horizon_data",  "ticketResponseTimes");
        createDatFile(searchResponseTimes, "infinite_horizon_data", "searchResponseTimes");
        createDatFile(validationResponseTimes, "infinite_horizon_data", "validationResponseTimes");
        createDatFile(validationVIPResponseTimes, "infinite_horizon_data", "validationVIPResponseTimes");
        createDatFile(backstageResponseTimes, "infinite_horizon_data", "backstageResponseTimes");

        // Delay
        createDatFile(ticketDelays, "infinite_horizon_data", "ticketDelays");
        createDatFile(searchDelays, "infinite_horizon_data", "searchDelays");
        createDatFile(validationDelays, "infinite_horizon_data", "validationDelays");
        createDatFile(validationVIPDelays, "infinite_horizon_data", "validationVIPDelays");
        createDatFile(backstageDelays, "infinite_horizon_data", "backstageDelays");

        // Utilization
        createDatFile(ticketUtilizations, "infinite_horizon_data", "ticketUtilizations");
        createDatFile(searchUtilizations, "infinite_horizon_data", "searchUtilizations");
        createDatFile(globalValidationUtilizations, "infinite_horizon_data", "globalValidationUtilizations");
        createDatFile(backstageUtilizations, "infinite_horizon_data", "backstageUtilizations");

        // Population
        createDatFile(ticketPopulations, "infinite_horizon_data", "ticketPopulations");
        createDatFile(searchPopulations, "infinite_horizon_data", "searchPopulations");
        createDatFile(validationPopulations, "infinite_horizon_data", "validationPopulations");
        createDatFile(validationVIPPopulations, "infinite_horizon_data", "validationVIPPopulations");
        createDatFile(backstagePopulations, "infinite_horizon_data", "backstagePopulations");

        // Interarrival time
        createDatFile(ticketInterarrival, "infinite_horizon_data", "ticketInterarrival");
        createDatFile(searchInterarrival, "infinite_horizon_data", "searchInterarrival");
        createDatFile(validationInterarrival, "infinite_horizon_data", "validationInterarrival");
        createDatFile(validationVIPInterarrival, "infinite_horizon_data", "validationVIPInterarrival");
        createDatFile(backstageInterarrival, "infinite_horizon_data", "backstageInterarrival");

        // Service Time
        createDatFile(ticketService, "infinite_horizon_data", "ticketService");
        createDatFile(searchService, "infinite_horizon_data", "searchService");
        createDatFile(globalValidationService, "infinite_horizon_data", "globalValidationService");
        createDatFile(backstageService, "infinite_horizon_data", "backstageService");

        // Queue Population
        createDatFile(ticketQueue, "infinite_horizon_data", "ticketQueue");
        createDatFile(searchQueue, "infinite_horizon_data", "searchQueue");
        createDatFile(validationQueue, "infinite_horizon_data", "validationQueue");
        createDatFile(validationVIPQueue, "infinite_horizon_data", "validationVIPQueue");
        createDatFile(backstageQueue, "infinite_horizon_data", "backstageQueue");

        List<String> filenames = List.of("ticketResponseTimes", "searchResponseTimes", "validationResponseTimes", "validationVIPResponseTimes", "backstageResponseTimes",
                "ticketDelays", "searchDelays", "validationDelays", "validationVIPDelays", "backstageDelays",
                "ticketUtilizations", "searchUtilizations", "globalValidationUtilizations", "backstageUtilizations",
                "ticketPopulations", "searchPopulations", "validationPopulations", "validationVIPPopulations", "backstagePopulations",
                "ticketInterarrival", "searchInterarrival", "validationInterarrival", "validationVIPInterarrival", "backstageInterarrival",
                "ticketService", "searchService", "globalValidationService", "backstageService",
                "ticketQueue", "searchQueue", "validationQueue", "validationVIPQueue", "backstageQueue"
        );

        for (String filename : filenames) {
            Estimate.createEstimate("infinite_horizon_data", filename);
        }
    }

    double exponential(double m, Rngs r) {
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

    double uniform(double a, double b, Rngs r) {
        /* --------------------------------------------
         * generate a Uniform random variate, use a < b
         * --------------------------------------------
         */
        return (a + (b - a) * r.random());
    }

    double getArrival(Rngs r, int stream) {
        /* --------------------------------------------------------------
         * generate the next arrival time, with rate 1/2
         * --------------------------------------------------------------
         */
        double LAMBDA = 1.06481;

        r.selectStream(stream);
        sarrival += exponential(1.0/LAMBDA, r);
        return (sarrival);
    }


    double getService(Rngs r, int stream, double serviceTime) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
        r.selectStream(stream);
        return (exponential(serviceTime, r));
    }

    int nextEvent(MsqEvent [] event) {
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

    int findOneTicket(MsqEvent [] event) {
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

    int findOneSearch(MsqEvent [] event) {
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

    int findOneValidation(MsqEvent [] event) {
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

    int findOneBackstage(MsqEvent [] event) {
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
