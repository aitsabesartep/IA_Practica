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
        double beta = 0;
        for (int i = 0; i < estat.bonificacions.length; i++) {
            if (estat.bonificacions[i].tipus == Agent.MINA) {
                double a = (x - estat.bonificacions[i].posicio.x);
                double b = (y - estat.bonificacions[i].posicio.y);
                double h = Math.sqrt((a * a) + (b * b));
                double alpha = Math.asin(a / h);
                System.out.println(alpha);
                if (alpha < 90) {
                    beta = 90 - alpha;
                } else if(alpha < 180){
                    beta = 180 - alpha;
                } else if(alpha < 270){
                    beta = 270 - alpha;
                } else{
                    beta = 360 - alpha;
                }
                System.out.println(angle);
                if (h < d && (angle > alpha - 2) && (angle < alpha + 2)) {
                    System.out.println("TRUE");
                    return true;
                }
            }
        }
        return false;

    }

    boolean funcioMina() {
        //controlar si angle = 90, 0, 270, 180.
        int angle = (int) estat.angle + 90;
        if (angle >= 360) {
            angle = angle - 360;
        }
        int p = 40;
        int x = estat.posicio.x;
        int y = estat.posicio.y;
//        System.out.println(x +" | " +y);

        double x2 = 0, y2 = 0, x3 = 0, y3 = 0, x4 = 0, y4 = 0, x5 = 0, y5 = 0;

        if (angle == 90) {
            x2 = x + 12.5;
            y2 = y - 12.5;
            x3 = x + 12.5;
            y3 = y - 12.5 - p;
            x4 = x - 12.5;
            y4 = y - 12.5;
            x5 = x - 12.5;
            y5 = y - 12.5 - p;
        } else if (angle == 180) {
            x2 = x - 12.5;
            y2 = y - 12.5;
            x3 = x - 12.5 - p;
            y3 = y - 12.5;
            x4 = x - 12.5;
            y4 = y + 12.5;
            x5 = x - 12.5 - p;
            y5 = y + 12.5;
        } else if (angle == 270) {
            x2 = x - 12.5;
            y2 = y + 12.5;
            x3 = x - 12.5;
            y3 = y + 12.5 + p;
            x4 = x + 12.5;
            y4 = y + 12.5;
            x5 = x + 12.5;
            y5 = y + 12.5 + p;

        } else if (angle == 0) {
            x2 = x + 12.5;
            y2 = y + 12.5;
            x3 = x + 12.5 + p;
            y3 = y + 12.5;
            x4 = x + 12.5;
            y4 = y - 12.5;
            x5 = x + 12.5 + p;
            y5 = y - 12.5;
        } else {
            double l = (12.5 / Math.tan(angle));
            x2 = (x + (12.5 / Math.sin(angle)) + ((12.5 - l) * Math.cos(angle)));
            y2 = (y + ((12.5 - l) * Math.sin(angle)));
            x3 = (x2 + (p * Math.cos(angle)));
            y3 = (y2 + (p * Math.sin(angle)));
            x4 = (x - (12.5 / Math.sin(angle)) + ((12.5 + l) * Math.cos(angle)));
            y4 = (y + ((12.5 + l) * Math.sin(angle)));
            x5 = (x4 + (p * Math.cos(angle)));
            y5 = (y4 + (p * Math.sin(angle)));
            System.out.println("Posicio robot: " + x + "   " + y);
            System.out.println(x2 + " , " + y2 + " | " + x3 + " , " + y3 + " | " + x4 + " , " + y4 + " | " + x5 + " , " + y5);
        }

        double xmin = Math.min(Math.min(x2, x3), Math.min(x4, x5));
        double xmax = Math.max(Math.max(x2, x3), Math.max(x4, x5));
        double ymin = Math.min(Math.min(y2, y3), Math.min(y4, y5));
        double ymax = Math.max(Math.max(y2, y3), Math.max(y4, y5));

        for (int i = 0; i < estat.bonificacions.length; i++) {
            if (estat.bonificacions[i].tipus == Agent.MINA && (xmin <= estat.bonificacions[i].posicio.x) && (xmax >= estat.bonificacions[i].posicio.x) && (ymin <= estat.bonificacions[i].posicio.y) && (ymax >= estat.bonificacions[i].posicio.y)) {
                System.out.println("TRUE");
                return true;
            }
        }
//        System.out.println("FALSE");
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
