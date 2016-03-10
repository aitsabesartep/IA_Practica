package agents;

import java.util.ArrayList;

public class Bitxo1 extends Agent {

    static final int PARET = 0;
    static final int NAU = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    static final int AMPLADA = 800;
    static final int ALTURA = 600;
    ArrayList<vector> marcar_linia = new ArrayList<vector>();
    static final int RADI = 40;
    static Punt memoria = new Punt(0, 0);
    static Punt memoria_old = new Punt(0, 0);
    static int control_impactes;

    Estat estat;
    int espera = 0;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1", "imatges/img1.png");
    }

    @Override
    public void inicia() {
        setAngleVisors(10);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(4);
        control_impactes = 0;
        espera = 0;
    }

    @Override
    public void avaluaComportament() {

        boolean enemic;
        enemic = false;
        int dir;
        estat = estatCombat();

        funcioMinaBo();

        if ((estat.impactesRebuts > control_impactes) && (!estat.disparant)) {
            hyperespai();
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
                }
                endavant();
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

    int Colisio(int dist) {
        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <= dist) {
            return 0;
        }

        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] <= dist) {
            return 1;
        }

        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] <= dist) {
            return 2;
        }
        return 3;
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
        vector v = new vector(900, 900, 900);
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
            if (estat.bonificacions[i].tipus != Agent.MINA && d < RADI) {
                vector v = new vector(estat.bonificacions[i].posicio.x, estat.bonificacions[i].posicio.y, d);
                marcar_linia.add(v);
            }
        }
    }

    double formula(int x, int y) {
        return Math.sqrt(((x - estat.posicio.x) * (x - estat.posicio.x)) + ((y - estat.posicio.y) * (y - estat.posicio.y)));
    }

    boolean funcioMinaBo() {
        int angle = (int) estat.angle;
        int x = estat.posicio.x;
        int y = estat.posicio.y;
        int d = 30;
        double alpha;
        double h;
        double tan;

        for (int i = 0; i < estat.bonificacions.length; i++) {
            if (estat.bonificacions[i].tipus == Agent.MINA) {

                int x1 = estat.bonificacions[i].posicio.x;
                int y1 = estat.bonificacions[i].posicio.y;

                if (x1 < x && y1 < y) {
                    double a = (x - x1);
                    double b = (y - y1);
                    h = Math.sqrt((a * a) + (b * b));
                    tan = (a / b);
                    alpha = Math.atan(tan);
                    alpha = Math.toDegrees(alpha);
                    System.out.println("-- Aplha: " + alpha);
                    alpha = 90 - alpha;
                } else if (x1 < x && y1 > y) {
                    double a = (x - x1);
                    double b = (y1 - y);
                    h = Math.sqrt((a * a) + (b * b));
                    tan = (a / b);
                    alpha = Math.atan(tan);
                    alpha = Math.toDegrees(alpha);
                    System.out.println("-- Aplha: " + alpha);
                    alpha = 90 + alpha;
                } else if (x1 > x && y1 > y) {
                    double a = (x1 - x);
                    double b = (y1 - y);
                    h = Math.sqrt((a * a) + (b * b));
                    tan = (a / b);
                    alpha = Math.atan(tan);
                    alpha = Math.toDegrees(alpha);
                    System.out.println("-- Aplha: " + alpha);
                    alpha = 270 - alpha;
                } else {
                    double a = (x1 - x);
                    double b = (y - y1);
                    h = Math.sqrt((a * a) + (b * b));
                    tan = (a / b);
                    alpha = Math.atan(tan);
                    alpha = Math.toDegrees(alpha);
                    System.out.println("-- Aplha: " + alpha);
                    alpha = 270 + alpha;
                }
                System.out.println("ALPHA BO: " + alpha);
                System.out.println("Angle tanque: " + angle);
                if ((h < d) && (angle > (alpha - 2)) && (angle < (alpha + 2))) {
                    System.out.println("TRUE");
//                    return true;
                }
            }
        }
        return false;

    }
}

class vector {

    int x;
    int y;
    double recta;

    public vector(int x, int y, double recta) {
        this.x = x;
        this.y = y;
        this.recta = recta;
    }
}
