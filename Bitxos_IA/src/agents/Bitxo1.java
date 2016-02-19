package agents;

// Exemple de Bitxo
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bitxo1 extends Agent {

    static final int PARET = 0;
    static final int NAU = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;
    int espera = 0;

    //PROPIS
    static Punt objectiu;
    static Punt objectiu_guard;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1", "imatges/robotank1.gif");
    }

    @Override
    public void inicia() {
        setAngleVisors(10);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(2);

        espera = 0;
        objectiu = null;
        objectiu_guard = null;

    }

    @Override
    public void avaluaComportament() {

        boolean enemic;

        enemic = false;

        int dir;

        estat = estatCombat();
//
//        System.out.println(estat.angle);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Bitxo1.class.getName()).log(Level.SEVERE, null, ex);
//        }

        if (espera > 0) {
            espera--;
        } else {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara

                if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5) {
                    dispara();   //bloqueig per nau, no giris dispara
                } else // hi ha un obstacle, gira i parteix
                {
                    gira(20); // 20 graus
                    if (hiHaParedDavant(20)) {
                        enrere();
                    } else {
                        endavant();
                    }
                    espera = 3;
                }
            } else {
                endavant();

                // Miram els visors per detectar els obstacles
                //Falta seguir amb els ifs
                int sensor = 0;

                if (estat.angle < 90 && estat.angle > 0) {
                    if (estat.objecteVisor[ESQUERRA] == PARET
                            && estat.distanciaVisors[ESQUERRA] < 15) {

                        gira((int) (0 - estat.angle));

                    } else if (estat.objecteVisor[DRETA] == PARET
                            && estat.distanciaVisors[DRETA] < 15) {
                        gira((int) (90 - estat.angle));
                    }
                }
//                if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 45) {
//                    sensor += 2;
//                }
//                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 45) {
//                    sensor += 4;
//                }

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
                        {
                            esquerra();
                        }
//                            }
//                        }
                        break;
                }

            }
        }
    }

    double diferenciAntenes(double a, double b) {
        return a - b;
    }

    boolean hiHaParedDavant(int dist) {

        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <= dist) {
            return true;
        }

        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] <= dist) {
            return true;
        }

        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] <= dist) {
            return true;
        }

        return false;
    }

    Punt objectiuMesProper(int tipus) {
        Punt mes_proper = null;
        for (int i = 0; i < estat.bonificacions.length; i++) {
            if (estat.bonificacions[i].tipus == tipus) {
                if (mes_proper == null) {
                    mes_proper = estat.bonificacions[i].posicio;
                } else if (distancia(estat.bonificacions[i].posicio) < distancia(mes_proper)) {
                    mes_proper = estat.bonificacions[i].posicio;
                }
            }
        }
        return mes_proper;
    }

    double distancia(Punt p) {
        int a = estat.posicio.x - p.x;
        int b = estat.posicio.y - p.y;
        return calcularVector(a, b);
    }

    double calcularVector(int x, int y) {
        return Math.sqrt((x * x) + (y * y));
    }

    double minimaDistanciaVisors() {
        double minim;

        minim = Double.POSITIVE_INFINITY;
        if (estat.objecteVisor[ESQUERRA] == PARET) {
            minim = estat.distanciaVisors[ESQUERRA];
        }
        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < minim) {
            minim = estat.distanciaVisors[CENTRAL];
        }
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < minim) {
            minim = estat.distanciaVisors[DRETA];
        }
        return minim;
    }
}
