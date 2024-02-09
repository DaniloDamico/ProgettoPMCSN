package model;

import libraries.Rngs;


import java.lang.*;
import java.text.*;


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


class ModelloComputazionale {
  static double START   = 0.0;            /* initial (open the door)        */
  static double STOP    = 3*3600.0;        /* terminal (close the door) time */

  final static int CAPACITY = 11500;

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

    long   arrivalsGenerated = 0;

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

    ModelloComputazionale m = new ModelloComputazionale();
    Rngs r = new Rngs();
    r.plantSeeds(0);


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
    while ((event[0].x != 0) || (numberTicket + numberSearch + numberValidation + numberVIPValidation + numberBackstage != 0)) {

      //System.out.println("numberTicket: " + numberTicket);
      //System.out.println("numberSearch: " + numberSearch);
      //System.out.println("numberValidation: " + numberValidation);
      //System.out.println("numberVIPValidation: " + numberVIPValidation);
      //System.out.println("numberBackstage: " + numberBackstage);

      e         = m.nextEvent(event);                /* next event index */
      t.next    = event[e].t;                        /* next event time  */

      System.out.println("current Event: " + e);

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
        } else{
          numberValidation--;
          indexValidation++;
          priority = false;
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

    DecimalFormat f = new DecimalFormat("###0.00");
    DecimalFormat g = new DecimalFormat("###0.000");

    System.out.println("\nfor " + indexTicket + " jobs the Ticket service node statistics are:\n");
    System.out.println("  avg interarrivals .. =   " + f.format(event[0].t / indexTicket));
    System.out.println("  avg wait ........... =   " + f.format(areaTicket / indexTicket) +"s, or " + f.format(areaTicket / indexTicket/60.0) + "min");
    System.out.println("  avg # in node ...... =   " + f.format(areaTicket / t.current));

    for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++)          /* adjust area to calculate */
       areaTicket -= sum[s].service;              /* averages for the queue   */

    System.out.println("  avg delay .......... =   " + f.format(areaTicket / indexTicket));
    System.out.println("  avg # in queue ..... =   " + f.format(areaTicket / t.current));
    System.out.println("\nthe ticket server statistics are:\n");
    System.out.println("    server     utilization     avg service      share");
    for (s = Events.ARRIVAL_TICKET; s <= Events.ARRIVAL_TICKET + Events.SERVERS_TICKET -1; s++) {
      System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
      System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)indexTicket));
    }

    System.out.println("");

    System.out.println("\nfor " + indexSearch + " jobs the Search service node statistics are:\n");
    System.out.println("  avg interarrivals .. =   " + f.format(event[Events.END_OF_TICKET].t / indexSearch));
    System.out.println("  avg wait ........... =   " + f.format(areaSearch / indexSearch) +"s, or " + f.format(areaSearch / indexSearch/60.0) + "min");
    System.out.println("  avg # in node ...... =   " + f.format(areaSearch / t.current));

    for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++)          /* adjust area to calculate */
      areaSearch -= sum[s].service;              /* averages for the queue   */

    System.out.println("  avg delay .......... =   " + f.format(areaSearch / indexSearch));
    System.out.println("  avg # in queue ..... =   " + f.format(areaSearch / t.current));
    System.out.println("\nthe search server statistics are:\n");
    System.out.println("    server     utilization     avg service      share");

    for (s = Events.END_OF_TICKET + Events.ARRIVAL_SEARCH; s <= Events.END_OF_TICKET + Events.SERVERS_SEARCH; s++) {
      System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
      System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)indexSearch));
    }

    System.out.println("");

    System.out.println("\nfor " + indexValidation + " jobs the Validation service node statistics are:\n");
    System.out.println("  avg interarrivals .. =   " + f.format(event[Events.END_OF_SEARCH+1].t / indexValidation));
    System.out.println("  avg wait ........... =   " + f.format(areaValidation / indexValidation) +"s, or " + f.format(areaValidation / indexValidation/60.0) + "min");
    System.out.println("  avg # in node ...... =   " + f.format(areaValidation / t.current));

    for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++)            /* adjust area to calculate */
      areaValidation -= sum[s].service;              /* averages for the queue   */

    System.out.println("  avg delay .......... =   " + f.format(areaValidation / indexValidation));
    System.out.println("  avg # in queue ..... =   " + f.format(areaValidation / t.current));

    System.out.println("\nfor " + indexVIPValidation + " jobs the VIP Validation service node statistics are:\n");
    System.out.println("  avg interarrivals .. =   " + f.format(event[Events.END_OF_SEARCH+1].t / indexVIPValidation));
    System.out.println("  avg wait ........... =   " + f.format(areaVIPValidation / indexVIPValidation) +"s, or " + f.format(areaVIPValidation / indexVIPValidation/60.0) + "min");
    System.out.println("  avg # in node ...... =   " + f.format(areaVIPValidation / t.current));

    for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.SERVERS_VALIDATION; s++)            /* adjust area to calculate */
      areaVIPValidation -= sum[s].serviceVIP;              /* averages for the queue   */

    System.out.println("  avg delay .......... =   " + f.format(areaVIPValidation / indexVIPValidation));
    System.out.println("  avg # in queue ..... =   " + f.format(areaVIPValidation / t.current));
    System.out.println("\nthe validation server statistics are:\n");
    System.out.println("    server     utilization     avg service      share");

    for (s = Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION; s <= Events.END_OF_SEARCH + Events.ARRIVAL_VALIDATION - 1 + Events.SERVERS_VALIDATION; s++) {
      System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
      System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)indexValidation));
    }

    System.out.println("");

    System.out.println("\nfor " + indexBackstage + " jobs the Backstage service node statistics are:\n");
    System.out.println("  avg interarrivals .. =   " + f.format(event[Events.END_OF_VALIDATION].t / indexBackstage));
    System.out.println("  avg wait ........... =   " + f.format(areaBackstage / indexBackstage) +"s, or " + f.format(areaBackstage / indexBackstage/60.0) + "min");
    System.out.println("  avg # in node ...... =   " + f.format(areaBackstage / t.current));

    for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++)            /* adjust area to calculate */
      areaBackstage -= sum[s].service;              /* averages for the queue   */

    System.out.println("  avg delay .......... =   " + f.format(areaBackstage / indexBackstage));
    System.out.println("  avg # in queue ..... =   " + f.format(areaBackstage / t.current));
    System.out.println("\nthe backstage server statistics are:\n");
    System.out.println("    server     utilization     avg service      share");

    for (s = Events.END_OF_VALIDATION + Events.ARRIVAL_BACKSTAGE; s <= Events.END_OF_VALIDATION + Events.SERVERS_BACKSTAGE; s++) {
      System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
      System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double)indexBackstage));
    }

    System.out.println("");
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
}
