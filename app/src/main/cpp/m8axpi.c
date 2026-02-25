#include <assert.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <gmp.h>
#include <android/log.h>

#define A 13591409
#define B 545140134
#define C 640320
#define D 12
#define BITS_PER_DIGIT 3.32192809488736234787
#define DIGITS_PER_ITER 14.1816474627254776555
#define DOUBLE_PREC 53
#define printf(...) __android_log_print(ANDROID_LOG_INFO, "M8AX_MOTOR", __VA_ARGS__)
#define puts(str) __android_log_print(ANDROID_LOG_INFO, "M8AX_MOTOR", "%s", str)
long double termis;
long tiempoa, tiempob, coco2, min;
double paginas = 1500.0, quijote = 2034611.0;
char ultimos1000[1001];
int idx;
#if CHECK_MEMUSAGE
#undef CHECK_MEMUSAGE
#define CHECK_MEMUSAGE
#else
#undef CHECK_MEMUSAGE
#define CHECK_MEMUSAGE
#endif
#if !defined(__sun) && (defined(USG) || defined(__SVR4) || defined(_UNICOS) || defined(__hpux))
static int
m8ax_tiempo_cpu()
{
    return (int)((double)clock() * 1000 / CLOCKS_PER_SEC);
}
#else

#include <sys/types.h>
#include <sys/time.h>
#include <sys/resource.h>

long static m8ax_tiempo_cpu() {
    struct rusage rus;
    getrusage(RUSAGE_SELF, &rus);
    return (long) rus.ru_utime.tv_sec * 1000 + (long) rus.ru_utime.tv_usec / 1000;
}

#endif

char *convertirARomano(int num) {
    static char romano[20];
    struct {
        int valor;
        char *letra;
    } tabla[] = {
            {1000, "M"},
            {900,  "CM"},
            {500,  "D"},
            {400,  "CD"},
            {100,  "C"},
            {90,   "XC"},
            {50,   "L"},
            {40,   "XL"},
            {10,   "X"},
            {9,    "IX"},
            {5,    "V"},
            {4,    "IV"},
            {1,    "I"}
    };
    romano[0] = '\0';
    for (int i = 0; i < 13; i++) {
        while (num >= tabla[i].valor) {
            strcat(romano, tabla[i].letra);
            num -= tabla[i].valor;
        }
    }
    return romano;
}

static mpf_t t1, t2;

static void
mi_lugar_del_sqrt(mpf_t r, unsigned long x) {
    unsigned long prec, bits, prec0;
    prec0 = mpf_get_prec(r);
    if (prec0 <= DOUBLE_PREC) {
        mpf_set_d(r, sqrt(x));
        return;
    }
    bits = 0;
    for (prec = prec0; prec > DOUBLE_PREC;) {
        int bit = prec & 1;
        prec = (prec + bit) / 2;
        bits = bits * 2 + bit;
    }
    mpf_set_prec_raw(t1, DOUBLE_PREC);
    mpf_set_d(t1, 1 / sqrt(x));
    while (prec < prec0) {
        prec *= 2;
        if (prec < prec0) {
            mpf_set_prec_raw(t2, prec);
            mpf_mul(t2, t1, t1);
            mpf_mul_ui(t2, t2, x);
            mpf_ui_sub(t2, 1, t2);
            mpf_set_prec_raw(t2, prec / 2);
            mpf_div_2exp(t2, t2, 1);
            mpf_mul(t2, t2, t1);
            mpf_set_prec_raw(t1, prec);
            mpf_add(t1, t1, t2);
        } else {
            break;
        }
        prec -= (bits & 1);
        bits /= 2;
    }
    mpf_set_prec_raw(t2, prec0 / 2);
    mpf_mul_ui(t2, t1, x);
    mpf_mul(r, t2, t2);
    mpf_ui_sub(r, x, r);
    mpf_mul(t1, t1, r);
    mpf_div_2exp(t1, t1, 1);
    mpf_add(r, t1, t2);
}

#if __GMP_MP_RELEASE >= 50001
#define m8ax_mis_divisiones mpf_div
#else

static void
m8ax_mis_divisiones(mpf_t r, mpf_t y, mpf_t x) {
    unsigned long prec, bits, prec0;
    prec0 = mpf_get_prec(r);
    if (prec0 <= DOUBLE_PREC) {
        mpf_set_d(r, mpf_get_d(y) / mpf_get_d(x));
        return;
    }
    bits = 0;
    for (prec = prec0; prec > DOUBLE_PREC;) {
        int bit = prec & 1;
        prec = (prec + bit) / 2;
        bits = bits * 2 + bit;
    }
    mpf_set_prec_raw(t1, DOUBLE_PREC);
    mpf_ui_div(t1, 1, x);
    while (prec < prec0) {
        prec *= 2;
        if (prec < prec0) {
            mpf_set_prec_raw(t2, prec);
            mpf_mul(t2, x, t1);
            mpf_ui_sub(t2, 1, t2);
            mpf_set_prec_raw(t2, prec / 2);
            mpf_mul(t2, t2, t1);
            mpf_set_prec_raw(t1, prec);
            mpf_add(t1, t1, t2);
        } else {
            prec = prec0;
            mpf_set_prec_raw(t2, prec / 2);
            mpf_mul(t2, t1, y);
            mpf_mul(r, x, t2);
            mpf_sub(r, y, r);
            mpf_mul(t1, t1, r);
            mpf_add(r, t1, t2);
            break;
        }
        prec -= (bits & 1);
        bits /= 2;
    }
}

#endif
#define min(x, y) ((x) < (y) ? (x) : (y))
#define max(x, y) ((x) > (y) ? (x) : (y))
typedef struct {
    unsigned long max_facs;
    unsigned long num_facs;
    unsigned long *fac;
    unsigned long *pow;
} fac_t[1];
typedef struct {
    long int fac;
    long int pow;
    long int nxt;
} cribam8ax_t;
static cribam8ax_t *m8ax_criba;
static long int m8ax_criba_size;
static fac_t ftmp, fmul;
#define INIT_FACS 32

static void
m8ax_mostrar_fac(fac_t f) {
    long int i;
    for (i = 0; i < f[0].num_facs; i++)
        if (f[0].pow[i] == 1)
            printf("( %ld ) ", f[0].fac[i]);
        else
            printf("( %ld^%ld ) ", f[0].fac[i], f[0].pow[i]);
    printf("\n");
}

static void
m8ax_reset_fac(fac_t f) {
    f[0].num_facs = 0;
}

static void
m8ax_fac_inicia(fac_t f, long int s) {
    if (s < INIT_FACS)
        s = INIT_FACS;
    f[0].fac = malloc(s * sizeof(unsigned long) * 2);
    f[0].pow = f[0].fac + s;
    f[0].max_facs = s;
    m8ax_reset_fac(f);
}

static void
m8ax_fac_ini(fac_t f) {
    m8ax_fac_inicia(f, INIT_FACS);
}

static void
m8ax_fac_limpia(fac_t f) {
    free(f[0].fac);
}

static void
m8ax_fac_redimensiona(fac_t f, long int s) {
    if (f[0].max_facs < s) {
        m8ax_fac_limpia(f);
        m8ax_fac_inicia(f, s);
    }
}

static void
m8ax_fac_setea_bp(fac_t f, unsigned long base, long int pow) {
    long int i;
    assert(base < m8ax_criba_size);
    for (i = 0, base /= 2; base > 0; i++, base = m8ax_criba[base].nxt) {
        f[0].fac[i] = m8ax_criba[base].fac;
        f[0].pow[i] = m8ax_criba[base].pow * pow;
    }
    f[0].num_facs = i;
    assert(i <= f[0].max_facs);
}

static void
m8ax_m8ax_fac_mul2(fac_t r, fac_t f, fac_t g) {
    long int i, j, k;
    for (i = j = k = 0; i < f[0].num_facs && j < g[0].num_facs; k++) {
        if (f[0].fac[i] == g[0].fac[j]) {
            r[0].fac[k] = f[0].fac[i];
            r[0].pow[k] = f[0].pow[i] + g[0].pow[j];
            i++;
            j++;
        } else if (f[0].fac[i] < g[0].fac[j]) {
            r[0].fac[k] = f[0].fac[i];
            r[0].pow[k] = f[0].pow[i];
            i++;
        } else {
            r[0].fac[k] = g[0].fac[j];
            r[0].pow[k] = g[0].pow[j];
            j++;
        }
    }
    for (; i < f[0].num_facs; i++, k++) {
        r[0].fac[k] = f[0].fac[i];
        r[0].pow[k] = f[0].pow[i];
    }
    for (; j < g[0].num_facs; j++, k++) {
        r[0].fac[k] = g[0].fac[j];
        r[0].pow[k] = g[0].pow[j];
    }
    r[0].num_facs = k;
    assert(k <= r[0].max_facs);
}

static void
m8ax_fac_mul(fac_t f, fac_t g) {
    fac_t tmp;
    m8ax_fac_redimensiona(fmul, f[0].num_facs + g[0].num_facs);
    m8ax_m8ax_fac_mul2(fmul, f, g);
    tmp[0] = f[0];
    f[0] = fmul[0];
    fmul[0] = tmp[0];
}

static void
m8ax_fac_mul_bp(fac_t f, unsigned long base, unsigned long pow) {
    m8ax_fac_setea_bp(ftmp, base, pow);
    m8ax_fac_mul(f, ftmp);
}

static void
m8ax_compacta_fac(fac_t f) {
    long int i, j;
    for (i = 0, j = 0; i < f[0].num_facs; i++) {
        if (f[0].pow[i] > 0) {
            if (j < i) {
                f[0].fac[j] = f[0].fac[i];
                f[0].pow[j] = f[0].pow[i];
            }
            j++;
        }
    }
    f[0].num_facs = j;
}

static void
m8ax_bs_mul(mpz_t r, long int a, long int b) {
    long int i, j;
    if (b - a <= 32) {
        mpz_set_ui(r, 1);
        for (i = a; i < b; i++)
            for (j = 0; j < fmul[0].pow[i]; j++)
                mpz_mul_ui(r, r, fmul[0].fac[i]);
    } else {
        mpz_t r2;
        mpz_init(r2);
        m8ax_bs_mul(r2, a, (a + b) / 2);
        m8ax_bs_mul(r, (a + b) / 2, b);
        mpz_mul(r, r, r2);
        mpz_clear(r2);
    }
}

static mpz_t gcd;
#if HAVE_DIVEXACT_PREINV
static mpz_t mgcd;
void mpz_invert_mod_2exp(mpz_ptr, mpz_srcptr);
void mpz_divexact_pre(mpz_ptr, mpz_srcptr, mpz_srcptr, mpz_srcptr);
#endif

static void
m8ax_fac_elimina_gcd(mpz_t p, fac_t fp, mpz_t g, fac_t fg) {
    long int i, j, k, c;
    m8ax_fac_redimensiona(fmul, min(fp->num_facs, fg->num_facs));
    for (i = j = k = 0; i < fp->num_facs && j < fg->num_facs;) {
        if (fp->fac[i] == fg->fac[j]) {
            c = min(fp->pow[i], fg->pow[j]);
            fp->pow[i] -= c;
            fg->pow[j] -= c;
            fmul->fac[k] = fp->fac[i];
            fmul->pow[k] = c;
            i++;
            j++;
            k++;
        } else if (fp->fac[i] < fg->fac[j]) {
            i++;
        } else {
            j++;
        }
    }
    fmul->num_facs = k;
    assert(k <= fmul->max_facs);
    if (fmul->num_facs) {
        m8ax_bs_mul(gcd, 0, fmul->num_facs);
#if HAVE_DIVEXACT_PREINV
        mpz_invert_mod_2exp(mgcd, gcd);
        mpz_divexact_pre(p, p, gcd, mgcd);
        mpz_divexact_pre(g, g, gcd, mgcd);
#else
#define SIZ(x) x->_mp_size
        mpz_divexact(p, p, gcd);
        mpz_divexact(g, g, gcd);
#endif
        m8ax_compacta_fac(fp);
        m8ax_compacta_fac(fg);
    }
}

int out = 0;
static mpz_t *pstack, *qstack, *gstack;
static fac_t *fpstack, *fgstack;
static long int top = 0;
static double progress = 0, percent;
#define p1 (pstack[top])
#define q1 (qstack[top])
#define g1 (gstack[top])
#define fp1 (fpstack[top])
#define fg1 (fgstack[top])
#define p2 (pstack[top + 1])
#define q2 (qstack[top + 1])
#define g2 (gstack[top + 1])
#define fp2 (fpstack[top + 1])
#define fg2 (fgstack[top + 1])
static long gcd_time = 0;

static void
mviiiax_bs(unsigned long a, unsigned long b, unsigned gflag, long int level) {
    unsigned long i, mid;
    long int ccc, HORY, MINY, SEGY, HORY1, MINY1, SEGY1;
    float TIEMPI, tanto, tanto2, TIEMPY1, TIEMPY;
    if (b - a == 1) {
        mpz_set_ui(p1, b);
        mpz_mul_ui(p1, p1, b);
        mpz_mul_ui(p1, p1, b);
        mpz_mul_ui(p1, p1, (C / 24) * (C / 24));
        mpz_mul_ui(p1, p1, C * 24);
        mpz_set_ui(g1, 2 * b - 1);
        mpz_mul_ui(g1, g1, 6 * b - 1);
        mpz_mul_ui(g1, g1, 6 * b - 5);
        mpz_set_ui(q1, b);
        mpz_mul_ui(q1, q1, B);
        mpz_add_ui(q1, q1, A);
        mpz_mul(q1, q1, g1);
        if (b % 2)
            mpz_neg(q1, q1);
        i = b;
        while ((i & 1) == 0)
            i >>= 1;
        m8ax_fac_setea_bp(fp1, i, 3);
        m8ax_fac_mul_bp(fp1, 3 * 5 * 23 * 29, 3);
        fp1[0].pow[0]--;
        m8ax_fac_setea_bp(fg1, 2 * b - 1, 1);
        m8ax_fac_mul_bp(fg1, 6 * b - 1, 1);
        m8ax_fac_mul_bp(fg1, 6 * b - 5, 1);
        if (b > (int) (progress)) {
            fflush(stdout);
            progress += percent * 2;
            tiempob = m8ax_tiempo_cpu();
            TIEMPY = ((double) tiempob - tiempoa) / 1000;
            HORY = (TIEMPY / 3600);
            MINY = fmod((TIEMPY / 60), 60);
            SEGY = fmod(TIEMPY, 60);
            tanto = (progress * 100) / termis;
            tanto2 = ((100.00 - tanto) * TIEMPY) / tanto;
            TIEMPY1 = tanto2;
            HORY1 = (TIEMPY1 / 3600);
            MINY1 = fmod((TIEMPY1 / 60), 60);
            SEGY1 = fmod(TIEMPY1, 60);
            if (tanto <= 100)
                printf("\nM8AX - %.f%c - TEC - %.0f / DEC - %.0f - %lih %lim %lis - ETA - %lih %lim %lis",
                       tanto, 37, progress, progress * DIGITS_PER_ITER, HORY, MINY, SEGY, HORY1,
                       MINY1, SEGY1);
        }
    } else {
        mid = a + (b - a) * 0.5224;
        mviiiax_bs(a, mid, 1, level + 1);
        top++;
        mviiiax_bs(mid, b, gflag, level + 1);
        top--;
        if (level == 0) {
            tiempob = m8ax_tiempo_cpu();
            TIEMPY = ((double) tiempob - tiempoa) / 1000;
            HORY = (TIEMPY / 3600);
            MINY = fmod((TIEMPY / 60), 60);
            SEGY = fmod(TIEMPY, 60);
            tanto = (progress * 100) / termis;
            tanto2 = ((100.00 - tanto) * TIEMPY) / tanto;
            TIEMPY1 = tanto2;
            HORY1 = (TIEMPY1 / 3600);
            MINY1 = fmod((TIEMPY1 / 60), 60);
            SEGY1 = fmod(TIEMPY1, 60);
            if (tanto <= 100)
                printf("\nM8AX - %.f%c - TEC - %.0f / DEC - %.0f - %lih %lim %lis - ETA - %lih %lim %lis",
                       tanto, 37, progress, progress * DIGITS_PER_ITER, HORY, MINY, SEGY, HORY1,
                       MINY1, SEGY1);
            puts("");
            printf("\n\nM8AX - [ División Binaria Completada En - %.5f Segundos. ] - M8AX",
                   ((double) (tiempob - coco2)) / 1000);
        }
        ccc = level == 0;
        if (ccc)
                CHECK_MEMUSAGE;
        if (level >= 4) {
#if 0
            long t = m8ax_tiempo_cpu();
#endif
            m8ax_fac_elimina_gcd(p2, fp2, g1, fg1);
#if 0
            gcd_time += m8ax_tiempo_cpu()-t;
#endif
        }
        if (ccc)
                CHECK_MEMUSAGE;
        mpz_mul(p1, p1, p2);
        if (ccc)
                CHECK_MEMUSAGE;
        mpz_mul(q1, q1, p2);
        if (ccc)
                CHECK_MEMUSAGE;
        mpz_mul(q2, q2, g1);
        if (ccc)
                CHECK_MEMUSAGE;
        mpz_add(q1, q1, q2);
        if (ccc)
                CHECK_MEMUSAGE;
        m8ax_fac_mul(fp1, fp2);
        if (gflag) {
            mpz_mul(g1, g1, g2);
            m8ax_fac_mul(fg1, fg2);
        }
    }
    if (out & 2) {
        printf("M8AX - [ p( %ld,%ld ) ] - ", a, b);
        m8ax_mostrar_fac(fp1);
        if (gflag) {
            printf("M8AX - [ g( %ld,%ld ) ] - ", a, b);
            m8ax_mostrar_fac(fg1);
        }
    }
}

static void
construye_m8ax_criba(long int n, cribam8ax_t *s) {
    long int m, i, j, k;
    m8ax_criba_size = n;
    m = (long int) sqrt(n);
    memset(s, 0, sizeof(cribam8ax_t) * n / 2);
    s[1 / 2].fac = 1;
    s[1 / 2].pow = 1;
    for (i = 3; i <= n; i += 2) {
        if (s[i / 2].fac == 0) {
            s[i / 2].fac = i;
            s[i / 2].pow = 1;
            if (i <= m) {
                for (j = i * i, k = i / 2; j <= n; j += i + i, k++) {
                    if (s[j / 2].fac == 0) {
                        s[j / 2].fac = i;
                        if (s[k].fac == i) {
                            s[j / 2].pow = s[k].pow + 1;
                            s[j / 2].nxt = s[k].nxt;
                        } else {
                            s[j / 2].pow = 1;
                            s[j / 2].nxt = k;
                        }
                    }
                }
            }
        }
    }
}

int calcular_pi(long int d, int out) {
    mpf_t pi, qi;
    long int i, depth = 1, maxi, minim, sumd = 0, terms, numeris[256];
    unsigned long psize, qsize;
    float megas, indv, hum, hum2 = 0, pcen, ab, bc;
    double chi_square = 0, esperado;
    long begin, mid0, mid1, mid2, mid3, mid4, end;
    int letra, ju, ja, jar;
    progress = 0;
    percent = 0;
    termis = 0;
    sumd = 0;
    top = 0;
    gcd_time = 0;
    tiempoa = 0;
    tiempob = 0;
    coco2 = 0;
    puts("MvIiIaX - Cálculo De Decimales De PI - MvIiIaX");
    puts("");
    puts("Al Finalizar El Cálculo, El Programa Nos Dará Un Índice De Velocidad,");
    puts("Para Obtener Un Índice De Velocidad Real Y Poder Comparar Tu Móvil,");
    puts("Se Recomienda Calcular Al Menos 10.000.000 (10M) De Decimales.");
    puts("Este Índice Tendrá Una Referencia De 100 Puntos Si Logra 10M En 1 Seg.");
    puts("Por Ejemplo: S4 Mini (1.285 pts) | Mi 9 Lite (5.506 pts) | ¿ Y Tu Móvil ? |");
    puts("Calcula Ahora 10M De Decimales... ¿ Qué Puntuación Sacas ?");
    puts("");
    puts("Te Puede Servir A Modo De BenchMark En Tus Equipos Móviles Ó Para");
    puts("Competir Con Tus Amigos... A Ver Quién Tiene El Mejor Móvil.");
    puts("A Más Decimales Más Trabajo Cuesta... Este Programa Hace Un Uso");
    puts("Intensivo De La CPU Y La Memoria RAM, Así Que Cuanto Más Rápidos Sean,");
    puts("Más Rápidos Serán Los Cálculos...");
    puts("");
    puts("Si El Programa Se Interrumpe Por Sí Solo, Lo Más Probable, Es Que");
    puts("Has Puesto A Calcular Un Gran Número De Decimales De Pi Y Tu");
    puts("Dispositivo No Tiene Tanta Memoria RAM Para Calcularlos, Así Que");
    puts("Tendrás Que Bajar El Número De Decimales Un Poco.");
    puts("El Programa Desactiva Los Botones No Compatibles Con Tu Smartphone");
    puts("De Todas Maneras Un Pico De Ram, Puede Causar El Cierre Si Estás Al Límite.");
    puts("");
    puts("¿ Sabías Que El Consumo De RAM Es Progresivo Y No Lineal ?");
    puts("Calcular 4.5 Millones De Decimales Requiere 72Mb De RAM, Pero");
    puts("Al Doblar Los Dígitos, El Consumo Se Multiplica Exponencialmente.");
    puts("Para 400 Millones Se Superan Los 5.4GB De Memoria Real.");
    puts("");
    puts("¿ Tiene Tu Dispositivo RAM Suficiente O Saltará El OOM Killer ?");
    puts("Bloqueo De Seguridad Activado Según La RAM De Tu Sistema.");
    puts("");
    puts("Una Cosa Más, Si Tu Dispositivo Tiene 8Gb De RAM, No Quiere Decir Que");
    puts("8Gb De RAM Esté Disponible Para Este Programa, Ya Que El Propio");
    puts("Sistema Operativo Ya Consume Parte De Esa Memoria, Aplicaciones");
    puts("En Segundo Plano, Etc...");
    puts("");
    puts("Las Estadísticas De Quijotes Y Páginas A5, Se Refieren A Que Como");
    puts("Cada Decimal De Pi Es Como Si Fuera Una Letra, Pues El Libro Del");
    puts("Quijote Tiene Unos 2M De Caracteres Y Una Página Unos 1500.");
    puts("De Ahí, Pues Saco Estadísticas Curiosas.");
    puts("");
    if (out == 3)
        out = 8;
    terms = d / DIGITS_PER_ITER;
    termis = terms;
    megas = (d * 0.141) / 10000;
    printf("------------------------------------------------------------------------\n");
    printf("M8AX - [ Cálculo De %li Dígitos De PI - M.RAM - %.2lf MB ] - M8AX\n", d, megas);
    printf("------------------------------------------------------------------------\n");
    printf("\nM8AX - [ Comenzando El Trabajo. CPU & RAM Preparados. ] - M8AX\n");
    printf("M8AX - [ Algorítmo Del Cálculo - Fórmula De ChudNovSky. ] - M8AX\n");
    while ((1L << depth) < terms)
        depth++;
    depth++;
    percent = terms / 100.0;
    printf("M8AX - [ Número De Términos. - %ld. Profundidad FFT. - %ld. ] - M8AX\n", terms, depth);
    begin = m8ax_tiempo_cpu();
    printf("\n------------------------------------------------------------------------\n");
    m8ax_criba_size = max(3 * 5 * 23 * 29 + 1, terms * 6);
    m8ax_criba = (cribam8ax_t *) malloc(sizeof(cribam8ax_t) * m8ax_criba_size / 2);
    construye_m8ax_criba(m8ax_criba_size, m8ax_criba);
    mid0 = m8ax_tiempo_cpu();
    printf("M8AX - [ Criba De ChudNovSky. - %.5f Segundos. ] - M8AX\n",
           (double) (mid0 - begin) / 1000);
    printf("------------------------------------------------------------------------\n");
    puts("");
    pstack = malloc(sizeof(mpz_t) * depth);
    qstack = malloc(sizeof(mpz_t) * depth);
    gstack = malloc(sizeof(mpz_t) * depth);
    fpstack = malloc(sizeof(fac_t) * depth);
    fgstack = malloc(sizeof(fac_t) * depth);
    for (i = 0; i < depth; i++) {
        mpz_init(pstack[i]);
        mpz_init(qstack[i]);
        mpz_init(gstack[i]);
        m8ax_fac_ini(fpstack[i]);
        m8ax_fac_ini(fgstack[i]);
    }
    mpz_init(gcd);
#if HAVE_DIVEXACT_PREINV
    mpz_init(mgcd);
#endif
    m8ax_fac_ini(ftmp);
    m8ax_fac_ini(fmul);
    if (terms <= 0) {
        mpz_set_ui(p2, 1);
        mpz_set_ui(q2, 0);
        mpz_set_ui(g2, 1);
    } else {
        tiempoa = m8ax_tiempo_cpu();
        mviiiax_bs(0, terms, 0, 0);
    }
    mid1 = m8ax_tiempo_cpu();
    coco2 = m8ax_tiempo_cpu();
    printf("\nM8AX - [ Sumando Series. %ld Terminos. - %.5f Segundos. ] - M8AX\n", terms,
           (double) (mid1 - tiempob) / 1000);
    printf("M8AX - [ Máximo Común Divisor. - %.5f Segundos. ] - M8AX\n",
           (double) (gcd_time) / 1000);
    free(m8ax_criba);
#if HAVE_DIVEXACT_PREINV
    mpz_clear(mgcd);
#endif
    mpz_clear(gcd);
    m8ax_fac_limpia(ftmp);
    m8ax_fac_limpia(fmul);
    for (i = 1; i < depth; i++) {
        mpz_clear(pstack[i]);
        mpz_clear(qstack[i]);
        mpz_clear(gstack[i]);
        m8ax_fac_limpia(fpstack[i]);
        m8ax_fac_limpia(fgstack[i]);
    }
    mpz_clear(gstack[0]);
    m8ax_fac_limpia(fpstack[0]);
    m8ax_fac_limpia(fgstack[0]);
    free(gstack);
    free(fpstack);
    free(fgstack);
    mpf_set_default_prec((long int) (d * BITS_PER_DIGIT + 16));
    psize = mpz_sizeinbase(p1, 10);
    qsize = mpz_sizeinbase(q1, 10);
    mpz_addmul_ui(q1, p1, A);
    mpz_mul_ui(p1, p1, C / D);
    mpf_init(pi);
    mpf_set_z(pi, p1);
    mpz_clear(p1);
    mpf_init(qi);
    mpf_set_z(qi, q1);
    mpz_clear(q1);
    free(pstack);
    free(qstack);
    mid2 = m8ax_tiempo_cpu();
    mpf_init(t1);
    mpf_init(t2);
    m8ax_mis_divisiones(qi, pi, qi);
    mid3 = m8ax_tiempo_cpu();
    printf("M8AX - [ Haciendo Divisiones. - %.5f Segundos. ] - M8AX\n",
           (double) (mid3 - mid2) / 1000);
    mi_lugar_del_sqrt(pi, C);
    mid4 = m8ax_tiempo_cpu();
    printf("M8AX - [ Haciendo Raíces Cuadradas Inversas. - %.5f Segundos. ] - M8AX\n",
           (double) (mid4 - mid3) / 1000);
    mpf_mul(qi, qi, pi);
    end = m8ax_tiempo_cpu();
    printf("M8AX - [ Haciendo Multiplicaciones. - %.5f Segundos. ] - M8AX\n",
           (double) (end - mid4) / 1000);
    printf("M8AX - [ Tiempo Total Del Cálculo. - %.5f Segundos. ] - M8AX\n\n",
           (double) (end - begin) / 1000);
    fflush(stdout);
    printf("M8AX - [ Tamaño De P. - %ld Dígitos. ( %f ) ] - M8AX\n"
           "M8AX - [ Tamaño De Q. - %ld Dígitos. ( %f ) ] - M8AX\n\n",
           psize, (double) psize / d, qsize, (double) qsize / d);
    printf("M8AX - [ Decimales De PI Calculados. - %li Decimales. ]\n", d);
    double tiempo_total_seg = (double) (end - begin) / 1000.0;
    double q_total = (double) d / quijote;
    double p_total = (double) d / paginas;
    double q_seg = q_total / tiempo_total_seg;
    double p_seg = p_total / tiempo_total_seg;
    double d_seg = (double) d / tiempo_total_seg;
    double v_kmh = (((double) d * 0.25100377) / 100000.0) / (tiempo_total_seg / 3600.0);
    double l_seg = ((double) d * 0.0512) / 1000.0 / tiempo_total_seg;
    long int t_s = (long int) tiempo_total_seg;
    long int anoss = t_s / 31536000;
    long int diass = (t_s % 31536000) / 86400;
    long int horass = ((t_s % 31536000) % 86400) / 3600;
    long int minn = (((t_s % 31536000) % 86400) % 3600) / 60;
    long int segg = (((t_s % 31536000) % 86400) % 3600) % 60;
    printf("M8AX - [ Tiempo Total Del Cálculo. - %lia %lid %lih %lim %lis. ]\n", anoss, diass,
           horass, minn, segg);
    printf("M8AX - [ Libros Del Quijote Procesados. - %.3f Libros. ]\n",
           (double) d / (double) quijote);
    printf("M8AX - [ Páginas A5 Del Quijote Procesadas. - %.3f Páginas. ]\n",
           (double) d / (double) paginas);
    printf("M8AX - [ Quijotes Por Segundo Procesados. - %.3f Quijotes/Seg. ]\n",
           q_seg);
    printf("M8AX - [ Páginas A5 Por Segundo. - %.3f PgsA5/Seg. ]\n",
           p_seg);
    printf("M8AX - [ Decimales De Pi/Seg. - %.3f DecPi/Seg. ]\n",
           d_seg);
    hum = (d / 2) / 86400.000;
    if (hum < 1) {
        hum = ((d / 2) / 86400.000) * 1440;
        hum2 = 1;
        printf("M8AX - [ Minutos Leyendo Los Decimales En Voz Alta - %.5f m. ]\n", hum);
    }
    if (hum2 == 0)
        printf("M8AX - [ Días Leyendo Los Decimales En Voz Alta - %.5f d. ]\n", hum);
    double metrosH = (double) d * 0.0000000001;
    if (metrosH < 0.001) {
        printf("M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f mm. ]\n", metrosH * 1000.0);
    } else if (metrosH < 1.0) {
        printf("M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f cm. ]\n", metrosH * 100.0);
    } else if (metrosH < 1000.0) {
        printf("M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f m. ]\n", metrosH);
    } else {
        printf("M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f Km. ]\n", metrosH / 1000.0);
    }
    printf("M8AX - [ Decimales De Pi En Fila ( Arial 10 pt ) Miden - %.5f Km. ]\n",
           ((double) d * 0.25100377) / 100000);
    printf("M8AX - [ Velocidad En Km/h. - %.5f Km/h. ]\n",
           v_kmh);
    printf("M8AX - [ Vueltas Al Mundo Por Segundo - %.8f Vueltas. ]\n", (v_kmh / 3600.0) / 40075.0);
    if (v_kmh > 0) {
        printf("M8AX - [ Tiempo Para Dar Una Vuelta Al Mundo - %.2f Horas. ]\n", 40075.0 / v_kmh);
    }
    double distanciaLuna = 384400.0;
    double segundosALaLuna = (distanciaLuna / v_kmh) * 3600.0;
    long long s_total = (long long) segundosALaLuna;
    int Xdias = s_total / 86400;
    int Xresto_dias = s_total % 86400;
    int Xhoras = Xresto_dias / 3600;
    int Xresto_horas = Xresto_dias % 3600;
    int Xminutos = Xresto_horas / 60;
    int Xsegundos = Xresto_horas % 60;
    printf("M8AX - [ Llegaríamos A La Luna En - %dd %dh %dm %ds. ]\n", Xdias, Xhoras, Xminutos,
           Xsegundos);
    printf("M8AX - [ Bolis Bic Gastados Escribiendo Los Decimales - %.5f Bolis. ]\n",
           (((double) d * 0.25100377) / 100.0) / 2435.0);
    printf("M8AX - [ Bolsas De 1Kg De Arroz. Cada Grano Un Dígito. - %.5f Bolsas. ]\n",
           (double) d / 30000.00000);
    printf("M8AX - [ Litros De Agua. 1 Dígito = 1 Gota De Agua - %.5f Litros. ]\n",
           ((double) d * 0.0512) / 1000.00000);
    printf("M8AX - [ Litros De Agua/Seg Procesados. - %.5f Litros/Seg. ]\n",
           l_seg);
    double segundosPiscina = 2500000.0 / l_seg;
    long long sp_total = (long long) segundosPiscina;
    int P_dias = sp_total / 86400;
    int P_resto_dias = sp_total % 86400;
    int P_horas = P_resto_dias / 3600;
    int P_resto_horas = P_resto_dias % 3600;
    int P_minutos = P_resto_horas / 60;
    int P_segundos = P_resto_horas % 60;
    printf("M8AX - [ Llenaríamos Una Piscina Olímpica En - %dd %dh %dm %ds. ]\n", P_dias, P_horas,
           P_minutos, P_segundos);
    indv = (d_seg * 100.0) / 10000000.0;
    printf("\n------------------------------------------------------------------------\n"
           "M8AX - [ Índice De Velocidad De Tu Móvil - %.3f ]\n"
           "M8AX - [ Ref. 100 Pts = 10M Dec/Seg ] - ¿ Algún Móvil Lo Logrará ?\n"
           "M8AX - [ [10M]: Xiaomi Mi 9 Lite = 5.506 ] | [ Samsung Galaxy S4 Mini = 1.285 ]\n",
           indv);
    if (out == 1) {
        printf("\nM8AX - [ Mapeo En RAM, LIMBS ( GMP ) A Decimales Y Creando Fichero TXT ] - M8AX\n");
        printf("M8AX - [ ESPERA... PROCESO CRÍTICO DE ALTA INTENSIDAD EN RAM Y CPU ] - M8AX\n");
        FILE *a = fopen("/storage/emulated/0/Android/data/com.m8ax_megapi/files/M8AX_Pi.txt", "w");
        for (int j = 0; j < 256; j++) numeris[j] = 0;
        sumd = 0;
        if (a) {
            mp_exp_t exp;
            char *s = mpf_get_str(NULL, &exp, 10, d + 5, qi);
            if (s) {
                char *p = s;
                if (*p == '-') p++;
                fprintf(a, "3.");
                p++;
                idx = 0;
                for (long long i = 0; i < (long long) d; i++) {
                    unsigned char digito = (unsigned char) p[i];
                    fputc(digito, a);
                    numeris[digito]++;
                    sumd += (digito - '0');
                    if (i >= (long long) d - 1000) {
                        ultimos1000[idx++] = digito;
                    }
                }
                ultimos1000[idx] = '\0';
                free(s);
            }
            fclose(a);
            puts("");
            printf("M8AX - [ Proceso Completado Correctamente Y Fichero M8AX_Pi.txt Grabado En /storage/emulated/0/Android/data/com.m8ax_megapi/files/ En %.5f Segundos. ] - M8AX\n\n",
                   (double) (m8ax_tiempo_cpu() - end) / 1000);
        }
        maxi = 0;
        minim = 2147483647;
        chi_square = 0;
        esperado = (double) d / 10.0;
        sumd = 0;
        for (ju = 48; ju <= 57; ju++) {
            sumd = sumd + numeris[ju] * (ju - 48);
            double dif = (double) numeris[ju] - esperado;
            chi_square += (dif * dif) / esperado;
            if (numeris[ju] > maxi) {
                maxi = numeris[ju];
                ja = ju;
            }
            if (numeris[ju] < minim) {
                minim = numeris[ju];
                jar = ju;
            }
            pcen = (numeris[ju] * 100.000) / d;
            printf(". El Dígito %c Aparece %li Veces. El %.3f%c - [ Suma - %li ] .\n", ju,
                   numeris[ju], pcen, 37, numeris[ju] * (ju - 48));
        }
        ab = (numeris[ja] * 100.000) / d;
        bc = (numeris[jar] * 100.000) / d;
        puts("");
        printf("\n. El Dígito Que Más Ha Salido Es El %c. Sale %li Veces, El %.3f%c .\n", ja, maxi,
               ab, 37);
        printf(". El Dígito Que Menos Ha Salido Es El %c. Sale %li Veces, El %.3f%c .\n\n", jar,
               minim, bc, 37);
        printf("M8AX - [ Suma De Todos Los Decimales. 3.14159 = 20. - %li. ] - M8AX\n", sumd);
        printf("M8AX - [ Test De Aleatoriedad Chi-Square: %.4f ] - M8AX\n", chi_square);
        if (chi_square < 16.92) {
            printf("M8AX - [ Distribución: EXCELENTE - Normalidad CPU / RAM ] - M8AX\n");
            printf("M8AX - [ Tu Smartphone Tiene Una Salud Envidiable. ] - M8AX\n\n");
        } else {
            printf("M8AX - [ Distribución: SOSPECHOSA - Posible Error De CPU / RAM ] - M8AX\n");
            printf("M8AX - [ Datos No Uniformes. El Hardware Podría Estar Fallando. ] - M8AX\n\n");
        }
        FILE *archi2 = fopen("/storage/emulated/0/Android/data/com.m8ax_megapi/files/M8AX_Pi.txt",
                             "a");
        if (archi2) {
            fprintf(archi2, " \n\n");
            fprintf(archi2,
                    "-------------------------------------------------------------------------------------------------------------------------\n\n");
            fprintf(archi2, "M8AX - [ Pi - ( %ld Términos ) - %li Decimales. ] - M8AX\n", terms,
                    d);
            for (ju = 48; ju <= 57; ju++) {
                pcen = (numeris[ju] * 100.000) / d;
                fprintf(archi2,
                        "\n... El Dígito [ %c ] Aparece [ %li ] Veces. El [ %.3f%c ] Del Total ... Suma De Dígitos [ %c's ] - [ %li ] ...",
                        ju, numeris[ju], pcen, 37, ju, numeris[ju] * (ju - 48));
            }
            fprintf(archi2,
                    "\n\n-------------------------------------------------------------------------------------------------------------------------\n");
            fprintf(archi2,
                    "\n... El Dígito Que Más Ha Salido Es El %c. Sale %li Veces, El %.3f%c ...\n",
                    ja, maxi, ab, 37);
            fprintf(archi2,
                    "... El Dígito Que Menos Ha Salido Es El %c. Sale %li Veces, El %.3f%c ...\n",
                    jar, minim, bc, 37);
            fprintf(archi2,
                    "\n-------------------------------------------------------------------------------------------------------------------------\n");
            fprintf(archi2, "\nM8AX - [ Test De Aleatoriedad Chi-Square: %.4f ] - M8AX\n",
                    chi_square);
            if (chi_square < 16.92) {
                fprintf(archi2,
                        "M8AX - [ Distribución: EXCELENTE - Distribución De Dígitos Uniforme ( Normalidad CPU / RAM ) ] - M8AX\n");
                fprintf(archi2,
                        "M8AX - [ Tu Smartphone Tiene Una Salud Envidiable ] - M8AX\n");
            } else {
                fprintf(archi2,
                        "M8AX - [ Distribución: SOSPECHOSA - ¡ Atención ! Posible Inestabilidad ( Fallo De CPU / RAM ) ] - M8AX\n");
                fprintf(archi2,
                        "M8AX - [ Los Dígitos No Están Bien Repartidos. El Cálculo Podría Estar Corrupto Por Fallo De Hardware ] - M8AX\n");
                fprintf(archi2,
                        "M8AX - [ Se Recomienda Bajar La Frecuencia O Revisar La Temperatura De Tu Dispositivo ] - M8AX\n");
            }
            fprintf(archi2,
                    "\n-------------------------------------------------------------------------------------------------------------------------\n");
            fprintf(archi2,
                    "\nM8AX - [ Cálculo De %li Dígitos De PI - M.RAM - %.2f MB ] - M8AX\n", d,
                    megas);
            fprintf(archi2, "M8AX - [ Algorítmo Del Cálculo - Fórmula De ChudNovSky. ] - M8AX\n");
            fprintf(archi2,
                    "M8AX - [ Número De Términos. - %ld. Profundidad FFT. - %ld. ] - M8AX\n", terms,
                    depth);
            fprintf(archi2, "M8AX - [ Tamaño De P. - %ld Dígitos. ( %f ) ] - M8AX\n", psize,
                    (double) psize / d);
            fprintf(archi2, "M8AX - [ Tamaño De Q. - %ld Dígitos. ( %f ) ] - M8AX\n", qsize,
                    (double) qsize / d);
            fprintf(archi2,
                    "M8AX - [ Tiempo Total Del Cálculo. - %lia %lid %lih %lim %lis. ] - M8AX\n",
                    anoss, diass, horass, minn, segg);
            fprintf(archi2, "M8AX - [ Libros Del Quijote Procesados. - %.3f Libros. ] - M8AX\n",
                    (double) d / (double) quijote);
            fprintf(archi2,
                    "M8AX - [ Páginas A5 Del Quijote Procesadas. - %.3f Páginas. ] - M8AX\n",
                    (double) d / (double) paginas);
            fprintf(archi2,
                    "M8AX - [ Quijotes Por Segundo Procesados. - %.3f Quijotes/Seg. ] - M8AX\n",
                    q_seg);
            fprintf(archi2, "M8AX - [ Páginas A5 Por Segundo. - %.3f PgsA5/Seg. ] - M8AX\n",
                    p_seg);
            fprintf(archi2, "M8AX - [ Decimales De Pi/Seg. - %.3f DecPi/Seg. ] - M8AX\n",
                    d_seg);
            if (hum2 == 1)
                fprintf(archi2,
                        "M8AX - [ Minutos Leyendo Los Decimales En Voz Alta - %.5f m. ] - M8AX\n",
                        hum);
            else
                fprintf(archi2,
                        "M8AX - [ Días Leyendo Los Decimales En Voz Alta - %.5f d. ] - M8AX\n",
                        hum);
            double metrosH = (double) d * 0.0000000001;
            if (metrosH < 0.001) {
                fprintf(archi2,
                        "M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f mm. ] - M8AX\n",
                        metrosH * 1000.0);
            } else if (metrosH < 1.0) {
                fprintf(archi2,
                        "M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f cm. ] - M8AX\n",
                        metrosH * 100.0);
            } else if (metrosH < 1000.0) {
                fprintf(archi2,
                        "M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f m. ] - M8AX\n",
                        metrosH);
            } else {
                fprintf(archi2,
                        "M8AX - [ Longitud En Fila De Átomos De Hidrógeno - %.6f Km. ] - M8AX\n",
                        metrosH / 1000.0);
            }
            fprintf(archi2,
                    "M8AX - [ Decimales De Pi En Fila ( Arial 10 pt ) Miden - %.5f Km. ] - M8AX\n",
                    ((double) d * 0.25100377) / 100000);
            fprintf(archi2, "M8AX - [ Velocidad En Km/h. - %.5f Km/h. ] - M8AX\n",
                    v_kmh);
            fprintf(archi2, "M8AX - [ Vueltas Al Mundo Por Segundo - %.8f Vueltas. ] - M8AX\n",
                    (v_kmh / 3600.0) / 40075.0);
            if (v_kmh > 0) {
                fprintf(archi2,
                        "M8AX - [ Tiempo Para Dar Una Vuelta Al Mundo - %.2f Horas. ] - M8AX\n",
                        40075.0 / v_kmh);
            }
            double distanciaLuna = 384400.0;
            double segundosALaLuna = (distanciaLuna / v_kmh) * 3600.0;
            long long s_total = (long long) segundosALaLuna;
            int Xdias = s_total / 86400;
            int Xresto_dias = s_total % 86400;
            int Xhoras = Xresto_dias / 3600;
            int Xresto_horas = Xresto_dias % 3600;
            int Xminutos = Xresto_horas / 60;
            int Xsegundos = Xresto_horas % 60;
            fprintf(archi2, "M8AX - [ Llegaríamos A La Luna En - %dd %dh %dm %ds. ] - M8AX\n",
                    Xdias, Xhoras, Xminutos, Xsegundos);
            fprintf(archi2,
                    "M8AX - [ Bolis Bic Gastados Escribiendo Los Decimales - %.5f Bolis. ] - M8AX\n",
                    (((double) d * 0.25100377) / 100.0) / 2435.0);
            fprintf(archi2,
                    "M8AX - [ Bolsas De 1Kg De Arroz. Cada Grano Un Dígito. - %.5f Bolsas. ] - M8AX\n",
                    (double) d / 30000.00000);
            fprintf(archi2,
                    "M8AX - [ Litros De Agua. 1 Dígito = 1 Gota De Agua - %.5f Litros. ] - M8AX\n",
                    ((double) d * 0.0512) / 1000.00000);
            fprintf(archi2, "M8AX - [ Litros De Agua/Seg Procesados. - %.5f Litros/Seg. ] - M8AX\n",
                    l_seg);
            double segundosPiscina = 2500000.0 / l_seg;
            long long sp_total = (long long) segundosPiscina;
            int P_dias = sp_total / 86400;
            int P_resto_dias = sp_total % 86400;
            int P_horas = P_resto_dias / 3600;
            int P_resto_horas = P_resto_dias % 3600;
            int P_minutos = P_resto_horas / 60;
            int P_segundos = P_resto_horas % 60;
            fprintf(archi2,
                    "M8AX - [ Llenaríamos Una Piscina Olímpica En - %dd %dh %dm %ds. ] - M8AX\n",
                    P_dias, P_horas, P_minutos, P_segundos);
            fprintf(archi2, "M8AX - [ Suma De Todos Los Decimales. 3.14159 = 20. - %li. ] - M8AX\n",
                    sumd);
            fprintf(archi2,
                    "\n-------------------------------------------------------------------------------------------------------------------------\n\n"
                    "M8AX - [ Índice De Velocidad De Tu Móvil - %.3f ]\n"
                    "M8AX - [ Referencia: 100 Puntos = 10.000.000 Decimales/Segundo ]\n"
                    "M8AX - [ Histórico [ 10M ]: Xiaomi Mi 9 Lite = 5.506 | Samsung Galaxy S4 Mini = 1.285 ]\n\n"
                    "-------------------------------------------------------------------------------------------------------------------------\n",
                    indv);
            time_t t = time(NULL);
            struct tm tm = *localtime(&t);
            int anio_actual = tm.tm_year + 1900;
            char *anio_romano = convertirARomano(anio_actual);
            fprintf(archi2,
                    "\nProgramado Por MarcoS OchoA DieZ En C++ Y Kotlin\nMail - mviiiax.m8ax@gmail.com\nCanal De YouTube - http://youtube.com/m8ax\nDonaciones PayPal - mviiiax.m8ax@hotmail.es\nDonaciones Bitcoin - bc1qycuse74j86vr65s4n3wms7wgd3awwu792qcd3e\nCreado En Portátil Ninkear A15 Plus Con AMD 5700U Y 32GB De RAM\n\n-------------------------------------------------------------------------------------------------------------------------\n\nGrácias Por Usar M8AX - Mega PI v10.03.77\nPrograma Dedicado A MDDD, Mi Madre...\n");
            time_t tt = time(NULL);
            struct tm tmm = *localtime(&t);
            fprintf(archi2, "Fecha: %02d/%02d/%d - Hora: %02d:%02d:%02d\n\n",
                    tm.tm_mday, tm.tm_mon + 1, tm.tm_year + 1900,
                    tm.tm_hour, tm.tm_min, tm.tm_sec);
            fprintf(archi2,
                    "............................................ MvIiIaX Corp. %d - %s  ...............................................",
                    anio_actual,
                    anio_romano);
            fclose(archi2);
        }
    }
    if (out == 8) {
        printf("\nM8AX - [ Pi - ( %ld Términos ) - %li Decimales. ] - M8AX\n\n", terms, d);
        mpf_out_str(stdout, 10, d + 2, qi);
        printf("\n\n");
    }
    printf("..........................................................................");
    puts("");
    printf("M8AX - [ Últimos 1000 Dígitos Calculados Del Número Pi ] - M8AX\n");
    puts("");
    printf("%s\n", ultimos1000);
    fflush(stdout);
    puts("");
    mpf_clear(pi);
    mpf_clear(qi);
    mpf_clear(t1);
    mpf_clear(t2);
    time_t t = time(NULL);
    struct tm tm = *localtime(&t);
    int anio_actual = tm.tm_year + 1900;
    char *anio_romano = convertirARomano(anio_actual);
    printf("..........................................................................");
    puts("");
    puts("Programado Por MarcoS OchoA DieZ En C++ Y Kotlin\n");
    puts("Mail - mviiiax.m8ax@gmail.com\n");
    puts("Canal De YouTube - http://youtube.com/m8ax\n");
    puts("Donaciones PayPal - mviiiax.m8ax@hotmail.es\n");
    puts("Donaciones Bitcoin - bc1qycuse74j86vr65s4n3wms7wgd3awwu792qcd3e\n");
    puts("Creado En Portatil Ninkear A15 Plus Con AMD 5700U Y 32GB De RAM\n");
    puts("");
    puts("..........................................................................\n");
    puts("");
    puts("Grácias Por Usar M8AX - Mega PI v10.03.77\n");
    puts("Programa Dedicado A MDDD, Mi Madre...");
    time_t tt = time(NULL);
    struct tm tmm = *localtime(&t);
    printf("Fecha: %02d/%02d/%d - Hora: %02d:%02d:%02d\n",
           tm.tm_mday, tm.tm_mon + 1, tm.tm_year + 1900,
           tm.tm_hour, tm.tm_min, tm.tm_sec);
    puts("");
    puts("..........................................................................\n");
    printf("...................... MvIiIaX Corp. %d - %s .......................", anio_actual,
           anio_romano);
    puts("..........................................................................");
    return 0;
}