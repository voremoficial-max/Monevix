# Monevix 1.2

Monevix es una aplicación para gestionar trabajadores, productividad, pagos y liquidaciones de forma organizada. La versión 1.2 incorpora un sistema de seguimiento de productividad por periodo, acumulación de pagos, generación de comprobantes en PDF e historial de pagos.

## Funciones principales

### Gestión de trabajadores

Monevix permite registrar y administrar las personas que trabajan en la empresa.

Cada trabajador mantiene su información dentro de la aplicación y sus registros de productividad y pagos quedan asociados a su persona correspondiente.

### Productividad por día

Ahora puedes registrar la productividad de cada trabajador según el día en que realizó el trabajo.

El sistema registra automáticamente la fecha al guardar una productividad.

Ejemplo:

Lunes:
Trabajador: Juan
Productividad: 10 unidades
Valor: $20.000

Martes:
Trabajador: Juan
Productividad: 15 unidades
Valor: $30.000

El sistema mantiene cada registro asociado a su fecha y al trabajador correspondiente.

Esto permite consultar cuánto produjo cada persona durante el periodo seleccionado.

### Calculadora mejorada

La calculadora ahora funciona integrada con el registro de productividad.

Al realizar un cálculo y guardarlo:

* Se registra automáticamente la fecha.
* Se asocia el registro con el trabajador.
* Se incorpora el valor al acumulado correspondiente.
* El registro queda disponible para consultas posteriores.

No necesitas escribir manualmente el día en cada registro.

### Sistema de pagos

Se añadió un menú específico para gestionar los pagos de los trabajadores.

El sistema acumula los valores registrados para cada persona durante el periodo seleccionado.

Cada trabajador mantiene su propio acumulado.

Ejemplo:

Juan:
Lunes: $20.000
Martes: $30.000
Miércoles: $25.000
Jueves: $35.000

Total acumulado: $110.000

Cuando se realiza el pago, el sistema registra la operación y reinicia el acumulado del trabajador en $0 para comenzar un nuevo periodo.

### Botón Pagar

El botón Pagar realiza el cierre del pago del trabajador.

Al presionarlo:

1. Toma el acumulado actual.
2. Registra el pago realizado.
3. Genera el comprobante correspondiente.
4. Guarda el pago en el historial.
5. Reinicia el acumulado del trabajador en $0.

Los pagos anteriores no se eliminan al reiniciar el acumulado.

### Generación de PDF

Cada pago realizado genera un documento PDF con la información correspondiente.

El PDF incluye los datos disponibles del pago, el trabajador, el periodo, el valor pagado y el nombre de la empresa configurado en Monevix.

Esto permite conservar un comprobante de cada liquidación realizada.

### Periodos de pago

Desde Ajustes puedes seleccionar la frecuencia de pago.

Opciones disponibles:

* Semanal
* Quincenal
* Mensual

La opción predeterminada es semanal.

La frecuencia seleccionada determina el periodo utilizado para organizar los pagos y las liquidaciones.

### Historial de pagos

Monevix guarda los pagos realizados para poder consultarlos posteriormente.

El historial muestra los pagos realizados anteriormente junto con su información correspondiente.

Los registros aparecen ordenados desde el pago más reciente hasta el más antiguo.

Esto permite consultar rápidamente la última liquidación realizada.

### Liquidaciones

La sección de liquidaciones permite consultar los pagos realizados y la información asociada a cada periodo.

Los datos de la empresa configurados en Ajustes también aparecen en las liquidaciones.

El historial conserva los pagos anteriores aunque el acumulado actual del trabajador vuelva a cero.

## Ajustes

La sección Ajustes incorpora nuevas opciones para configurar el funcionamiento de los pagos.

### Nombre de la empresa

Ahora puedes introducir el nombre de la empresa.

El nombre guardado se utiliza en las liquidaciones y en los documentos PDF generados por Monevix.

### Frecuencia de pago

Puedes seleccionar el periodo de pago desde Ajustes:

* Semanal
* Quincenal
* Mensual

Por defecto está configurado como semanal.

### Guardado de configuración

Al guardar los cambios aparece una confirmación indicando que la configuración se guardó correctamente.

Esto permite comprobar inmediatamente que los cambios fueron registrados.

### Desplazamiento en Ajustes

La pantalla de Ajustes permite deslizar hacia abajo para acceder a todas las opciones disponibles, incluso cuando el contenido supera el tamaño de la pantalla.

### Botón de regreso

El botón de regreso de Ajustes tiene ahora un color visible para facilitar su identificación y navegación.

## Organización de los datos

Monevix mantiene separados los datos de productividad, acumulados y pagos realizados.

El acumulado actual representa el dinero pendiente de pagar.

El historial conserva los pagos ya realizados.

Cuando se pulsa Pagar, el acumulado pasa a cero, pero el pago queda almacenado en el historial.

## Flujo de trabajo

El funcionamiento principal de Monevix 1.2 sigue este proceso:

1. Registrar o seleccionar un trabajador.
2. Registrar su productividad.
3. La aplicación guarda automáticamente la fecha.
4. El valor se suma al acumulado del trabajador.
5. Repetir el proceso durante los días del periodo.
6. Entrar al menú Pagos.
7. Revisar el acumulado.
8. Pulsar Pagar.
9. Monevix registra el pago.
10. Monevix genera el PDF.
11. El pago aparece en el historial.
12. El acumulado del trabajador vuelve a $0.

## Persistencia de información

Los registros importantes se mantienen almacenados para evitar perder el historial al realizar un nuevo pago.

La información de productividad queda asociada a sus fechas y trabajadores.

Los pagos realizados quedan almacenados en el historial.

El acumulado se reinicia únicamente cuando se confirma un pago.

## Interfaz

La versión 1.2 mantiene la estructura existente de Monevix y añade las nuevas funciones sin modificar las funciones que ya estaban implementadas.

También se corrigieron detalles de navegación en Ajustes para facilitar el uso de la aplicación.

## Versión

Versión actual: Monevix 1.2

Principales incorporaciones:

* Productividad diaria.
* Registro automático de fecha.
* Calculadora integrada con productividad.
* Acumulado individual por trabajador.
* Menú de pagos.
* Pagos semanales.
* Pagos quincenales.
* Pagos mensuales.
* Periodo semanal como opción predeterminada.
* Botón para realizar pagos.
* Reinicio automático del acumulado después del pago.
* Generación de comprobantes PDF.
* Historial de pagos.
* Orden del historial desde el pago más reciente.
* Liquidaciones.
* Nombre de empresa configurable.
* Nombre de empresa en PDF y liquidaciones.
* Confirmación al guardar configuración.
* Desplazamiento vertical en Ajustes.
* Botón de regreso visible en Ajustes.
* Persistencia de los pagos realizados.
