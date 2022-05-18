package trisserver;

/**
 * Main lato server di gioco - Tris
 *
 * @author Ivan De Simone
 */
public class GameMain {

    public static void main(String args[]) {
        //istanza del server
        Server s = new Server();
        //decide casualmente il giocatore che inizia
        int player = (int) (Math.random() * 2);
        //contatore dei turni
        int c = 0;
        //creazione delle connessioni
        s.start();
        //loop: il gioco viene interrotto dall'interno
        while (true) {
            //cambio turno
            player = s.changeTurn(player);
            //escuzione della mossa. Se ritorna true ha vinto il giocatore attuale
            if (s.move(player)) {
                break;
            }
            //aumenta il contatore
            c++;
            //se al nono turno non ha vinto nessuno è pareggio
            if (c == 9) {
                s.reportDraw();
                break;
            }
        }
    }
}
