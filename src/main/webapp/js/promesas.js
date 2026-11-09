import NotificationFactory from "./utils/notification_factory.js";

// Crear una promesa que simula éxito/fallo con 50% de probabilidad
const miPromesa = new Promise((cumplir, fallar) => {
    setTimeout(() => {
        if (Math.random() > 0.5) {
            cumplir("¡Se cumplió la promesa exitosamente!");
        } else {
            fallar("No se cumplió la promesa (Error simulado)");
        }
    }, 1000);
});

// Consumo de la promesa con .then() y .catch()
miPromesa
    .then(respuesta => console.log("ÉXITO:", respuesta))
    .catch(error => NotificationFactory.emit(document, {
        title: "La promesa no se completó",
        detail: String(error),
        type: "error",
        emitter: "Ejemplo de promesas"
    }));

