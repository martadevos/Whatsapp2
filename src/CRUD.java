import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class CRUD {

    /**
     * Recibe una tabla y unos datos para insertar en esta y ejecuta la sentencia SQL
     *
     * @param campos ArrayList de cadenas con los campos que se desean insertar
     * @param datos  ArrayList de cadenas con los valores para los campos que se desean insertar
     * @param tabla  cadena con el nombre de la tabla en la que se quiere insertar
     */
    public static void insertarEnTabla(ArrayList<String> campos, ArrayList<String> datos, String tabla) {
        StringBuilder sql = new StringBuilder("INSERT INTO ad2223_cmendoza." + tabla + " (");
        for (int i = 0; i < campos.size() - 1; i++) {
            sql.append(campos.get(i)).append(",");
        }
        sql.append(campos.get(campos.size() - 1)).append(") VALUES (");
        for (int i = 0; i < datos.size() - 1; i++) {
            sql.append(datos.get(i)).append(",");
        }
        sql.append(datos.get(datos.size() - 1)).append(");");
        try {
            MainChat.st.executeUpdate(sql.toString());
        } catch (SQLException e) {
            System.out.println("El registro ya existe");
        }
    }

    /**
     * Bloquea o desbloquea un contacto, si el contacto no existe, muestra un mensaje de error
     *
     * @param usuarioConectado cadena que corresponde al usuario que utiliza la app
     */
    public static void bloquearDesbloquearContactos(String usuarioConectado) {
        Scanner s = new Scanner(System.in);
        StringBuilder sql = new StringBuilder("UPDATE ad2223_cmendoza.Contactos SET bloqueado=");
        String idUsuario2;
        int bloqueado = 2;

        System.out.println("Introduce el usuario a bloquear/desbloquear");
        idUsuario2 = s.nextLine();

        try {
            //Buscamos el contacto
            ResultSet rs = MainChat.st.executeQuery("SELECT * FROM ad2223_cmendoza.Contactos WHERE idUsuario1 LIKE '" + usuarioConectado + "' AND idUsuario2 LIKE '" + idUsuario2 + "'");

            ResultSetMetaData md = rs.getMetaData();
            //Comprobamos el estado de bloqueo y lo cambiamos
            while (rs.next()) {
                bloqueado = rs.getInt(md.getColumnLabel(3));
            }
            if (bloqueado == 0) {
                bloqueado = 1;
            } else if (bloqueado == 1) {
                bloqueado = 0;
            }

            if (bloqueado != 2) {
                sql.append(bloqueado);

                sql.append(" WHERE idUsuario1 LIKE '").append(usuarioConectado).append("' AND idUsuario2 LIKE '").append(idUsuario2).append("'");

                MainChat.st.executeUpdate(sql.toString());

                System.out.println("Se ha bloqueado al usuario " + idUsuario2 + " en tus contactos");
            } else System.out.println("El usuario" + idUsuario2 + "no existe");
        } catch (SQLException e) {
            System.out.println("Hubo un error al borrar los datos de la base de datos");
        }
    }

    /**
     * Pide un contacto y lo borra de la lista de contactos
     *
     * @param usuarioConectado cadena que corresponde al usuario que utiliza la app
     */
    public static void borrarEnTablaContactos(String usuarioConectado) {
        Scanner s = new Scanner(System.in);
        String idUsuario2;

        System.out.println("Introduce el usuario a borrar");
        idUsuario2 = s.nextLine();

        String sql = "DELETE FROM ad2223_cmendoza.Contactos WHERE idUsuario1 LIKE '" + usuarioConectado + "' AND idUsuario2 LIKE '" + idUsuario2 + "'";

        try {
            MainChat.st.executeUpdate(sql);
            System.out.println("Se ha borrado el usuario " + idUsuario2 + " de tu lista de contactos");
        } catch (SQLException e) {
            System.out.println("Hubo un error al borrar los datos de la base de datos");
        }
    }

    /**
     * Recoge datos y los inserta en la base de datos, la primera opci??n crea un usuario y la segunda a??ade un contacto
     *
     * @param idUsuarioIniciado cadena que corresponde al usuario que utiliza la app
     * @param opc               entero que corresponde a la opci??n 1 o 2
     */
    public static void recogerDatosEInsertar(int opc, String idUsuarioIniciado) {
        String tabla = "";
        boolean fallo = false;
        ArrayList<String> campos = new ArrayList<>();
        ArrayList<String> datos = new ArrayList<>();
        switch (opc) {
            //En la primera opci??n creamos un nuevo usuario
            case 1 -> {
                campos.add("nombreUsuario");
                campos.add("contrasena");
                pedirDatos(datos, 1);
                tabla = "Usuario";
            }
            //En la segunda opci??n a??adimos un usuario a nuestra lista de contactos
            case 2 -> {
                campos.add("idUsuario1");
                campos.add("idUsuario2");
                datos.add("'" + idUsuarioIniciado + "'");
                pedirDatos(datos, 2);
                tabla = "Contactos";
                if (Objects.equals(datos.get(0), datos.get(1))) {
                    System.out.println("No puedes a??adirte a ti mismo como contacto ;b");
                    fallo = true;
                }
            }

            default -> {
                fallo = true;
                System.out.println("se ha producido un error");
            }
        }
        if (!fallo) insertarEnTabla(campos, datos, tabla);
    }


    /**
     * Env??a un mensaje a otro usuario y llama al m??todo pedirUsuarioReceptor
     *
     * @param idUsuarioIniciado cadena que representa el nombre del usuario que utiliza la app
     */
    public static void enviarMensaje(String idUsuarioIniciado) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> campos = new ArrayList<>();
        ArrayList<String> datos = new ArrayList<>();
        campos.add("idEmisor");
        campos.add("idReceptor");
        campos.add("texto");
        datos.add(idUsuarioIniciado);
        datos.add(pedirUsuarioReceptor(idUsuarioIniciado));
        System.out.println("Introduzca su mensaje:");
        datos.add("'" + sc.nextLine() + "'");
        insertarEnTabla(campos, datos, "Mensaje");
    }

    /**
     * Pide el usuario al que se desea enviar el mensaje y comprueba si existe el contacto y si no est?? bloqueado
     * y en caso de que si est?? bloqueado o no exista, sigue pidiendo un usuario hasta que se responda uno v??lido
     *
     * @param idUsuarioIniciado cadena que representa el nombre del usuario que utiliza la app
     * @return cadena con el nombre del usuario al que se env??a el mensaje
     */
    public static String pedirUsuarioReceptor(String idUsuarioIniciado) {
        Scanner sc = new Scanner(System.in);
        String idReceptor;
        boolean contactoExiste = true;
        ResultSet rs, rs2;
        do {
            System.out.println("Introduzca el usuario al que desea enviar un mensaje");
            idReceptor = sc.nextLine();
            try {
                String sql = "SELECT * FROM ad2223_cmendoza.Contactos WHERE idUsuario2 LIKE '" + idReceptor + "'AND idUsuario1 LIKE '" + idUsuarioIniciado + "' AND bloqueado=0";
                rs = MainChat.st.executeQuery(sql);
                if (!rs.next()) {
                    contactoExiste = false;
                }
                String sql2 = "SELECT * FROM ad2223_cmendoza.Contactos WHERE idUsuario2 LIKE '" + idUsuarioIniciado + "'AND idUsuario1 LIKE '" + idReceptor + "' AND bloqueado=0";
                rs2 = MainChat.st.executeQuery(sql2);

                if (!rs2.next()) {
                    contactoExiste = false;
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (!contactoExiste) System.out.println("El contacto no existe o est?? bloqueado");
        } while (!contactoExiste);
        return idReceptor;
    }

    /**
     * Muestra un hist??rico de los mensajes con el usuario introducido
     *
     * @param idUsuarioIniciado cadena que corresponde al usuario que utiliza la app
     */
    public static void mostrarHistoricoMensajes(String idUsuarioIniciado) {
        Scanner sc = new Scanner(System.in);
        String idUsuario2;
        boolean contactoExiste = true;
        ResultSet rs;
        try {
            do {
                System.out.println("Introduzca el usuario para ver la conversaci??n");
                idUsuario2 = sc.nextLine();

                String sql = "SELECT * FROM ad2223_cmendoza.Mensaje WHERE idEmisor LIKE '" + idUsuario2 + "' OR '" + idUsuarioIniciado + "' AND idReceptor LIKE '" + idUsuarioIniciado + "' OR '" + idUsuario2 + "'";
                rs = MainChat.st.executeQuery(sql);
                if (!rs.next()) {
                    contactoExiste = false;
                    System.out.println("No hay mensajes con el usuario " + idUsuario2);
                } else {
                    ResultSetMetaData md = rs.getMetaData();
                    do {
                        System.out.println(rs.getString(md.getColumnLabel(2)).toUpperCase(Locale.ROOT) + ": " + rs.getString(md.getColumnLabel(5)) + "       " + rs.getString(md.getColumnLabel(6)));
                    } while (rs.next());
                    System.out.println("--------------------------------------");
                }
            } while (!contactoExiste);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hace un recuento de los mensajes sin leer para el usuario que se pasa por par??metro
     *
     * @param idUsuarioIniciado cadena que corresponde al usuario que utiliza la app
     */
    public static void recuentoMensajesSinLeer(String idUsuarioIniciado) {
        int contador=0;
        ArrayList<String> idEmisores = new ArrayList<>();
        ResultSet rs;
        try {
            String sql = "SELECT * FROM ad2223_cmendoza.Mensaje WHERE idReceptor LIKE '" + idUsuarioIniciado + "' AND leido=0";
            rs = MainChat.st.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                contador++;
                if(!idEmisores.contains(rs.getString(md.getColumnLabel(2)))) {
                    idEmisores.add(rs.getString(md.getColumnLabel(2)));
                }
            }
            System.out.println("Tienes " + contador + " mensajes pendientes.\nMensajes pendientes de:");

            for (String idEmisor : idEmisores) {
                System.out.println(idEmisor);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Muestra un hist??rico de los mensajes sin leer con el usuario introducido
     *
     * @param idUsuarioIniciado cadena que corresponde al usuario que utiliza la app
     */
    public static void mostrarMensajesSinLeer(String idUsuarioIniciado) {
        Scanner sc = new Scanner(System.in);
        String idUsuario2;
        ArrayList<Integer> idMensajes = new ArrayList<>();
        boolean contactoExiste = true;
        ResultSet rs;
        try {
            do {
                System.out.println("Introduzca el usuario para ver los mensajes no Le??dos");
                idUsuario2 = sc.nextLine();

                String sql = "SELECT * FROM ad2223_cmendoza.Mensaje WHERE idEmisor LIKE '" + idUsuario2 + "' AND idReceptor LIKE '" + idUsuarioIniciado + "' AND leido=0";
                rs = MainChat.st.executeQuery(sql);
                if (!rs.next()) {
                    contactoExiste = false;
                    System.out.println("No hay mensajes con el usuario " + idUsuario2);
                } else {
                    ResultSetMetaData md = rs.getMetaData();
                    do {
                        idMensajes.add(rs.getInt(md.getColumnLabel(1)));
                        System.out.println(rs.getString(md.getColumnLabel(2)) + ": " + rs.getString(md.getColumnLabel(5)) + "       " + rs.getString(md.getColumnLabel(6)));
                    } while (rs.next());
                    System.out.println("--------------------------------------");

                    for (Integer idMensaje : idMensajes) {
                        MainChat.st.executeUpdate("UPDATE ad2223_cmendoza.Mensaje SET leido=1 WHERE idMensaje=" + idMensaje);
                    }
                }
            } while (!contactoExiste);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pide datos al usuario por pantalla y los almacena en un array que devuelve despu??s,
     * la opci??n 1 pide datos para crear un usuario y la 2 para a??adir un contacto
     *
     * @param datos Array list que se quiere completar
     * @param opc   entero que corresponde a la opci??n 1 o 2
     */
    public static void pedirDatos(ArrayList<String> datos, int opc) {
        Scanner sc = new Scanner(System.in);
        switch (opc) {
            case 1 -> {
                System.out.println("Introduzca un nombre de usuario");
                datos.add("'" + sc.nextLine() + "'");
                System.out.println("Introduzca una contrase??a");
                datos.add("'" + sc.nextLine() + "'");
            }
            case 2 -> {
                System.out.println("Introduzca un nombre de usuario");
                datos.add("'" + sc.nextLine() + "'");
            }
        }
    }

    /**
     * Pide un usuario y una contrase??a; si el usuario no existe, devuelve al menu de inicio y si existe,
     * pero la contrase??a es incorrecta entra en bucle hasta que se introduzca una correcta
     *
     * @return cadena con el nombre de usuario iniciado, si el usuario no existe, devuelve una cadena vac??a
     */
    public static String iniciarSesion() {
        Scanner s = new Scanner(System.in);
        String usuario, contrasena;
        ResultSet rs;
        boolean salir = false, contrasenaBien;
        System.out.println("Introduce tu usuario");
        usuario = s.nextLine();
        if (!selectTablaUsuario("nombreUsuario", usuario)) {
            usuario = "";
            System.out.println("El usuario introducido no existe");
            Menus.menuInicio();
            salir = true;
        }
        while (!salir) {
            System.out.println("Introduce tu contrase??a");
            contrasena = s.nextLine();
            try {
                rs = MainChat.st.executeQuery("SELECT * FROM ad2223_cmendoza.Usuario WHERE nombreUsuario LIKE '" + usuario + "' AND contrasena LIKE '" + contrasena + "'");
                contrasenaBien = rs.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (!contrasenaBien) {
                System.out.println("La contrase??a introducida no es correcta, int??ntelo de nuevo");
            } else salir = true;
        }
        return usuario;
    }

    /**
     * M??todo que hace un Select de la taba usuario comprobando que el registro con el dato introducido existe
     *
     * @param campo campo al que corresponde el dato que se introduce
     * @param dato  dato para filtrar el select
     * @return un booleano verdadero si se encuentran registros con el dato introducido y falso si no se encuentran registros
     */
    public static boolean selectTablaUsuario(String campo, String dato) {
        boolean usuarioEncontrado;
        ResultSet rs;
        try {
            String sql = "SELECT * FROM ad2223_cmendoza.Usuario WHERE " + campo + " LIKE '" + dato + "'";
            rs = MainChat.st.executeQuery(sql);
            usuarioEncontrado = rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return usuarioEncontrado;
    }

    /**
     * Muestra la lista de contactos del usuario que entra por par??metro
     *
     * @param usuarioConectado cadena que representa al usuario que utiliza la app
     */
    public static void selectMostrarTablaContactos(String usuarioConectado) {
        int bloqueado;
        String cadena = "";
        ResultSet rs;
        System.out.println("----------LISTA DE CONTACTOS----------");
        try {
            String sql = "SELECT * FROM ad2223_cmendoza.Contactos WHERE idUsuario1 LIKE '" + usuarioConectado + "'";
            rs = MainChat.st.executeQuery(sql);

            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                cadena = rs.getString(md.getColumnLabel(2));
                bloqueado = rs.getInt(md.getColumnLabel(3));
                if (bloqueado == 1) cadena += " -> BLOQUEADO";
                System.out.println(cadena);
            }
            if (Objects.equals(cadena, "")) System.out.println("No hay contactos");
            System.out.println("--------------------------------------");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
