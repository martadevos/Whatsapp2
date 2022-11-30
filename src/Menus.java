import java.util.Objects;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Menus {

    //Aquí están los diferentes menus que encontrara el usuario a lo largo de la app
    public static void mostrarMenuInicio() {
        System.out.println("Elija una opción:" +
                "\n 1. Crear un nuevo usuario. " +
                "\n 2. Iniciar sesión con un usuario existente." +
                "\n 3. Salir.");
    }

    //Método que realiza la acción elegida por el usuario en el menu inicio
    public static void menuInicio() {
        Scanner s = new Scanner(System.in);
        String idUsuarioIniciado = "";
        int opc;
        mostrarMenuInicio();
        opc = s.nextInt();
        switch (opc) {
            case 1 -> {
                CRUD.recogerDatosEInsertar(1, idUsuarioIniciado);
                menuInicio();
            }
            case 2 -> {
                idUsuarioIniciado = CRUD.iniciarSesion();
                if (!Objects.equals(idUsuarioIniciado, "")) {

                    menuPrincipal(idUsuarioIniciado);
                }
            }
            case 3 -> System.out.println("¡Gracias por utilizar Whatsapp2!\n¡Hasta luego! ;)");
        }
    }

    public static void mostrarMenuPrincipal() {
        System.out.println("Elija una opción:" +
                "\n 1. Ver menu contactos. " +
                "\n 2. Ver mensajes pendientes." +
                "\n 3. Añadir contacto." +
                "\n 4. Salir.");
    }

    //Método que realiza la acción elegida por el usuario en el menu principal tras iniciar sesión
    public static void menuPrincipal(String idUsuarioIniciado) {
        Scanner s = new Scanner(System.in);
        int opc;
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                CRUD.recuentoMensajesSinLeer(idUsuarioIniciado);
            }
        }, 0, 60000);

        mostrarMenuPrincipal();
        opc = s.nextInt();
        switch (opc) {
            case 1 -> menuContacto(idUsuarioIniciado);
            case 2 -> {
                CRUD.recuentoMensajesSinLeer(idUsuarioIniciado);
                CRUD.mostrarMensajesSinLeer(idUsuarioIniciado);
            }
            case 3 -> {
                CRUD.recogerDatosEInsertar(2, idUsuarioIniciado);
                menuPrincipal(idUsuarioIniciado);
            }
            case 4 -> {
                System.out.println("¡Gracias por utilizar Whatsapp2!\n¡Hasta luego! ;)");

                timer.cancel();
                timer.purge();
            }
        }
    }


    public static void mostrarMenuContacto() {
        System.out.println("Elija una opción:" +
                "\n 1. Ver lista contactos" +
                "\n 2. Escribir mensaje " +
                "\n 3. Bloquear/Desbloquear" +
                "\n 4. Borrar contacto" +
                "\n 5. Ver conversación" +
                "\n 6. Volver a menú principal");
    }

    //Método que realiza la acción elegida por el usuario en el menu contacto
    public static void menuContacto(String idUsuarioIniciado) {
        Scanner s = new Scanner(System.in);
        int opc, seguir = 0;
        mostrarMenuContacto();
        opc = s.nextInt();
        switch (opc) {
            case 1 -> CRUD.selectMostrarTablaContactos(idUsuarioIniciado);
            case 2 -> CRUD.enviarMensaje(idUsuarioIniciado);
            case 3 -> CRUD.bloquearDesbloquearContactos(idUsuarioIniciado);
            case 4 -> CRUD.borrarEnTablaContactos(idUsuarioIniciado);
            case 5 -> CRUD.mostrarHistoricoMensajes(idUsuarioIniciado);
            case 6 -> {
                menuPrincipal(idUsuarioIniciado);
                seguir = 1;
            }
        }
        if (seguir == 0) menuContacto(idUsuarioIniciado);
    }

}
