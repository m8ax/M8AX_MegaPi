\# 💪 M8AX - MegaPI 💪



!\[Kotlin](https://img.shields.io/badge/Kotlin-65%25-blue?style=for-the-badge\&logo=kotlin)

!\[C++](https://img.shields.io/badge/C++-35%25-red?style=for-the-badge\&logo=c%2B%2B)

!\[Status](https://img.shields.io/badge/Status-Estable-green?style=for-the-badge)

!\[Android](https://img.shields.io/badge/Android-SDK%2026+-brightgreen?style=for-the-badge\&logo=android)



---



Este repositorio alberga un proyecto de ingeniería de software de alto rendimiento para Android, desarrollado en \*\*Kotlin (65%)\*\* y \*\*C++ nativo (35%)\*\*. \*\*M8AX MegaPI\*\* es una suite de computación avanzada que trasciende las capacidades de un benchmark convencional, integrando algoritmos de precisión arbitraria, motores de juegos nativos y herramientas de monitorización de hardware en tiempo real.



El proyecto destaca por su uso intensivo del \*\*NDK (Native Development Kit)\*\* para maximizar la capacidad de cómputo del silicio ARM64, posicionándose como una herramienta de referencia para el testeo de estabilidad y rendimiento térmico en dispositivos móviles.



---



\## 🛠️ Tecnologías Principales



\- \*\*Lenguaje:\*\* Kotlin y C++ (vía JNI)

\- \*\*Motor de Cálculo:\*\* Implementación del \*\*Algoritmo de Chudnovsky\*\* con Binary Splitting

\- \*\*Librería Matemática:\*\* \*\*GNU MP Library (GMP)\*\* para aritmética de precisión arbitraria

\- \*\*Arquitectura:\*\* Ejecución nativa multihilo optimizada para ARM64-v8a

\- \*\*Interfaz:\*\* Sistema de consola dinámico con renderizado de alto refresco



---



\## ◼ Núcleo de Computación de Alto Rendimiento



El sistema utiliza un puente JNI para ejecutar instrucciones directamente sobre la ALU del procesador, eliminando el "overhead" de la máquina virtual Android.



\- \*\*m8ax\_pi\_engine.cpp:\*\* Motor principal basado en la arquitectura \*\*Mini-Pi de Alexander J. Yee\*\*.

\- \*\*Chudnovsky Algorithm:\*\* Optimizado para el cálculo masivo de decimales mediante series matemáticas complejas.

\- \*\*Binary Splitting:\*\* Técnica de división recursiva para acelerar la computación de fracciones de gran escala.

\- \*\*GMP Integration:\*\* Gestión de números de millones de bits con uso eficiente de la memoria caché.

\- \*\*Stress Test:\*\* Algoritmo diseñado para generar una carga del 100% en todos los núcleos, ideal para diagnosticar \*Thermal Throttling\*.



---



\## ◼ Ecosistema M8AX Integrado



Más allá del cálculo matemático, la aplicación gestiona una serie de módulos operativos que validan la respuesta del sistema bajo carga extrema:



\- \*\*M8AX Chess:\*\* Motor de ajedrez táctico con lógica de evaluación de posiciones en tiempo real.

\- \*\*The Pong:\*\* Implementación del clásico arcade con físicas calculadas en el núcleo nativo para garantizar latencia cero.

\- \*\*Monitor de Sistema:\*\* Análisis dinámico de la memoria RAM y monitorización de la frecuencia de cálculo (dígitos por segundo).

\- \*\*Historial de Benchmarks:\*\* Registro técnico de tiempos de ejecución y récords de profundidad decimal.



---



\## ◼ Seguridad y Estabilidad del Sistema



\- \*\*Gestión de Memoria:\*\* Algoritmos de control que monitorizan la RAM disponible antes de iniciar cálculos de gran envergadura (hasta 800 millones de dígitos).

\- \*\*Arquitectura Multihilo:\*\* Aislamiento del hilo de cálculo principal para evitar el bloqueo de la interfaz de usuario (ANR).

\- \*\*Optimización Térmica:\*\* Diseñado para llevar los procesadores ARM64 a su temperatura crítica de funcionamiento, evaluando la calidad de la disipación del dispositivo.



---



\## 📦 Stack Tecnológico y Dependencias (Gradle)



\### 📱 Core y UI

\- `androidx.core:core-ktx`

\- `androidx.appcompat:appcompat`

\- `com.google.android.material:material`

\- `androidx.constraintlayout:constraintlayout`



\### ⚙️ Computación Nativa (NDK)

\- \*\*C++ Standard:\*\* 17

\- \*\*ABIs:\*\* `arm64-v8a`, `armeabi-v7a`

\- \*\*External Libs:\*\* `libgmp.so` (GNU Multiple Precision)



---



\## ⚙️ Configuración del Envío



\- \*\*Compile / Target SDK:\*\* 34 (Android 14)

\- \*\*Min SDK:\*\* 26 (Android 8.0 Oreo)

\- \*\*NDK Version:\*\* 25.x o superior

\- \*\*CMake:\*\* 3.22.1



---



\## ◼ Notas Finales



> \*\*Rendimiento:\*\* Validado como el "Prime95 de los smartphones", capaz de detectar inestabilidades en el silicio que otros benchmarks sintéticos ignoran.

>

> \*\*Versatilidad:\*\* La integración de juegos nativos permite verificar la respuesta del sistema mientras el motor de PI trabaja en segundo plano.



---



\# 🇺🇸 English Version



\## 💪 M8AX - MegaPI 💪



!\[Kotlin](https://img.shields.io/badge/Kotlin-65%25-blue?style=for-the-badge\&logo=kotlin)

!\[C++](https://img.shields.io/badge/C++-35%25-red?style=for-the-badge\&logo=c%2B%2B)

!\[Status](https://img.shields.io/badge/Status-Stable-green?style=for-the-badge)

!\[Android](https://img.shields.io/badge/Android-SDK%2024+-brightgreen?style=for-the-badge\&logo=android)



---



This repository hosts a \*\*high-performance Android software engineering project\*\*, developed in \*\*65% Kotlin\*\* and \*\*35% Native C++\*\*. \*\*M8AX MegaPI\*\* is an advanced computational suite that goes beyond conventional benchmarks, integrating arbitrary-precision algorithms, native game engines, and real-time hardware monitoring tools.



The project stands out for its intensive use of the \*\*NDK (Native Development Kit)\*\* to maximize the computing power of ARM64 silicon, positioning itself as a reference tool for stability and thermal performance testing on mobile devices.



---



\## 🛠️ Main Technologies



\- \*\*Language:\*\* Kotlin and C++ (via JNI)

\- \*\*Calculation Engine:\*\* Implementation of the \*\*Chudnovsky Algorithm\*\* with Binary Splitting

\- \*\*Math Library:\*\* \*\*GNU MP Library (GMP)\*\* for arbitrary-precision arithmetic

\- \*\*Architecture:\*\* Multi-threaded native execution optimized for ARM64-v8a

\- \*\*Interface:\*\* Dynamic console system with high-refresh rate rendering



---



\## ◼ High-Performance Computing Core



The system uses a JNI bridge to execute instructions directly on the processor's ALU, eliminating Android Virtual Machine overhead.



\- \*\*m8ax\_pi\_engine.cpp:\*\* Core engine based on the \*\*Mini-Pi architecture by Alexander J. Yee\*\*.

\- \*\*Chudnovsky Algorithm:\*\* Optimized for massive decimal calculation using complex mathematical series.

\- \*\*Binary Splitting:\*\* Recursive division technique to accelerate the computation of giant fractions.

\- \*\*GMP Integration:\*\* Handling of million-bit numbers with efficient cache memory usage.

\- \*\*Stress Test:\*\* Algorithm designed to generate a 100% load on all cores, ideal for diagnosing Thermal Throttling.



---



\## ◼ Integrated M8AX Ecosystem



Beyond mathematical calculation, the application manages several operational modules that validate system responsiveness under extreme load:



\- \*\*M8AX Chess:\*\* Tactical chess engine with real-time position evaluation logic.

\- \*\*The Pong:\*\* Classic arcade recreation with physics calculated in the native core to ensure zero latency.

\- \*\*System Monitor:\*\* Dynamic RAM analysis and real-time tracking of calculation frequency (digits per second).

\- \*\*Benchmark History:\*\* Technical log of execution times and decimal depth records.



---



\## ◼ System Security and Stability



\- \*\*Memory Management:\*\* Control algorithms that monitor available RAM before starting large-scale calculations.

\- \*\*Multi-threaded Architecture:\*\* Isolation of the main calculation thread to prevent UI freezing (ANR).

\- \*\*Thermal Optimization:\*\* Designed to push ARM64 processors to their critical operating temperature, allowing evaluation of the device's heat dissipation quality.



---



\## 📦 Tech Stack and Dependencies (Gradle)



\- \*\*C++ Standard:\*\* 17

\- \*\*Core UI:\*\* AndroidX AppCompat \& Material Components

\- \*\*Native Libs:\*\* GNU Multiple Precision (GMP)

\- \*\*Target SDK:\*\* 34



---



> \*\*Performance:\*\* Validated as the "Prime95 for smartphones," capable of detecting silicon instabilities that other synthetic benchmarks ignore.



---

