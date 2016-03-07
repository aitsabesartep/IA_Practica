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
    static final int RADI = 46;
    static Punt memoria = new Punt(0, 0);
    static Punt memoria_old = new Punt(0, 0);
    static int control_impactes;

    Estat estat;
    int espera = 0;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1", "imatges/dibuix.png");
    }

    @Override
    public void inicia() {
        setAngleVisors(10);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(6);
        control_impactes = 0;
        espera = 0;
    }

    @Override
    public void avaluaComportament() {

        boolean enemic;

        enemic = false;

        int dir;

        estat = estatCombat();
        
        if ((estat.impactesRebuts > control_impactes)&&(!estat.disparant)) {
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
                    gira(25); // 20 graus
                    if (hiHaParedDavant(20)) {
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

    public double formula(int x, int y) {
        return Math.sqrt(((x - estat.posicio.x) * (x - estat.posicio.x)) + ((y - estat.posicio.y) * (y - estat.posicio.y)));
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getRecta() {
        return recta;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setRecta(double recta) {
        this.recta = recta;
    }

}
