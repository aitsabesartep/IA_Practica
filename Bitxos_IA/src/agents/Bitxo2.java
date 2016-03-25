package agents;

// Exemple de Bitxo,
import java.util.ArrayList;

public class Bitxo2 extends Agent {
    
    static final int PARET = 0;
    static final int NAU = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    static final int AMPLADA = 800;
    static final int ALTURA = 600;
    ArrayList<vector1> marcar_linia = new ArrayList<vector1>();
    static final int RADI = 55;
    static Punt memoria = new Punt(0, 0);
    static Punt memoria_old = new Punt(0, 0);
    static Punt memoria_old_posicio = new Punt(0, 0);
    static int control = 0;
    static long darrer_hyper_old;
    static int control_impactes;
    double temps = 0;

    Estat estat;
    int espera = 0;

    public Bitxo2(Agents pare) {
        super(pare, "Champi", "imatges/img1.gif");
    }

    @Override
    public void inicia() {
        setAngleVisors(10);
        setDistanciaVisors(350);
        setVelocitatLineal(4);
        setVelocitatAngular(4);
        control_impactes = 0;
        espera = 0;
        darrer_hyper_old = 30000;
    }

    @Override
    public void avaluaComportament() {
        
        estat = estatCombat();
        if (memoria_old_posicio.x == estat.posicio.x && memoria_old_posicio.y == estat.posicio.y) {
            control++;
        } else {
            control = 0;
        }
        memoria_old_posicio = estat.posicio;
        if (control == 10 && (darrer_hyper_old - estat.temps) > 2000) {
            control = 0;
            hyperespai();
            darrer_hyper_old = estat.temps;

        }

        if ((estat.impactesRebuts > control_impactes) && (!estat.disparant)) {
            if ((temps - estat.temps) >= 5000 || temps != 0) {
                hyperespai();
                darrer_hyper_old = estat.temps;
            }
            control_impactes = estat.impactesRebuts;
        }

        if (espera > 0) {
            espera--;
        } else {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara

                if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5) {
                    if (estat.balaEnemigaDetectada) {
                        activaEscut();
                        temps = estat.temps;
                    }
                    if (estat.perforadores > 0) {
                        perforadora();
                    } else {
                        dispara();   //bloqueig per nau, no giris dispara
                    }   //bloqueig per nau, no giris dispara
                } else // hi ha un obstacle, gira i parteix
                {
//                    int total = Colisio(8);
//                    switch (total) {
//                        case 0:
//                            System.out.println("gira -20");
//                            gira(-25);
//                            break;
//                        case 1:
//                            System.out.println("gira 80");
//                            gira(190);
//                            break;
//                        default:
//                            System.out.println("gira 20");
//                            gira(20);
//                            break;
//                    }
                    // 20 graus
                    if (hiHaParedDavant(25)) {
                        enrere();
                    } else {
                        endavant();
                    }
                    espera = 3;
                }
            } else {

                if (estat.balaEnemigaDetectada) {
                    activaEscut();
                    temps = estat.temps;
                }
                endavant();
                int fm = funcioMina();
                if (estat.veigEnemic) {
                    if (estat.sector == 2 || estat.sector == 3) {
                        mira(estat.posicioEnemic.x, estat.posicioEnemic.y);
                    } else if (estat.sector == 1) {
                        dreta();
                    } else {
                        esquerra();
                    }
                    //Si hi ha un recurs a un radi a prop el detectara i girara cap a ell
                } else if (recursAprop()) {
                    if (memoria_old != memoria) {
                        mira(memoria.x, memoria.y);
                        memoria_old = memoria;
                    }
                    endavant();
                } else if (fm == 1) {
                    gira(-90);
                } else if (fm == 2) {
                    gira(90);
                }

                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant && estat.impactesRival < 5) {
                    if (estat.perforadores > 0) {
                        perforadora();
                    } else {
                        dispara();   //bloqueig per nau, no giris dispara
                    }
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
                            int colisio = Colisio(distancia);
                            if (colisio == 1) {
                                gira(-60);
                            } else if (colisio == 2) {
                                gira(60);
                            } else {
                                gira(90);
                            }
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

    boolean hiHaParedDavant(int dist) 
    {

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

    int Colisio(double dist) {
        double d = 0;
        double e = 0;
        double c = 0;
        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <= dist) {
            e = estat.distanciaVisors[ESQUERRA];
        }
        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] <= dist) {
            c = estat.distanciaVisors[CENTRAL];
        }
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] <= dist) {
            d = estat.distanciaVisors[DRETA];
        }
        if (e < d && c != 0) {
            return 1;
        } else if (e > d && c != 0) {
            return 2;
        } else {
            return 3;
        }
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

    boolean recursAprop() {
        marcarLinea();
        vector1 v = new vector1(900, 900, 900);
        for (int i = 0; i < marcar_linia.size(); i++) {
            if (v.recta > marcar_linia.get(i).recta) {
                v = marcar_linia.get(i);
            }
        }
        Punt p = new Punt(v.x, v.y);
        memoria = p;
        if (p.x != 900 & p.y != 900) {
            return true;
        }
        return false;
    }

    void marcarLinea() {
        for (int i = 0; i < marcar_linia.size(); i++) {
            marcar_linia.remove(i);
        }
        for (int i = 0; i < estat.bonificacions.length; i++) {
            double d = formula(estat.bonificacions[i].posicio.x, estat.bonificacions[i].posicio.y);
            if (estat.bonificacions[i].tipus != Agent.MINA && d < RADI && !hiHaParedDavant(20)) {
                vector1 v = new vector1(estat.bonificacions[i].posicio.x, estat.bonificacions[i].posicio.y, d);
                marcar_linia.add(v);
            }
        }
    }

    double formula(int x, int y
    ) {
        return Math.sqrt(((x - estat.posicio.x) * (x - estat.posicio.x)) + ((y - estat.posicio.y) * (y - estat.posicio.y)));
    }

    int funcioMina() {
        int x = estat.posicio.x;
        int y = estat.posicio.y;
        double angle = estat.angle;
        int d = 30;
        double h = 0;
        double m = 0;
        double ordenada_origen;
        double sol;

        for (int i = 0; i < estat.bonificacions.length; i++) {
            if (estat.bonificacions[i].tipus == Agent.MINA) {

                int x1 = estat.bonificacions[i].posicio.x;
                int y1 = estat.bonificacions[i].posicio.y;
                double a = (x1 - x);
                double b = (y - y1);
                m = Math.tan(angle);
                h = Math.sqrt((a * a) + (b * b));
//                if (angle < 90) {
//                    m = 90 - angle;
//                } else if (90 > angle && angle < 180) {
//                    m = 180 - angle;
//                } else if (180 > angle && angle < 270) {
//                    m = 270 - angle;
//                } else if (270 > angle && angle < 360) {
//                    m = 360 - angle;
//                }
                ordenada_origen = (y - (m * x));
                sol = (m * x1) + ordenada_origen;
                if (h < d && ((sol + 22.5) >= y1) && ((sol - 22.5) <= y1)) {
                    if (y1 < sol) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            }
        }
        return 0;

    }

}

class vector1 {

    int x;
    int y;
    double recta;

    public vector1(int x, int y, double recta) {
        this.x = x;
        this.y = y;
        this.recta = recta;
    }
}