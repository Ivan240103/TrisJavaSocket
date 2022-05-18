package trisclient;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client0 del gioco tris che si connette alla porta 10000
 *
 * @author Ivan De Simone
 */
public class Client0 {

    //nome simbolico del server di gioco
    String serverName = "localhost";
    //porta del server di gioco
    int serverPort = 10000;
    //connessione
    Socket s;
    //canale di output
    DataOutputStream outStream;
    //canale di input
    BufferedReader inStream;
    //scanner per leggere i dati da tastiera
    Scanner sc = new Scanner(System.in);

    /**
     * Connette il client al server
     * 
     * @return oggetto connessione Socket
     */
    protected Socket connect() {
        System.out.println("Client0 partito");
        try {
            //creazione della connessione
            s = new Socket(serverName, serverPort);
            outStream = new DataOutputStream(s.getOutputStream());
            inStream = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("Errore - metodo connect()");
        }
        return s;
    }

    /**
     * Esecuzione del proprio turno di gioco.
     * 
     * @return true se il gioco deve andare avanti, false se deve terminare
     */
    protected boolean communicate() {
        //true se si continua, false se c'è un vincitore
        boolean isToContinue = true;
        //validità della mossa: 1 = valida, 0 = non valida
        int valid = 1;
        try {
            //segnalazione giocatore attuale / vittoria avversario / pareggio
            String read = inStream.readLine();
            System.out.println(read);
            //se la stringa contiene 'Partita conclusa' il gioco viene terminato
            if (read.contains("Partita conclusa")) {
                isToContinue = false;
            }
            //se la stringa contiene '0' è il turno di questo client
            else if (read.contains("0")) {
                //output della griglia
                System.out.println(parseStringMatrix(inStream.readLine()));
                do {
                    //controlla la validità della mossa
                    if (valid == 0) {
                        System.out.println("La posizione inserita non è valida.");
                    }
                    //risposta riga dove posizionare
                    System.out.print(inStream.readLine());
                    int r = sc.nextInt();
                    outStream.writeBytes(Integer.toString(r) + '\n');
                    //risposta colonna dove posizionare
                    System.out.print(inStream.readLine());
                    int c = sc.nextInt();
                    outStream.writeBytes(Integer.toString(c) + '\n');
                    //riceve la validità o meno della mossa
                    valid = Integer.parseInt(inStream.readLine());
                } while (valid != 1);
                //output della griglia
                System.out.println(parseStringMatrix(inStream.readLine()));
                //segnalazione cambio turno / vittoria / pareggio
                read = inStream.readLine();
                System.out.println(read);
                //se la stringa contiene 'Partita conclusa' il gioco viene terminato
                if (read.contains("Partita conclusa")) {
                    isToContinue = false;
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Errore - metodo communicate()");
        }
        return isToContinue;
    }

    /**
     * Converte la matrice da formato stringa a forma tabellare.
     * 
     * @param s - matrice in formato stringa (trasferimento)
     * @return matrice in formato tabellare
     */
    private String parseStringMatrix(String s) {
        //divide la matrice in righe
        String[] rows = s.split(",");
        //ricompone la matrice andando a capo dopo ogni riga
        return rows[0] + "\n" + rows[1] + "\n" + rows[2];
    }

    public static void main(String args[]) {
        //istanza del client
        Client0 c = new Client0();
        //creazione connessione
        c.connect();
        //svolgimento del gioco fino a che non viene interrotto
        while (c.communicate());
    }
}
