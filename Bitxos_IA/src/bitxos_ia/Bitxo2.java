package agents;

// Exemple de Bitxo,

public class Bitxo2 extends Agent {

    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;

    Estat estat;
    int espera =0;

    public Bitxo2(Agents pare) {
        super(pare, "Bitxo2", "imatges/robotank2.gif");
    }
  
        @Override
    public void inicia()
    {
        setAngleVisors(30);
        setDistanciaVisors(400);
        setVelocitatLineal(5);
        setVelocitatAngular(2);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
    {
        boolean enemic;

        enemic = false;

        int dir;

        activaEscut();
        estat = estatCombat();
        if (espera > 0) {
            espera--;
        }
        else
        {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara

                if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5)
                {
                    dispara();   //bloqueig per nau, no giris dispara
                }
                else // hi ha un obstacle, gira i parteix
                {
                    gira(20); // 20 graus
                    if (hiHaParedDavant(20)) enrere();
                    else endavant();
                    espera=3;
                }
            } else {
                endavant();
                if (estat.veigEnemic)
                {
                    if (estat.sector == 2 || estat.sector == 3)
                        mira(estat.posicioEnemic.x, estat.posicioEnemic.y);
                    else if (estat.sector == 1)  dreta();
                    else  esquerra();
                }

                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant && estat.impactesRival < 5)
                {
                    dispara();
                }
                // Miram els visors per detectar els obstacles
                int sensor = 0;

                if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 45) {
                    sensor += 1;
                }
                if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 45) {
                    sensor += 2;
                }
                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 45) {
                    sensor += 4;
                }

                switch (sensor) {
                    case 0:
                        endavant();
                        break;
                    case 1:
                    case 3:  // esquerra bloquejada
                        dreta();
                        break;
                    case 4:
                    case 6:  // dreta bloquejada
                        esquerra();
                        break;
                    case 5:
                        endavant();
                        break;  // centre lliure
                    case 2:  // paret devant
                    case 7:  // si estic molt aprop, torna enrere
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 15) {
                            espera = 8;
                            enrere();
                        } else // gira aleatòriament a la dreta o a l'esquerra
//                        if (distancia < 50) {
//                            if (Math.random() * 500 < 250) {
//                                dreta();
//                            } else {
                                esquerra();
//                            }
//                        }
                        break;
                }

            }
        }
    }

    boolean hiHaParedDavant(int dist)
    {

       if (estat.objecteVisor[ESQUERRA]== PARET && estat.distanciaVisors[ESQUERRA]<=dist)
           return true;

       if (estat.objecteVisor[CENTRAL ]== PARET && estat.distanciaVisors[CENTRAL ]<=dist)
           return true;

       if (estat.objecteVisor[DRETA   ]== PARET && estat.distanciaVisors[DRETA   ]<=dist)
           return true;

       return false;
    }

    double minimaDistanciaVisors()
    {
        double minim;

        minim = Double.POSITIVE_INFINITY;
        if (estat.objecteVisor[ESQUERRA] == PARET)
            minim = estat.distanciaVisors[ESQUERRA];
        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL]<minim)
            minim = estat.distanciaVisors[CENTRAL];
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA]<minim)
            minim = estat.distanciaVisors[DRETA];
        return minim;
    }
}