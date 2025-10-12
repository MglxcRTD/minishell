package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import tokenizer.MissingFileException;
import tokenizer.TCommand;
import tokenizer.TLine;
import tokenizer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Minishell {

    public static void ejecutarLinea(String lineaComando) {
        boolean background = false;

        // Detectar si el comando termina con "&"
        if (lineaComando.trim().endsWith("&")) {
            background = true;
            lineaComando = lineaComando.substring(0, lineaComando.lastIndexOf("&")).trim();
        }

        try {
            TLine linea = Tokenizer.tokenize(lineaComando);
            if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
                System.err.println("Error: comando vacío o inválido.");
                return;
            }

            List<TCommand> comandos = linea.getCommands();

            // Si hay pipes
            if (comandos.size() > 1) {
                ejecutarPipeline(linea, background);
            } else {
                ejecutarComandoSimple(linea, background);
            }

        } catch (MissingFileException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void ejecutarPipeline(TLine linea, boolean background) {
        List<ProcessBuilder> constructores = new ArrayList<>();
        List<TCommand> comandos = linea.getCommands();

        for (int i = 0; i < comandos.size(); i++) {
            TCommand cmd = comandos.get(i);
            ProcessBuilder pb = new ProcessBuilder(cmd.getArgv());

            // Redirección de entrada (solo en el primero)
            if (i == 0 && linea.getRedirectInput() != null) {
                pb.redirectInput(new File(linea.getRedirectInput()));
            }

            // Redirección de salida y error (solo en el último)
            if (i == comandos.size() - 1) {
                aplicarRedirecciones(pb, linea);
            }

            constructores.add(pb);
        }

        try {
            List<Process> procesos = ProcessBuilder.startPipeline(constructores);
            Process ultimo = procesos.get(procesos.size() - 1);

            if (background) {
                System.out.println("[Proceso en background: PID " + ultimo.pid() + "]");
            } else {
                try (BufferedReader out = new BufferedReader(new InputStreamReader(ultimo.getInputStream()));
                     BufferedReader err = new BufferedReader(new InputStreamReader(ultimo.getErrorStream()))) {

                    String lineaOut;
                    while ((lineaOut = out.readLine()) != null) {
                        System.out.println(lineaOut);
                    }

                    String lineaErr;
                    while ((lineaErr = err.readLine()) != null) {
                        System.err.println(lineaErr);
                    }

                    int codigo = ultimo.waitFor();
                    if (codigo != 0) {
                        System.err.println("Pipeline finalizó con error: " + codigo);
                    }
                } catch (InterruptedException e) {
                    System.err.println("Ejecución interrumpida: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Error ejecutando pipeline: " + e.getMessage());
        }
    }

    private static void ejecutarComandoSimple(TLine linea, boolean background) {
        TCommand cmd = linea.getCommands().get(0);
        ProcessBuilder pb = new ProcessBuilder(cmd.getArgv());
        aplicarRedirecciones(pb, linea);

        try {
            Process p = pb.start();

            if (background) {
                System.out.println("[Proceso en background: PID " + p.pid() + "]");
            } else {
                int codigo = p.waitFor();
                if (codigo != 0) {
                    System.err.println("El proceso terminó con error: " + codigo);
                }
            }

        } catch (IOException e) {
            System.err.println("Error ejecutando comando: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Ejecución interrumpida: " + e.getMessage());
        }
    }

    // Método reutilizable para aplicar redirecciones a cualquier ProcessBuilder
    private static void aplicarRedirecciones(ProcessBuilder pb, TLine linea) {
        if (linea.getRedirectInput() != null) {
            pb.redirectInput(new File(linea.getRedirectInput()));
        }

        if (linea.getRedirectOutput() != null) {
            File out = new File(linea.getRedirectOutput());
            if (linea.isAppendOutput()) {
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(out));
            } else {
                pb.redirectOutput(out);
            }
        }

        if (linea.getRedirectError() != null) {
            File err = new File(linea.getRedirectError());
            if (linea.isAppendError()) {
                pb.redirectError(ProcessBuilder.Redirect.appendTo(err));
            } else {
                pb.redirectError(err);
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Introduce el nombre que quieres que salga en la shell:");
        String nombre = sc.nextLine();
        if (nombre.isEmpty()) nombre = "default";

        while (true) {
            System.out.print("\u001B[32m" + nombre + "@>\u001B[0m ");
            String comando = sc.nextLine().trim();

            if (comando.equalsIgnoreCase("exit")) {
                System.out.println("Saliendo del minishell, ¡hasta la próxima!");
                break;
            }

            ejecutarLinea(comando);
        }

        sc.close();
    }
}

