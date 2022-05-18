package trisserver;

import java.io.*;
import java.net.*;

/**
 * Server che gestisce il gioco Tris. Connette due client sulle porte 10000 e
 * 10001 contemporaneamente.
 *
 * @author Ivan De Simone
 */
public class Server {

    //connessione lato server
    private ServerSocket server0, server1;
    //connessione lato client
    private Socket client0, client1;
    //canali di output
    private DataOutputStream outStream0, outStream1;
    //canali di input
    private BufferedReader inStream0, inStream1;

    //dimensione della griglia di gioco
    private final int DIM = 3;
    //griglia di gioco (il 5 rappresenta una casella vuota)
    private final int grid[][] = {{5, 5, 5}, {5, 5, 5}, {5, 5, 5}};

    /**
     * Esegue la connessione con i due client. Prima con la porta 10000 e poi
     * con la porta 10001.
     */
    protected void start() {
        System.out.println("Server partito");
        try {
            //connessione del primo client
            server0 = new ServerSocket(10000);
            client0 = server0.accept();
            inStream0 = new BufferedReader(new InputStreamReader(client0.getInputStream()));
            outStream0 = new DataOutputStream(client0.getOutputStream());
            server0.close();
            System.out.println("Creata connessione con client0");
            //connessione del secondo client
            server1 = new ServerSocket(10001);
            client1 = server1.accept();
            inStream1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
            outStream1 = new DataOutputStream(client1.getOutputStream());
            server1.close();
            System.out.println("Creata connessione con client1");
        } catch (IOException e) {
            System.out.println("Errore - metodo start()");
        }
    }

    /**
     * Richiede al client la mossa da effettuare e la esegue, poi controlla se è
     * stato fatto tris.
     *
     * @param player - numero del giocatore attuale (0 o 1)
     * @return true se la mossa è vincente, false se il gioco deve proseguire
     */
    protected boolean move(int player) {
        //canali di input e output
        DataOutputStream outStream;
        BufferedReader inStream;
        //in base al giocatore che muove seleziona i canali da usare
        if (player == 0) {
            outStream = outStream0;
            inStream = inStream0;
        } else {
            outStream = outStream1;
            inStream = inStream1;
        }
        try {
            //mostra la griglia al client
            outStream.writeBytes(gridToString());
            //riga e colonna in cui posizionare
            int r, c;
            //validità della mossa: 1 = valida, 0 = non valida
            int valid = 1;
            do {
                do {
                    //segnala al client la non correttezza
                    if (valid == 0) {
                        outStream.writeBytes(Integer.toString(valid) + '\n');
                    }
                    //riporta a default il valore di valid
                    valid = 1;
                    //richiede la riga in cui posizionare
                    outStream.writeBytes("Inserisci la riga (0,1,2): " + '\n');
                    r = Integer.parseInt(inStream.readLine());
                    //richiede la colonna in cui posizionare
                    outStream.writeBytes("Inserisci la colonna (0,1,2): " + '\n');
                    c = Integer.parseInt(inStream.readLine());
                    //controlla che le posizioni siano esistenti
                    if (r < 0 || r > 2 || c < 0 || c > 2) {
                        valid = 0;
                    }
                } while (valid == 0);
                //controlla che la posizione sia libera, nel caso la occupa
                if (!place(r, c, player)) {
                    valid = 0;
                }
            } while (valid == 0);
            //segnala al client che la mossa è stata eseguita correttamente
            outStream.writeBytes(Integer.toString(valid) + '\n');
            //mostra la griglia aggiornata
            outStream.writeBytes(gridToString());
            //controlla se è stato fatto tris
            if (checkTris(player)) {
                //nel caso segnala la vittoria
                reportWin(player);
                return true;
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Errore - metodo move()");
        }
        return false;
    }

    /**
     * Cambia il giocatore che attualmente gioca.
     *
     * @param player - numero del giocatore attuale (0 o 1)
     * @return numero del giocatore del nuovo turno
     */
    protected int changeTurn(int player) {
        //scambia da 0 a 1 e viceversa
        int g = (player + 1) % 2;
        try {
            //segnala ad entrambi i client il giocatore attuale
            outStream0.writeBytes("Tocca al giocatore " + g  + '\n');
            outStream1.writeBytes("Tocca al giocatore " + g  + '\n');
        } catch (IOException e) {
            System.out.println("Errore - metodo changeTurn()");
        }
        return g;
    }

    /**
     * Controlla se la casella è libera, nel caso la occupa con il segno del
     * giocatore attuale.
     *
     * @param row - riga in cui posizionare
     * @param col - colonna in cui posizionare
     * @param playerValue - valore che rappresenta il giocatore (0 o 1)
     * @return true se la mossa è andata a buon fine, false altrimenti
     */
    private boolean place(int row, int col, int playerValue) {
        //controlla se la posizione è libera
        if (grid[row][col] == 5) {
            //occupa la posizione
            grid[row][col] = playerValue;
            return true;
        }
        return false;
    }

    /**
     * Controlla se il giocatore ha fatto tris.
     *
     * @param player - numero del giocatore attuale (0 o 1)
     * @return true se è tris, false se non lo è
     */
    private boolean checkTris(int player) {
        //valore che deve avere la somma della riga, colonna o diagonale per
        //essere tris in base al giocatore attuale
        int sum;
        if (player == 0) {
            sum = 0;
        } else {
            sum = 3;
        }
        //controlla le righe, colonne e diagonali
        return checkRows(sum) || checkColumns(sum) || checkDiagonals(sum);
    }

    /**
     * Controlla se è stato fatto tris nelle righe.
     *
     * @param value - somma che dovrebbe avere la riga per essere tris
     * @return true se è tris, false altrimenti
     */
    private boolean checkRows(int value) {
        //somma i valori per riga
        for (int i = 0; i < DIM; i++) {
            int sum = 0;
            for (int j = 0; j < DIM; j++) {
                sum += grid[i][j];
            }
            //controlla se è tris
            if (sum == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controlla se è stato fatto tris nelle colonne.
     *
     * @param value - somma che dovrebbe avere la colonna per essere tris
     * @return true se è tris, false altrimenti
     */
    private boolean checkColumns(int value) {
        //somma i valori per colonna
        for (int i = 0; i < DIM; i++) {
            int sum = 0;
            for (int j = 0; j < DIM; j++) {
                sum += grid[j][i];
            }
            //controlla se è tris
            if (sum == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controlla se è stato fatto tris nelle diagonali.
     *
     * @param value - somma che dovrebbe avere la diagonale per essere tris
     * @return true se è tris, false altrimenti
     */
    private boolean checkDiagonals(int value) {
        //somma diagonale principale
        int maxDiagonal = grid[0][0] + grid[1][1] + grid[2][2];
        //somma diagonale secondaria
        int minDiagonal = grid[0][2] + grid[1][1] + grid[2][0];
        //controlla se è tris
        return maxDiagonal == value || minDiagonal == value;
    }

    /**
     * Segnala la vittoria ai client e termina le connessioni.
     *
     * @param player - numero del giocatore attuale (0 o 1)
     */
    private void reportWin(int player) {
        try {
            if (player == 0) {
                //client0 ha vinto, client1 ha perso
                outStream0.writeBytes("Partita conclusa. Hai vinto!" + '\n');
                outStream1.writeBytes("Partita conclusa. Hai perso." + '\n');
            } else {
                //client0 ha perso, client1 ha vinto
                outStream1.writeBytes("Partita conclusa. Hai vinto!" + '\n');
                outStream0.writeBytes("Partita conclusa. Hai perso." + '\n');
            }
            //chiusura delle connessioni
            client0.close();
            client1.close();
            System.out.println("Connessioni terminate.");
        } catch (IOException e) {
            System.out.println("Errore - metodo reportWin()");
        }
    }

    /**
     * Segnala il pareggio ai client e termina le connessioni
     */
    protected void reportDraw() {
        try {
            //segnalazione e chiusura connessioni
            outStream0.writeBytes("Partita conclusa. Pareggio." + '\n');
            client0.close();
            outStream1.writeBytes("Partita conclusa. Pareggio." + '\n');
            client1.close();
            System.out.println("Connessioni terminate.");
        } catch (IOException e) {
            System.out.println("Errore - metodo reportDraw()");
        }
    }

    /**
     * Rappresenta la griglia in una stringa. Sostituisce al client0 la X, al
     * client1 il O e alle caselle vuote il -. Le righe sono separate da ','.
     *
     * @return griglia in formato String
     */
    private String gridToString() {
        String gridStr = "";
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                //in base all'occupazione sostituisce il simbolo
                switch (grid[i][j]) {
                    default:
                        gridStr += " -";
                        break;
                    case 0:
                        gridStr += " X";
                        break;
                    case 1:
                        gridStr += " O";
                        break;
                }
            }
            //divisione delle righe
            gridStr += ",";
        }
        return gridStr + '\n';
    }
}
